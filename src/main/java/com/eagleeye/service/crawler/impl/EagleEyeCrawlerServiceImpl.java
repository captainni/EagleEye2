package com.eagleeye.service.crawler.impl;

import cn.hutool.core.lang.UUID;
import com.eagleeye.model.entity.CrawlerConfig;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.repository.CrawlerConfigRepository;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import com.eagleeye.service.crawler.EagleEyeCrawlerService;
import com.eagleeye.util.CrawlerUtil;
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
        return crawl(null, sourceName, listUrl, maxArticles);
    }

    @Override
    public CrawlResult crawl(String taskId, String sourceName, String listUrl, Integer maxArticles) {
        logger.info("调用 EagleEye 爬虫服务: taskId={}, sourceName={}, listUrl={}, maxArticles={}", taskId, sourceName, listUrl, maxArticles);

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("listUrl", listUrl);
            requestBody.put("sourceName", sourceName);
            requestBody.put("maxArticles", maxArticles != null ? maxArticles : 3);
            requestBody.put("taskId", taskId != null ? taskId : "");
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
     * 优先从 metadata.json 读取文章数量，否则从字符串解析
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

            // 优先从 metadata.json 读取文章数量
            Integer articleCount = 0;
            String categoryStats = null;

            if (batchPath != null) {
                try {
                    java.nio.file.Path metadataPath = java.nio.file.Paths.get("/home/captain/projects/EagleEye2", batchPath, "metadata.json");
                    if (java.nio.file.Files.exists(metadataPath)) {
                        String metadataContent = new String(java.nio.file.Files.readAllBytes(metadataPath));
                        JsonNode metadataJson = objectMapper.readTree(metadataContent);
                        if (metadataJson.has("articleCount")) {
                            articleCount = metadataJson.get("articleCount").asInt();
                            logger.info("从 metadata.json 读取到文章数量: {}", articleCount);
                        }
                        // 提取分类统计
                        if (metadataJson.has("articles")) {
                            JsonNode articles = metadataJson.get("articles");
                            int policyCount = 0;
                            int competitorCount = 0;
                            for (JsonNode article : articles) {
                                String category = article.has("category") ? article.get("category").asText() : "";
                                if ("policy".equals(category)) policyCount++;
                                else if ("competitor".equals(category)) competitorCount++;
                            }
                            categoryStats = String.format("{\"policy\":%d,\"competitor\":%d}", policyCount, competitorCount);
                            logger.info("分类统计: policyCount={}, competitorCount={}, categoryStats={}", policyCount, competitorCount, categoryStats);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("从 metadata.json 读取失败，尝试字符串解析: {}", e.getMessage());
                }
            }

            // 如果从文件读取失败，尝试从字符串解析 (兼容旧格式)
            if (articleCount == 0) {
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
            }

            return new CrawlResult(true, batchPath, articleCount, categoryStats, null);

        } catch (Exception e) {
            logger.error("提取结果信息失败", e);
            return new CrawlResult(true, null, 0, null, null); // 即使解析失败也返回成功
        }
    }

    /**
     * 从 URL 提取 sourceName
     * 使用 CrawlerUtil.extractSourceNameFromUrl 统一处理
     * 例如: https://bank.eastmoney.com/a/czzyh.html -> bank_eastmoney
     * 例如: https://finance.eastmoney.com/a/20251226.html -> finance_eastmoney
     * 例如: https://bank.jrj.com.cn/ -> bank_jrj
     */
    public String extractSourceNameFromUrl(String url) {
        return CrawlerUtil.extractSourceNameFromUrl(url);
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

        // 从 URL 提取 sourceName (例如: https://bank.eastmoney.com/a/czzyh.html -> eastmoney_czzyh)
        String sourceName = extractSourceNameFromUrl(listUrl);
        logger.info("从 URL 提取 sourceName: {} -> {}", listUrl, sourceName);

        // 生成任务ID
        String taskId = UUID.randomUUID().toString(true);

        // 创建任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setConfigId(configId);
        taskLog.setTargetUrl(listUrl);
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing");
        taskLog.setAnalysisStatus("pending");  // 设置初始分析状态

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
                // 调用同步爬虫方法（带 taskId）
                CrawlResult result = crawl(finalTaskId, finalSourceName, finalListUrl, finalMaxArticles);

                // 更新任务日志 - 先查询现有记录
                CrawlerTaskLog existingLog = crawlerTaskLogService.getById(finalTaskLogId);
                if (existingLog == null) {
                    logger.error("无法找到任务日志记录: logId={}", finalTaskLogId);
                    return;
                }

                existingLog.setEndTime(LocalDateTime.now());
                existingLog.setUpdatedAt(LocalDateTime.now());

                if (result.getSuccess()) {
                    existingLog.setStatus("success");
                    existingLog.setAnalysisStatus("pending");  // 重置为待分析状态
                    existingLog.setBatchPath(result.getBatchPath());
                    existingLog.setArticleCount(result.getArticleCount());
                    existingLog.setCategoryStats(result.getCategoryStats());

                    // 重新查询配置并更新 result_path（避免 lambda 中变量非 final 问题）
                    CrawlerConfig configToUpdate = crawlerConfigRepository.selectById(finalConfigId);
                    if (configToUpdate != null) {
                        configToUpdate.setResultPath(result.getBatchPath());
                        configToUpdate.setUpdateTime(LocalDateTime.now());
                        crawlerConfigRepository.updateById(configToUpdate);
                    }

                    logger.info("异步爬虫执行成功: taskId={}, batchPath={}", finalTaskId, result.getBatchPath());
                } else {
                    existingLog.setStatus("failure");
                    existingLog.setErrorMessage(result.getErrorMessage());
                    existingLog.setAnalysisStatus("failed");  // 失败时设置分析状态
                    logger.warn("异步爬虫执行失败: taskId={}, error={}", finalTaskId, result.getErrorMessage());
                }

                crawlerTaskLogService.updateTaskLog(existingLog);

            } catch (Exception e) {
                logger.error("异步爬虫执行异常: taskId={}", finalTaskId, e);

                // 先查询现有记录
                CrawlerTaskLog existingLog = crawlerTaskLogService.getById(finalTaskLogId);
                if (existingLog != null) {
                    existingLog.setEndTime(LocalDateTime.now());
                    existingLog.setUpdatedAt(LocalDateTime.now());
                    existingLog.setStatus("failure");
                    existingLog.setErrorMessage("执行异常: " + e.getMessage());
                    existingLog.setAnalysisStatus("failed");  // 异常时设置分析状态
                    crawlerTaskLogService.updateTaskLog(existingLog);
                } else {
                    logger.error("无法找到任务日志记录进行异常更新: logId={}", finalTaskLogId);
                }
            }
        });

        // 立即返回任务ID
        return CompletableFuture.completedFuture(taskId);
    }

    @Override
    @Async
    public CompletableFuture<CrawlResult> triggerAsyncWithResult(Long configId, String taskId, Integer maxArticles) {
        logger.info("异步触发 EagleEye 爬虫服务（含taskId）: configId={}, taskId={}, maxArticles={}", configId, taskId, maxArticles);

        // 查询配置
        CrawlerConfig config = crawlerConfigRepository.selectById(configId);
        if (config == null) {
            logger.error("配置不存在: configId={}", configId);
            return CompletableFuture.completedFuture(new CrawlResult(false, null, 0, null, "配置不存在"));
        }

        // 处理 sourceUrls
        String listUrl = null;
        if (config.getSourceUrls() != null && !config.getSourceUrls().isEmpty()) {
            String[] urls = config.getSourceUrls().split("\\r?\\n");
            if (urls.length > 0) {
                listUrl = urls[0].trim();
            }
        }

        if (listUrl == null || listUrl.isEmpty()) {
            return CompletableFuture.completedFuture(new CrawlResult(false, null, 0, null, "配置的 sourceUrls 为空"));
        }

        // 从 URL 提取 sourceName
        String sourceName = extractSourceNameFromUrl(listUrl);

        // 创建任务日志
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setConfigId(configId);
        taskLog.setTargetUrl(listUrl);
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("processing");
        taskLog.setAnalysisStatus("pending");

        boolean logSaved = crawlerTaskLogService.saveTaskLog(taskLog);
        if (!logSaved) {
            logger.warn("Failed to save initial task log for taskId={}", taskId);
        }

        // 异步执行爬虫并返回结果
        final Long finalConfigId = configId;
        final String finalSourceName = sourceName;
        final String finalListUrl = listUrl;
        final Integer finalMaxArticles = maxArticles;
        final String finalTaskId = taskId;
        final Long finalTaskLogId = taskLog.getLogId();

        return CompletableFuture.supplyAsync(() -> {
            CrawlResult result = crawl(finalTaskId, finalSourceName, finalListUrl, finalMaxArticles);

            // 更新任务日志 - 先查询现有记录
            CrawlerTaskLog existingLog = crawlerTaskLogService.getById(finalTaskLogId);
            if (existingLog != null) {
                existingLog.setEndTime(LocalDateTime.now());
                existingLog.setUpdatedAt(LocalDateTime.now());

                if (result.getSuccess()) {
                    existingLog.setStatus("success");
                    existingLog.setAnalysisStatus("pending");
                    existingLog.setBatchPath(result.getBatchPath());
                    existingLog.setArticleCount(result.getArticleCount());
                    existingLog.setCategoryStats(result.getCategoryStats());

                    // 更新配置的 result_path
                    CrawlerConfig configToUpdate = crawlerConfigRepository.selectById(finalConfigId);
                    if (configToUpdate != null) {
                        configToUpdate.setResultPath(result.getBatchPath());
                        configToUpdate.setUpdateTime(LocalDateTime.now());
                        crawlerConfigRepository.updateById(configToUpdate);
                    }

                    logger.info("异步爬虫执行成功: taskId={}, batchPath={}", finalTaskId, result.getBatchPath());
                } else {
                    existingLog.setStatus("failure");
                    existingLog.setErrorMessage(result.getErrorMessage());
                    existingLog.setAnalysisStatus("failed");
                    logger.warn("异步爬虫执行失败: taskId={}, error={}", finalTaskId, result.getErrorMessage());
                }

                crawlerTaskLogService.updateTaskLog(existingLog);
            } else {
                logger.error("无法找到任务日志记录: logId={}", finalTaskLogId);
            }

            return result;
        });
    }
}
