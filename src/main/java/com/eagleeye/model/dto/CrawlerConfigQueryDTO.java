package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("爬虫配置查询参数")
public class CrawlerConfigQueryDTO {

    @ApiModelProperty(value = "目标名称关键词", example = "人民银行")
    private String keyword; // 可用于模糊搜索 targetName

    @ApiModelProperty(value = "爬虫服务类型 ('legacy'=旧服务, 'eagleeye'=新服务)", example = "eagleeye")
    private String crawlerService;

    @ApiModelProperty(value = "是否启用 (true/false)", example = "true")
    private Boolean isActive;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1; // Default value

    @ApiModelProperty(value = "每页记录数", example = "10")
    private Integer pageSize = 10; // Default value
} 