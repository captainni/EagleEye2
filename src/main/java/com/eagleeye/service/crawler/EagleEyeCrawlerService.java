package com.eagleeye.service.crawler;

import java.util.concurrent.CompletableFuture;

/**
 * EagleEye 爬虫服务接口
 * 用于调用 FastAPI 代理服务，使用 Claude Code Skills 执行爬虫任务
 */
public interface EagleEyeCrawlerService {

    /**
     * 执行爬虫任务（同步）
     * @param sourceName 来源标识 (如: eastmoney_bank)
     * @param listUrl 列表页面 URL
     * @param maxArticles 最大文章数
     * @return 爬取结果
     */
    CrawlResult crawl(String sourceName, String listUrl, Integer maxArticles);

    /**
     * 异步触发爬虫任务
     * @param configId 配置ID
     * @param maxArticles 最大文章数
     * @return 任务ID的CompletableFuture
     */
    CompletableFuture<String> triggerAsync(Long configId, Integer maxArticles);

    /**
     * 爬取结果
     */
    class CrawlResult {
        private Boolean success;
        private String batchPath;
        private Integer articleCount;
        private String categoryStats;
        private String errorMessage;

        public CrawlResult() {}

        public CrawlResult(Boolean success, String batchPath, Integer articleCount, String categoryStats, String errorMessage) {
            this.success = success;
            this.batchPath = batchPath;
            this.articleCount = articleCount;
            this.categoryStats = categoryStats;
            this.errorMessage = errorMessage;
        }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getBatchPath() { return batchPath; }
        public void setBatchPath(String batchPath) { this.batchPath = batchPath; }

        public Integer getArticleCount() { return articleCount; }
        public void setArticleCount(Integer articleCount) { this.articleCount = articleCount; }

        public String getCategoryStats() { return categoryStats; }
        public void setCategoryStats(String categoryStats) { this.categoryStats = categoryStats; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 异步触发响应
     */
    class TriggerAsyncResult {
        private String taskId;
        private String message;

        public TriggerAsyncResult() {}

        public TriggerAsyncResult(String taskId, String message) {
            this.taskId = taskId;
            this.message = message;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
