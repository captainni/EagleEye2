package com.eagleeye.service.crawler.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagleeye.model.dto.CrawlerConfigCreateDTO;
import com.eagleeye.model.dto.CrawlerConfigQueryDTO;
import com.eagleeye.model.dto.CrawlerConfigUpdateDTO;
import com.eagleeye.model.entity.CrawlerConfig;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.model.vo.CrawlerConfigDetailVO;
import com.eagleeye.model.vo.CrawlerConfigVO;
import com.eagleeye.repository.CrawlerConfigRepository;
import com.eagleeye.service.crawler.CrawlerConfigAdminService;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CrawlerConfigAdminServiceImpl extends ServiceImpl<CrawlerConfigRepository, CrawlerConfig> implements CrawlerConfigAdminService {

    @Resource
    private RabbitTemplate rabbitTemplate; // 用于发送MQ消息
    
    @Resource
    private CrawlerTaskLogService crawlerTaskLogService; // 添加任务日志服务

    private static final String TASK_QUEUE_NAME = "eagleeye.crawl.tasks"; // 定义队列名称常量

    @Override
    @Transactional // 添加事务注解
    public Long createConfig(CrawlerConfigCreateDTO createDTO) {
        CrawlerConfig config = BeanUtil.copyProperties(createDTO, CrawlerConfig.class);
        config.setIsDeleted(false); // 默认未删除
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        this.save(config);
        return config.getConfigId();
    }

    @Override
    public Page<CrawlerConfigVO> listConfigs(CrawlerConfigQueryDTO queryDTO) {
        Page<CrawlerConfig> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CrawlerConfig> wrapper = Wrappers.lambdaQuery(CrawlerConfig.class)
                .eq(CrawlerConfig::getIsDeleted, false) // 只查询未删除的
                .like(StringUtils.hasText(queryDTO.getKeyword()), CrawlerConfig::getTargetName, queryDTO.getKeyword())
                .eq(StringUtils.hasText(queryDTO.getTargetType()), CrawlerConfig::getTargetType, queryDTO.getTargetType())
                .eq(queryDTO.getIsActive() != null, CrawlerConfig::getIsActive, queryDTO.getIsActive())
                .orderByDesc(CrawlerConfig::getUpdateTime); // 按更新时间降序

        Page<CrawlerConfig> resultPage = this.page(page, wrapper);

        // 转换成 VO
        Page<CrawlerConfigVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<CrawlerConfigVO> voList = resultPage.getRecords().stream()
                .map(config -> BeanUtil.copyProperties(config, CrawlerConfigVO.class))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public CrawlerConfigDetailVO getConfigDetail(Long configId) {
        CrawlerConfig config = this.getOne(Wrappers.lambdaQuery(CrawlerConfig.class)
                                        .eq(CrawlerConfig::getConfigId, configId)
                                        .eq(CrawlerConfig::getIsDeleted, false));
        if (config != null) {
            return BeanUtil.copyProperties(config, CrawlerConfigDetailVO.class);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updateConfig(Long configId, CrawlerConfigUpdateDTO updateDTO) {
        CrawlerConfig existingConfig = this.getById(configId);
        if (existingConfig == null || existingConfig.getIsDeleted()) {
            log.warn("Attempted to update non-existent or deleted config: {}", configId);
            return false; // Or throw exception
        }
        CrawlerConfig configToUpdate = BeanUtil.copyProperties(updateDTO, CrawlerConfig.class);
        configToUpdate.setConfigId(configId); // 确保ID正确
        configToUpdate.setUpdateTime(LocalDateTime.now());
        // 注意：BeanUtil.copyProperties 会覆盖所有字段，如果只想更新非null字段，需要额外处理或使用Mapstruct等
        return this.updateById(configToUpdate);
    }

    @Override
    @Transactional
    public boolean deleteConfig(Long configId) {
        // 逻辑删除
        CrawlerConfig config = new CrawlerConfig();
        config.setConfigId(configId);
        config.setIsDeleted(true);
        config.setUpdateTime(LocalDateTime.now());
        return this.updateById(config);
        // 或者物理删除：return this.removeById(configId);
    }

    @Override
    @Transactional
    public boolean updateConfigStatus(Long configId, Boolean isActive) {
        CrawlerConfig config = new CrawlerConfig();
        config.setConfigId(configId);
        config.setIsActive(isActive);
        config.setUpdateTime(LocalDateTime.now());
        return this.updateById(config);
    }

    @Override
    public boolean triggerConfig(Long configId) {
        CrawlerConfig config = this.getOne(Wrappers.lambdaQuery(CrawlerConfig.class)
                .eq(CrawlerConfig::getConfigId, configId)
                .eq(CrawlerConfig::getIsDeleted, false)
                .eq(CrawlerConfig::getIsActive, true)); // 确保配置存在、未删除且已激活

        if (config == null) {
            log.warn("Cannot trigger config: configId={}, not found, deleted, or inactive.", configId);
            return false;
        }

        // 构造任务消息
        Map<String, Object> taskMessage = new HashMap<>();
        String taskId = UUID.randomUUID().toString(true); // 生成唯一任务ID
        taskMessage.put("task_id", taskId);
        taskMessage.put("config_id", config.getConfigId());
        // 简化处理，直接传递 URLs，实际可能需要更复杂的URL生成逻辑
        // 正确处理 sourceUrls: 按行分割
        List<String> urls = List.of(); // Default to empty list
        if (StringUtils.hasText(config.getSourceUrls())) {
            urls = Arrays.stream(config.getSourceUrls().split("\\r?\\n")) // Split by newline (Windows/Unix)
                    .map(String::trim) // Trim whitespace
                    .filter(StringUtils::hasText) // Filter out empty lines
                    .collect(Collectors.toList());
        }

        if (urls.isEmpty()) {
             log.warn("Cannot trigger config: configId={}, sourceUrls is empty or contains only whitespace.", configId);
             return false; // Do not send task if no valid URLs found
        }

        taskMessage.put("target_urls", urls); // 使用处理后的 URL 列表
        taskMessage.put("trigger_type", "manual"); // 手动触发
        taskMessage.put("timestamp", LocalDateTime.now().toString());

        // 首先记录任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setConfigId(configId);
        taskLog.setTargetUrl(urls.get(0)); // 记录第一个URL作为主要目标
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing"); // 初始状态为处理中
        
        // 异步保存任务日志
        boolean logSaved = crawlerTaskLogService.saveTaskLog(taskLog);
        if (!logSaved) {
            log.warn("Failed to save initial task log for taskId={}, configId={}", taskId, configId);
            // 继续执行，不因日志保存失败而阻止任务触发
        }

        // 发送消息到 RabbitMQ
        try {
            rabbitTemplate.convertAndSend(TASK_QUEUE_NAME, taskMessage);
            log.info("Successfully sent trigger task for configId={} to queue={}", configId, TASK_QUEUE_NAME);
            return true;
        } catch (Exception e) {
            log.error("Failed to send trigger task for configId={} to queue={}", configId, TASK_QUEUE_NAME, e);
            
            // 更新任务日志为失败状态
            if (logSaved) { // 仅当初始日志记录成功时才尝试更新
                taskLog.setEndTime(LocalDateTime.now());
                taskLog.setStatus("failure");
                taskLog.setErrorMessage("Failed to send to MQ: " + e.getMessage());
                // 调用更新方法，而不是插入方法
                boolean updated = crawlerTaskLogService.updateTaskLog(taskLog);
                if (!updated) {
                     log.warn("Failed to update task log status to failure for taskId={}, configId={}", taskId, configId);
                }
            }
            
            return false;
        }
    }
} 