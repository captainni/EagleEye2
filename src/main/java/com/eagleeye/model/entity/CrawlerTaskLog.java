package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫任务执行日志表
 *
 * @author eagleeye
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("crawler_task_log")
public class CrawlerTaskLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志主键ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 任务唯一ID (来自MQ消息)
     */
    private String taskId;

    /**
     * 关联的爬虫配置ID
     */
    private Long configId;

    /**
     * 本次任务抓取的主要目标URL
     */
    private String targetUrl;

    /**
     * 任务大致开始时间 (由消费者记录)
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间 (抓取完成时间，来自MQ消息)
     */
    private LocalDateTime endTime;

    /**
     * 执行状态 (success, failure)
     */
    private String status;

    /**
     * 错误信息 (如果 status=failure)
     */
    private String errorMessage;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否逻辑删除 (0:否, 1:是)
     */
    @TableLogic // 逻辑删除注解
    private Integer isDeleted;

} 