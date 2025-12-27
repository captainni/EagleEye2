package com.eagleeye.service.crawler;

import com.eagleeye.model.dto.UserSubscriptionCreateDTO;
import com.eagleeye.model.dto.UserSubscriptionUpdateDTO;
import com.eagleeye.model.vo.CrawlerConfigVO;
import com.eagleeye.model.vo.UserSubscriptionVO;

import java.util.List;

/**
 * 用户信息源订阅服务接口
 */
public interface UserSubscriptionService {

    /**
     * 获取当前用户可订阅的信息源列表 (已启用的爬虫配置)
     * @return 可订阅配置列表
     */
    List<CrawlerConfigVO> listAvailableConfigs(); // 注意返回的是 CrawlerConfigVO

    /**
     * 用户订阅一个信息源
     * @param createDTO 订阅参数 (需要包含 configId)
     * @param userId 当前用户ID (从安全上下文获取)
     * @return 新订阅记录的ID
     */
    Long subscribe(UserSubscriptionCreateDTO createDTO, Long userId);

    /**
     * 获取当前用户的所有订阅
     * @param userId 当前用户ID (从安全上下文获取)
     * @return 订阅列表 (包含关联的配置信息)
     */
    List<UserSubscriptionVO> listMySubscriptions(Long userId);

    /**
     * 用户取消订阅
     * @param subscriptionId 订阅记录ID
     * @param userId 当前用户ID (验证权限)
     * @return 是否成功
     */
    boolean unsubscribe(Long subscriptionId, Long userId);

    /**
     * 更新用户的订阅设置 (如通知偏好)
     * @param subscriptionId 订阅记录ID
     * @param updateDTO 更新参数
     * @param userId 当前用户ID (验证权限)
     * @return 是否成功
     */
    boolean updateSubscription(Long subscriptionId, UserSubscriptionUpdateDTO updateDTO, Long userId);
} 