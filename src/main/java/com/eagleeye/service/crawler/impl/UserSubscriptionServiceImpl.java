package com.eagleeye.service.crawler.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagleeye.model.dto.UserSubscriptionCreateDTO;
import com.eagleeye.model.dto.UserSubscriptionUpdateDTO;
import com.eagleeye.model.entity.CrawlerConfig;
import com.eagleeye.model.entity.UserSourceSubscription;
import com.eagleeye.model.vo.CrawlerConfigVO;
import com.eagleeye.model.vo.UserSubscriptionVO;
import com.eagleeye.repository.CrawlerConfigRepository;
import com.eagleeye.repository.UserSourceSubscriptionRepository;
import com.eagleeye.service.crawler.UserSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserSubscriptionServiceImpl extends ServiceImpl<UserSourceSubscriptionRepository, UserSourceSubscription> implements UserSubscriptionService {

    @Resource
    private CrawlerConfigRepository crawlerConfigRepository; // 需要查询配置信息

    @Override
    public List<CrawlerConfigVO> listAvailableConfigs() {
        // 查询所有未删除且已启用的爬虫配置
        List<CrawlerConfig> activeConfigs = crawlerConfigRepository.selectList(
                Wrappers.lambdaQuery(CrawlerConfig.class)
                        .eq(CrawlerConfig::getIsDeleted, false)
                        .eq(CrawlerConfig::getIsActive, true)
                        .orderByAsc(CrawlerConfig::getCrawlerService) // 按服务类型排序
                        .orderByAsc(CrawlerConfig::getTargetName)  // 再按名称排序
        );

        // 转换为 VO
        return activeConfigs.stream()
                .map(config -> BeanUtil.copyProperties(config, CrawlerConfigVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long subscribe(UserSubscriptionCreateDTO createDTO, Long userId) {
        // 检查配置是否存在且可用
        CrawlerConfig config = crawlerConfigRepository.selectOne(
                Wrappers.lambdaQuery(CrawlerConfig.class)
                        .eq(CrawlerConfig::getConfigId, createDTO.getConfigId())
                        .eq(CrawlerConfig::getIsDeleted, false)
                        .eq(CrawlerConfig::getIsActive, true)
        );
        if (config == null) {
            log.warn("User {} attempted to subscribe to non-existent, deleted, or inactive config: {}", userId, createDTO.getConfigId());
            // 可以抛出业务异常
            return null;
        }

        // 检查用户是否已订阅
        boolean alreadySubscribed = this.count(
                Wrappers.lambdaQuery(UserSourceSubscription.class)
                        .eq(UserSourceSubscription::getUserId, userId)
                        .eq(UserSourceSubscription::getConfigId, createDTO.getConfigId())
                        .eq(UserSourceSubscription::getIsDeleted, false) // 检查未删除的订阅
        ) > 0;

        if (alreadySubscribed) {
            log.info("User {} already subscribed to config: {}", userId, createDTO.getConfigId());
            // 可以返回已存在的订阅ID或提示信息，或者直接返回 null/抛异常
            // 这里选择返回 null 表示创建失败（因为已存在）
            return null;
        }


        UserSourceSubscription subscription = BeanUtil.copyProperties(createDTO, UserSourceSubscription.class);
        subscription.setUserId(userId);
        subscription.setSubscriptionStatus("active"); // 默认激活状态
        subscription.setIsDeleted(false);
        subscription.setCreateTime(LocalDateTime.now());
        subscription.setUpdateTime(LocalDateTime.now());

        this.save(subscription);
        return subscription.getSubscriptionId();
    }

    @Override
    public List<UserSubscriptionVO> listMySubscriptions(Long userId) {
        // 查询用户的所有未删除订阅
        List<UserSourceSubscription> subscriptions = this.list(
                Wrappers.lambdaQuery(UserSourceSubscription.class)
                        .eq(UserSourceSubscription::getUserId, userId)
                        .eq(UserSourceSubscription::getIsDeleted, false)
                        .orderByDesc(UserSourceSubscription::getCreateTime)
        );

        if (subscriptions.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关联的配置ID列表
        List<Long> configIds = subscriptions.stream()
                .map(UserSourceSubscription::getConfigId)
                .distinct()
                .collect(Collectors.toList());

        // 查询关联的配置信息
        Map<Long, CrawlerConfig> configMap = crawlerConfigRepository.selectList(
                Wrappers.lambdaQuery(CrawlerConfig.class)
                        .in(CrawlerConfig::getConfigId, configIds)
                        // 也查询已删除或未激活的配置，因为用户可能订阅了之后配置才变更
                        // .eq(CrawlerConfig::getIsDeleted, false)
        ).stream().collect(Collectors.toMap(CrawlerConfig::getConfigId, c -> c));


        // 组装 VO 列表
        return subscriptions.stream().map(sub -> {
            UserSubscriptionVO vo = BeanUtil.copyProperties(sub, UserSubscriptionVO.class);
            CrawlerConfig config = configMap.get(sub.getConfigId());
            if (config != null) {
                vo.setTargetName(config.getTargetName());
                vo.setTargetType(config.getCrawlerService()); // 使用 crawler_service 替代 targetType
                // 可以在这里添加更多需要的配置信息到VO
            } else {
                // 处理配置信息找不到的情况（例如配置被物理删除了）
                vo.setTargetName("配置信息不存在");
                vo.setTargetType("未知");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean unsubscribe(Long subscriptionId, Long userId) {
        // 验证订阅是否存在且属于该用户
        UserSourceSubscription subscription = this.getOne(
                Wrappers.lambdaQuery(UserSourceSubscription.class)
                        .eq(UserSourceSubscription::getSubscriptionId, subscriptionId)
                        .eq(UserSourceSubscription::getUserId, userId)
                        .eq(UserSourceSubscription::getIsDeleted, false) // 确保是未删除的订阅
        );

        if (subscription == null) {
            log.warn("User {} attempted to unsubscribe non-existent or unauthorized subscription: {}", userId, subscriptionId);
            return false;
        }

        // 逻辑删除
        UserSourceSubscription subToDelete = new UserSourceSubscription();
        subToDelete.setSubscriptionId(subscriptionId);
        subToDelete.setIsDeleted(true);
        subToDelete.setUpdateTime(LocalDateTime.now());
        return this.updateById(subToDelete);
        // 物理删除: return this.removeById(subscriptionId);
    }

    @Override
    @Transactional
    public boolean updateSubscription(Long subscriptionId, UserSubscriptionUpdateDTO updateDTO, Long userId) {
        // 验证订阅是否存在且属于该用户
        UserSourceSubscription existingSubscription = this.getOne(
                Wrappers.lambdaQuery(UserSourceSubscription.class)
                        .eq(UserSourceSubscription::getSubscriptionId, subscriptionId)
                        .eq(UserSourceSubscription::getUserId, userId)
                        .eq(UserSourceSubscription::getIsDeleted, false) // 确保是未删除的订阅
        );

        if (existingSubscription == null) {
            log.warn("User {} attempted to update non-existent or unauthorized subscription: {}", userId, subscriptionId);
            return false;
        }

        UserSourceSubscription subToUpdate = BeanUtil.copyProperties(updateDTO, UserSourceSubscription.class);
        subToUpdate.setSubscriptionId(subscriptionId); // 确保ID正确
        subToUpdate.setUpdateTime(LocalDateTime.now());
        // 同样注意 BeanUtil 的覆盖问题
        return this.updateById(subToUpdate);
    }
} 