package com.eagleeye.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel("爬虫配置详情视图对象")
public class CrawlerConfigDetailVO {

    @ApiModelProperty("配置主键ID")
    private Long configId;

    @ApiModelProperty("目标名称")
    private String targetName;

    @ApiModelProperty("爬虫服务类型 ('legacy'=旧服务, 'eagleeye'=新服务)")
    private String crawlerService;

    @ApiModelProperty("最新爬取结果文件夹路径")
    private String resultPath;

    @ApiModelProperty("起始/入口 URL 列表 (JSON格式)")
    private String sourceUrls;

    @ApiModelProperty("抓取深度")
    private Integer crawlDepth;

    @ApiModelProperty("触发计划 (Cron 表达式)")
    private String triggerSchedule;

    @ApiModelProperty("提取策略类型 (\'css\', \'llm\')")
    private String extractionStrategyType;

    @ApiModelProperty("提取Schema (JSON格式)")
    private String extractionSchema;

    @ApiModelProperty("LLM提取指令")
    private String llmInstruction;

    @ApiModelProperty("LLM提供商配置 (JSON格式)")
    private String llmProviderConfig;

    @ApiModelProperty("Crawl4AI配置覆盖 (JSON格式)")
    private String crawl4aiConfigOverride;

    @ApiModelProperty("是否启用")
    private Boolean isActive;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    // 可以根据需要添加关联信息，比如最近一次执行状态、执行历史等
    // @ApiModelProperty("最近一次执行状态")
    // private String lastExecutionStatus;
    // @ApiModelProperty("最近一次执行时间")
    // private LocalDateTime lastExecutionTime;
} 