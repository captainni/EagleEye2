package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 爬虫任务日志查询参数
 *
 * @author eagleeye
 */
@Data
@ApiModel(description = "爬虫任务日志查询参数")
public class TaskLogQueryDTO {

    @ApiModelProperty(value = "任务执行状态 (success, failure)")
    private String status;

    @ApiModelProperty(value = "查询起始时间 (格式: yyyy-MM-dd HH:mm:ss)")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "查询结束时间 (格式: yyyy-MM-dd HH:mm:ss)")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "配置ID")
    private Long configId;

    @ApiModelProperty(value = "页码，从1开始")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页记录数")
    private Integer pageSize = 10;
} 