package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("爬虫配置更新参数")
public class CrawlerConfigUpdateDTO {

    // ID 由 PathVariable 传入，这里不需要

    @ApiModelProperty(value = "目标名称", example = "中国人民银行政策发布")
    private String targetName;

    @ApiModelProperty(value = "爬虫服务类型 ('legacy'=旧服务, 'eagleeye'=新服务)", example = "eagleeye")
    private String crawlerService;

    @ApiModelProperty(value = "起始/入口 URL 列表 (JSON格式)", example = "[\"http://www.pbc.gov.cn/zhengcehuobisi/125207/125213/index.html\"]")
    private String sourceUrls;

    @ApiModelProperty(value = "抓取深度", example = "1")
    private Integer crawlDepth;

    @ApiModelProperty(value = "触发计划 (Cron 表达式)", example = "0 0 1 * * ?")
    private String triggerSchedule;

    @ApiModelProperty(value = "提取策略类型 (\'css\', \'llm\') - 仅传统服务需要", example = "css")
    private String extractionStrategyType;

    @ApiModelProperty(value = "提取Schema (JSON格式) - 仅传统服务需要", example = "{\"title\":\"h2 a\", \"url\":\"h2 a[href]\"}")
    private String extractionSchema;

    @ApiModelProperty(value = "LLM提取指令")
    private String llmInstruction;

    @ApiModelProperty(value = "LLM提供商配置 (JSON格式)")
    private String llmProviderConfig;

    @ApiModelProperty(value = "Crawl4AI配置覆盖 (JSON格式)")
    private String crawl4aiConfigOverride;

    @ApiModelProperty(value = "是否启用", example = "true")
    private Boolean isActive;
}
 