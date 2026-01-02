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
import com.eagleeye.repository.CrawlerTaskLogRepository;
import com.eagleeye.service.crawler.CrawlerConfigAdminService;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import com.eagleeye.service.crawler.EagleEyeCrawlerService;
import com.eagleeye.util.CrawlerUtil;
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

    @Resource
    private CrawlerTaskLogRepository crawlerTaskLogRepository; // 添加任务日志仓库

    @Resource
    private EagleEyeCrawlerService eagleEyeCrawlerService; // EagleEye 爬虫服务

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
                .eq(StringUtils.hasText(queryDTO.getCrawlerService()), CrawlerConfig::getCrawlerService, queryDTO.getCrawlerService())
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
    public boolean reCrawlAndUpdateTask(Long taskLogId) {
        // 1. 获取原任务日志
        CrawlerTaskLog taskLog = crawlerTaskLogRepository.selectById(taskLogId);
        if (taskLog == null) {
            log.warn("任务日志不存在: taskLogId={}", taskLogId);
            return false;
        }

        Long configId = taskLog.getConfigId();
        if (configId == null) {
            log.warn("任务没有关联的配置ID: taskLogId={}", taskLogId);
            return false;
        }

        // 2. 获取配置
        CrawlerConfig config = this.getOne(Wrappers.lambdaQuery(CrawlerConfig.class)
                .eq(CrawlerConfig::getConfigId, configId)
                .eq(CrawlerConfig::getIsDeleted, false)
                .eq(CrawlerConfig::getIsActive, true));

        if (config == null) {
            log.warn("配置不存在、已删除或未激活: configId={}", configId);
            return false;
        }

        // 3. 检查是否使用 eagleeye 爬虫服务
        if (!"eagleeye".equals(config.getCrawlerService())) {
            log.warn("只支持 EagleEye 爬虫服务的再爬取: crawlerService={}", config.getCrawlerService());
            return false;
        }

        // 4. 处理 sourceUrls
        List<String> urls = List.of();
        if (StringUtils.hasText(config.getSourceUrls())) {
            urls = Arrays.stream(config.getSourceUrls().split("\\r?\\n"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        if (urls.isEmpty()) {
            log.warn("配置的 sourceUrls 为空: configId={}", configId);
            return false;
        }

        String listUrl = urls.get(0);

        // 5. 更新原任务状态为处理中
        taskLog.setStatus("processing");
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setEndTime(null);
        taskLog.setErrorMessage(null);
        taskLog.setAnalysisStatus("pending");  // 重置分析状态
        taskLog.setAnalysisResult(null);       // 清空分析结果
        crawlerTaskLogRepository.updateById(taskLog);

        try {
            // 6. 调用 EagleEye 爬虫服务
            // 使用 CrawlerUtil.extractSourceNameFromUrl 从 URL 提取 sourceName
            String sourceName = CrawlerUtil.extractSourceNameFromUrl(listUrl);
            String taskId = taskLog.getTaskId(); // 使用任务ID
            EagleEyeCrawlerService.CrawlResult result = eagleEyeCrawlerService.crawl(
                    taskId, sourceName, listUrl, 3  // 传入 taskId
            );

            // 7. 更新原任务的爬取结果
            taskLog.setEndTime(LocalDateTime.now());
            if (result.getSuccess()) {
                taskLog.setStatus("success");
                taskLog.setBatchPath(result.getBatchPath());
                taskLog.setArticleCount(result.getArticleCount());
                taskLog.setCategoryStats(result.getCategoryStats());

                // 更新配置的 result_path
                config.setResultPath(result.getBatchPath());
                config.setUpdateTime(LocalDateTime.now());
                this.updateById(config);

                log.info("再爬取成功: taskLogId={}, batchPath={}", taskLogId, result.getBatchPath());
            } else {
                taskLog.setStatus("failure");
                taskLog.setErrorMessage(result.getErrorMessage());
                log.warn("再爬取失败: taskLogId={}, error={}", taskLogId, result.getErrorMessage());
            }

            crawlerTaskLogRepository.updateById(taskLog);
            return result.getSuccess();

        } catch (Exception e) {
            log.error("再爬取服务调用失败: taskLogId={}", taskLogId, e);
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus("failure");
            taskLog.setErrorMessage("服务调用失败: " + e.getMessage());
            crawlerTaskLogRepository.updateById(taskLog);
            return false;
        }
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
    public TriggerResult triggerConfigWithTaskId(Long configId) {
        CrawlerConfig config = this.getOne(Wrappers.lambdaQuery(CrawlerConfig.class)
                .eq(CrawlerConfig::getConfigId, configId)
                .eq(CrawlerConfig::getIsDeleted, false)
                .eq(CrawlerConfig::getIsActive, true));

        if (config == null) {
            log.warn("Cannot trigger config: configId={}, not found, deleted, or inactive.", configId);
            return new TriggerResult(false, null, "配置不存在、已删除或未激活");
        }

        // 根据 crawler_service 选择调用方式
        String crawlerService = config.getCrawlerService();
        if ("eagleeye".equals(crawlerService)) {
            // 使用新的 EagleEye 爬虫服务（异步）
            return triggerEagleEyeCrawlerAsync(config);
        } else {
            // 使用旧的 RabbitMQ 方式（立即返回，无 taskId）
            boolean success = triggerLegacyCrawler(config);
            if (success) {
                return new TriggerResult(true, null, "任务已发送到队列");
            } else {
                return new TriggerResult(false, null, "发送到队列失败");
            }
        }
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

        // 根据 crawler_service 选择调用方式
        String crawlerService = config.getCrawlerService();
        if ("eagleeye".equals(crawlerService)) {
            // 使用新的 EagleEye 爬虫服务
            return triggerEagleEyeCrawler(config);
        } else {
            // 使用旧的 RabbitMQ 方式 (默认)
            return triggerLegacyCrawler(config);
        }
    }

    /**
     * 使用 EagleEye 爬虫服务触发爬虫
     */
    private boolean triggerEagleEyeCrawler(CrawlerConfig config) {
        log.info("触发 EagleEye 爬虫服务: configId={}, targetName={}", config.getConfigId(), config.getTargetName());

        // 处理 sourceUrls
        List<String> urls = List.of();
        if (StringUtils.hasText(config.getSourceUrls())) {
            urls = Arrays.stream(config.getSourceUrls().split("\\r?\\n"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        if (urls.isEmpty()) {
            log.warn("Cannot trigger EagleEye crawler: configId={}, sourceUrls is empty.", config.getConfigId());
            return false;
        }

        // 使用第一个 URL 作为列表页，sourceName 使用 targetName
        String listUrl = urls.get(0);
        String sourceName = config.getTargetName().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

        // 记录任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(UUID.randomUUID().toString(true));
        taskLog.setConfigId(config.getConfigId());
        taskLog.setTargetUrl(listUrl);
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing");

        boolean logSaved = crawlerTaskLogService.saveTaskLog(taskLog);
        if (!logSaved) {
            log.warn("Failed to save initial task log for configId={}", config.getConfigId());
        }

        try {
            // 调用 EagleEye 爬虫服务
            EagleEyeCrawlerService.CrawlResult result = eagleEyeCrawlerService.crawl(
                    sourceName, listUrl, 3 // 默认爬取3篇
            );

            // 更新任务日志
            taskLog.setEndTime(LocalDateTime.now());
            if (result.getSuccess()) {
                taskLog.setStatus("success");
                taskLog.setBatchPath(result.getBatchPath());
                taskLog.setArticleCount(result.getArticleCount());
                taskLog.setCategoryStats(result.getCategoryStats());

                // 更新配置的 result_path
                config.setResultPath(result.getBatchPath());
                config.setUpdateTime(LocalDateTime.now());
                this.updateById(config);

                log.info("EagleEye 爬虫执行成功: configId={}, batchPath={}", config.getConfigId(), result.getBatchPath());
            } else {
                taskLog.setStatus("failure");
                taskLog.setErrorMessage(result.getErrorMessage());
                log.warn("EagleEye 爬虫执行失败: configId={}, error={}", config.getConfigId(), result.getErrorMessage());
            }

            // 更新任务日志
            crawlerTaskLogService.updateTaskLog(taskLog);
            return result.getSuccess();

        } catch (Exception e) {
            log.error("EagleEye 爬虫服务调用失败: configId={}", config.getConfigId(), e);
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus("failure");
            taskLog.setErrorMessage("服务调用失败: " + e.getMessage());
            crawlerTaskLogService.updateTaskLog(taskLog);
            return false;
        }
    }

    /**
     * 使用旧的 RabbitMQ 方式触发爬虫
     */
    private boolean triggerLegacyCrawler(CrawlerConfig config) {
        log.info("触发传统爬虫服务: configId={}", config.getConfigId());

        // 构造任务消息
        Map<String, Object> taskMessage = new HashMap<>();
        String taskId = UUID.randomUUID().toString(true);
        taskMessage.put("task_id", taskId);
        taskMessage.put("config_id", config.getConfigId());

        // 处理 sourceUrls
        List<String> urls = List.of();
        if (StringUtils.hasText(config.getSourceUrls())) {
            urls = Arrays.stream(config.getSourceUrls().split("\\r?\\n"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        if (urls.isEmpty()) {
            log.warn("Cannot trigger legacy crawler: configId={}, sourceUrls is empty.", config.getConfigId());
            return false;
        }

        taskMessage.put("target_urls", urls);
        taskMessage.put("trigger_type", "manual");
        taskMessage.put("timestamp", LocalDateTime.now().toString());

        // 记录任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setConfigId(config.getConfigId());
        taskLog.setTargetUrl(urls.get(0));
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing");

        boolean logSaved = crawlerTaskLogService.saveTaskLog(taskLog);
        if (!logSaved) {
            log.warn("Failed to save initial task log for taskId={}", taskId);
        }

        // 发送消息到 RabbitMQ
        try {
            rabbitTemplate.convertAndSend(TASK_QUEUE_NAME, taskMessage);
            log.info("成功发送传统爬虫任务到队列: configId={}", config.getConfigId());
            return true;
        } catch (Exception e) {
            log.error("发送传统爬虫任务失败: configId={}", config.getConfigId(), e);

            if (logSaved) {
                taskLog.setEndTime(LocalDateTime.now());
                taskLog.setStatus("failure");
                taskLog.setErrorMessage("发送到 MQ 失败: " + e.getMessage());
                crawlerTaskLogService.updateTaskLog(taskLog);
            }

            return false;
        }
    }

    /**
     * 使用 EagleEye 爬虫服务触发爬虫（异步，返回任务ID）
     */
    private TriggerResult triggerEagleEyeCrawlerAsync(CrawlerConfig config) {
        log.info("异步触发 EagleEye 爬虫服务: configId={}, targetName={}", config.getConfigId(), config.getTargetName());

        // 【新增】处理 sourceUrls，立即创建日志记录（参考 triggerEagleEyeCrawler 方法第208-219行）
        List<String> urls = List.of();
        if (StringUtils.hasText(config.getSourceUrls())) {
            urls = Arrays.stream(config.getSourceUrls().split("\\r?\\n"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        if (urls.isEmpty()) {
            log.warn("Cannot trigger EagleEye crawler: configId={}, sourceUrls is empty.", config.getConfigId());
            return new TriggerResult(false, null, "配置的 sourceUrls 为空");
        }

        String taskId = UUID.randomUUID().toString(true);

        try {
            // 调用 triggerAsyncWithResult，由它负责创建和更新任务日志
            eagleEyeCrawlerService.triggerAsyncWithResult(config.getConfigId(), taskId, 3);

            log.info("EagleEye 爬虫任务已提交: configId={}, taskId={}", config.getConfigId(), taskId);
            return new TriggerResult(true, taskId, "任务已提交，正在后台执行");

        } catch (Exception e) {
            log.error("EagleEye 爬虫服务调用失败: configId={}", config.getConfigId(), e);
            return new TriggerResult(false, null, "服务调用失败: " + e.getMessage());
        }
    }
}