package com.eagleeye.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫任务执行日志视图对象
 *
 * @author eagleeye
 */
@Data
@ApiModel(description = "爬虫任务执行日志视图对象")
public class CrawlerTaskLogVO {

    @ApiModelProperty("日志ID")
    private Long logId;

    @ApiModelProperty("任务唯一ID")
    private String taskId;

    @ApiModelProperty("关联的爬虫配置ID")
    private Long configId;

    @ApiModelProperty("关联的爬虫配置名称")
    private String configName; // 需要Service层填充

    @ApiModelProperty("本次任务抓取的主要目标URL")
    private String targetUrl;

    @ApiModelProperty("任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 统一格式化
    private LocalDateTime startTime;

    @ApiModelProperty("任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 统一格式化
    private LocalDateTime endTime;

    @ApiModelProperty("执行状态 (success, failure)")
    private String status;

    @ApiModelProperty("错误信息 (如果 status=failure)")
    private String errorMessage;

    @ApiModelProperty("批次文件夹路径")
    private String batchPath;

    @ApiModelProperty("本次爬取的文章数量")
    private Integer articleCount;

    @ApiModelProperty("分类统计 JSON")
    private String categoryStats;

    @ApiModelProperty("分析状态 (pending, analyzing, completed, failed)")
    private String analysisStatus;

    @ApiModelProperty("分析结果 JSON")
    private String analysisResult;

} 