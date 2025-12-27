package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("爬虫配置创建参数")
public class CrawlerConfigCreateDTO {

    @ApiModelProperty(value = "目标名称", required = true, example = "中国人民银行政策发布")
    @NotBlank(message = "目标名称不能为空")
    private String targetName;

    @ApiModelProperty(value = "目标类型 (\'policy\', \'competitor\')", required = true, example = "policy")
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @ApiModelProperty(value = "起始/入口 URL 列表 (JSON格式)", required = true, example = "[\"http://www.pbc.gov.cn/zhengcehuobisi/125207/125213/index.html\"]")
    @NotBlank(message = "源URL不能为空")
    private String sourceUrls;

    @ApiModelProperty(value = "抓取深度", example = "1")
    private Integer crawlDepth = 1; // Default value

    @ApiModelProperty(value = "触发计划 (Cron 表达式)", example = "0 0 * * * ?")
    private String triggerSchedule;

    @ApiModelProperty(value = "提取策略类型 (\'css\', \'llm\')", required = true, example = "css")
    @NotBlank(message = "提取策略类型不能为空")
    private String extractionStrategyType;

    @ApiModelProperty(value = "提取Schema (JSON格式)", required = true, example = "{\"title\":\"h2 a\", \"url\":\"h2 a[href]\"}")
    @NotBlank(message = "提取Schema不能为空")
    private String extractionSchema;

    @ApiModelProperty(value = "LLM提取指令")
    private String llmInstruction;

    @ApiModelProperty(value = "LLM提供商配置 (JSON格式)")
    private String llmProviderConfig;

    @ApiModelProperty(value = "Crawl4AI配置覆盖 (JSON格式)")
    private String crawl4aiConfigOverride;

    @ApiModelProperty(value = "是否立即启用", example = "true")
    @NotNull(message = "是否启用状态不能为空")
    private Boolean isActive = true; // Default value
} 