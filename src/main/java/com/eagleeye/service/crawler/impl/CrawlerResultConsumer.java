package com.eagleeye.service.crawler.impl;

import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.repository.CrawlerTaskLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
// RabbitMQ 相关功能已注释，当前项目未使用 MQ
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 爬虫结果消费者，监听来自爬虫服务的消息
 *
 * 注意：当前项目未使用 RabbitMQ，此类已禁用
 *
 * @author eagleeye
 * @deprecated RabbitMQ 功能已注释，此类不再使用
 */
@Slf4j
// @Component  // 注释掉组件注册，禁用 RabbitMQ 消费者
@Deprecated
public class CrawlerResultConsumer {

    @Resource
    private CrawlerTaskLogRepository crawlerTaskLogRepository;

    @Resource
    private ObjectMapper objectMapper;

    private static final String RESULT_QUEUE_NAME = "eagleeye.crawl.results";

    /**
     * 消费爬虫任务完成的消息，更新任务日志状态
     *
     * @param message 消息内容
     */
    @Transactional
    // @RabbitListener(queues = RESULT_QUEUE_NAME)  // 注释掉 RabbitMQ 监听器
    public void consumeResult(Map<String, Object> message) {
        try {
            log.info("Received crawler result: {}", message);

            // 提取任务信息
            String taskId = (String) message.get("task_id");
            String status = (String) message.get("status"); // 应该是 "success" 或 "failure"
            String errorMessage = (String) message.get("error_message"); // 如果失败，可能包含错误信息

            if (taskId == null || status == null) {
                log.error("Invalid crawler result message, missing required fields: {}", message);
                return;
            }

            // 查找任务日志
            CrawlerTaskLog taskLog = crawlerTaskLogRepository.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrawlerTaskLog>()
                            .eq(CrawlerTaskLog::getTaskId, taskId)
                            .eq(CrawlerTaskLog::getIsDeleted, false)
            );

            if (taskLog == null) {
                log.warn("Task log not found for taskId={}", taskId);
                return;
            }

            // 更新任务日志
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus(status);
            
            if ("failure".equals(status) && errorMessage != null) {
                taskLog.setErrorMessage(errorMessage);
            }

            // 保存更新
            int updated = crawlerTaskLogRepository.updateById(taskLog);
            if (updated > 0) {
                log.info("Updated task log status to {} for taskId={}", status, taskId);
            } else {
                log.warn("Failed to update task log for taskId={}", taskId);
            }

        } catch (Exception e) {
            log.error("Error processing crawler result message", e);
        }
    }
} 