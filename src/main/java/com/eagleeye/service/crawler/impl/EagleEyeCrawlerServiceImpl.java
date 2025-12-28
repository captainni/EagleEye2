package com.eagleeye.service.crawler.impl;

import cn.hutool.core.lang.UUID;
import com.eagleeye.model.entity.CrawlerConfig;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.repository.CrawlerConfigRepository;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import com.eagleeye.service.crawler.EagleEyeCrawlerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * EagleEye 爬虫服务实现
 * 调用 FastAPI 代理服务，使用 Claude Code Skills 执行爬虫任务
 */
@Service
public class EagleEyeCrawlerServiceImpl implements EagleEyeCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(EagleEyeCrawlerServiceImpl.class);

    @Value("${eagleeye.crawler.proxy-url:http://localhost:8000/api/crawl}")
    private String proxyUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private CrawlerTaskLogService crawlerTaskLogService;

    @Resource
    private CrawlerConfigRepository crawlerConfigRepository;

    @Override
    public CrawlResult crawl(String sourceName, String listUrl, Integer maxArticles) {
        logger.info("调用 EagleEye 爬虫服务: sourceName={}, listUrl={}, maxArticles={}", sourceName, listUrl, maxArticles);

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("listUrl", listUrl);
            requestBody.put("sourceName", sourceName);
            requestBody.put("maxArticles", maxArticles != null ? maxArticles : 3);
            requestBody.put("useSkill", true);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 调用 FastAPI 代理服务
            ResponseEntity<String> response = restTemplate.exchange(
                    proxyUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                logger.error("爬虫服务返回错误状态: {}", response.getStatusCode());
                return new CrawlResult(false, null, null, null, "HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("调用爬虫服务失败", e);
            return new CrawlResult(false, null, null, null, e.getMessage());
        }
    }

    /**
     * 解析 FastAPI 代理服务的响应
     * 响应格式: {"success":true,"method":"skill","data":"{Claude Code JSON 输出}"}
     */
    private CrawlResult parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (!root.has("success") || !root.get("success").asBoolean()) {
                return new CrawlResult(false, null, null, null, "服务返回失败");
            }

            String data = root.get("data").asText();
            JsonNode dataJson = objectMapper.readTree(data);

            // Claude Code 的 JSON 输出包含 result 字段
            if (dataJson.has("result")) {
                String result = dataJson.get("result").asText();
                return extractFromResult(result);
            } else if (dataJson.has("permission_denials") && dataJson.get("permission_denials").size() > 0) {
                return new CrawlResult(false, null, null, null, "权限被拒绝: " + dataJson.get("permission_denials").toString());
            } else {
                return new CrawlResult(false, null, null, null, "未知响应格式");
            }

        } catch (Exception e) {
            logger.error("解析响应失败", e);
            return new CrawlResult(false, null, null, null, "解析响应失败: " + e.getMessage());
        }
    }

    /**
     * 从 Claude Code 的 result 字段中提取爬取信息
     * result 格式示例: "爬虫任务执行完成！...批次路径: crawl_files/20251228_100307_eastmoney_bank/..."
     */
    private CrawlResult extractFromResult(String result) {
        try {
            // 提取批次路径 (格式: crawl_files/20251228_100307_eastmoney_bank/)
            String batchPath = null;
            int pathIndex = result.indexOf("crawl_files/");
            if (pathIndex != -1) {
                int endIndex = result.indexOf("/", pathIndex + 20);
                if (endIndex != -1) {
                    batchPath = result.substring(pathIndex, endIndex + 1);
                } else {
                    batchPath = result.substring(pathIndex).trim() + "/";
                }
            }

            // 提取文章数量 (查找 "成功爬取 X 篇文章")
            Integer articleCount = 0;
            int successIndex = result.indexOf("成功爬取");
            if (successIndex != -1) {
                int numStart = result.indexOf(" ", successIndex + 5);
                if (numStart != -1) {
                    int numEnd = result.indexOf(" ", numStart + 1);
                    if (numEnd != -1) {
                        String numStr = result.substring(numStart + 1, numEnd);
                        try {
                            articleCount = Integer.parseInt(numStr);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // 提取分类统计 (简化处理，可以后续增强)
            String categoryStats = null;

            return new CrawlResult(true, batchPath, articleCount, categoryStats, null);

        } catch (Exception e) {
            logger.error("提取结果信息失败", e);
            return new CrawlResult(true, null, 0, null, null); // 即使解析失败也返回成功
        }
    }

    @Override
    @Async
    public CompletableFuture<String> triggerAsync(Long configId, Integer maxArticles) {
        logger.info("异步触发 EagleEye 爬虫服务: configId={}, maxArticles={}", configId, maxArticles);

        // 查询配置
        CrawlerConfig config = crawlerConfigRepository.selectById(configId);
        if (config == null) {
            logger.error("配置不存在: configId={}", configId);
            return CompletableFuture.failedFuture(new RuntimeException("配置不存在: configId=" + configId));
        }

        // 处理 sourceName 和 listUrl
        String listUrl = null;
        if (config.getSourceUrls() != null && !config.getSourceUrls().isEmpty()) {
            String[] urls = config.getSourceUrls().split("\\r?\\n");
            if (urls.length > 0) {
                listUrl = urls[0].trim();
            }
        }

        if (listUrl == null || listUrl.isEmpty()) {
            logger.error("配置的 sourceUrls 为空: configId={}", configId);
            return CompletableFuture.failedFuture(new RuntimeException("配置的 sourceUrls 为空: configId=" + configId));
        }

        String sourceName = config.getTargetName().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

        // 生成任务ID
        String taskId = UUID.randomUUID().toString(true);

        // 创建任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setConfigId(configId);
        taskLog.setTargetUrl(listUrl);
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing");

        boolean logSaved = crawlerTaskLogService.saveTaskLog(taskLog);
        if (!logSaved) {
            logger.warn("Failed to save initial task log for taskId={}", taskId);
        }

        // 异步执行爬虫任务
        final Long finalConfigId = configId;
        final String finalTaskId = taskId;
        final Long finalTaskLogId = taskLog.getLogId();
        final String finalSourceName = sourceName;
        final String finalListUrl = listUrl;
        final Integer finalMaxArticles = maxArticles;

        CompletableFuture.runAsync(() -> {
            try {
                // 调用同步爬虫方法
                CrawlResult result = crawl(finalSourceName, finalListUrl, finalMaxArticles);

                // 更新任务日志
                CrawlerTaskLog updateLog = new CrawlerTaskLog();
                updateLog.setLogId(finalTaskLogId);
                updateLog.setEndTime(LocalDateTime.now());

                if (result.getSuccess()) {
                    updateLog.setStatus("success");
                    updateLog.setBatchPath(result.getBatchPath());
                    updateLog.setArticleCount(result.getArticleCount());
                    updateLog.setCategoryStats(result.getCategoryStats());

                    // 重新查询配置并更新 result_path（避免 lambda 中变量非 final 问题）
                    CrawlerConfig configToUpdate = crawlerConfigRepository.selectById(finalConfigId);
                    if (configToUpdate != null) {
                        configToUpdate.setResultPath(result.getBatchPath());
                        configToUpdate.setUpdateTime(LocalDateTime.now());
                        crawlerConfigRepository.updateById(configToUpdate);
                    }

                    logger.info("异步爬虫执行成功: taskId={}, batchPath={}", finalTaskId, result.getBatchPath());
                } else {
                    updateLog.setStatus("failure");
                    updateLog.setErrorMessage(result.getErrorMessage());
                    logger.warn("异步爬虫执行失败: taskId={}, error={}", finalTaskId, result.getErrorMessage());
                }

                crawlerTaskLogService.updateTaskLog(updateLog);

            } catch (Exception e) {
                logger.error("异步爬虫执行异常: taskId={}", finalTaskId, e);
                CrawlerTaskLog errorLog = new CrawlerTaskLog();
                errorLog.setLogId(finalTaskLogId);
                errorLog.setEndTime(LocalDateTime.now());
                errorLog.setStatus("failure");
                errorLog.setErrorMessage("执行异常: " + e.getMessage());
                crawlerTaskLogService.updateTaskLog(errorLog);
            }
        });

        // 立即返回任务ID
        return CompletableFuture.completedFuture(taskId);
    }
}
