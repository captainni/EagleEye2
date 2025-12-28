package com.eagleeye.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel("爬虫配置视图对象 (列表)")
public class CrawlerConfigVO {

    @ApiModelProperty("配置主键ID")
    private Long configId;

    @ApiModelProperty("目标名称")
    private String targetName;

    @ApiModelProperty("爬虫服务类型 ('legacy'=旧服务, 'eagleeye'=新服务)")
    private String crawlerService;

    @ApiModelProperty("最新爬取结果文件夹路径")
    private String resultPath;

    @ApiModelProperty("触发计划 (Cron 表达式)")
    private String triggerSchedule;

    @ApiModelProperty("提取策略类型 (\'css\', \'llm\')")
    private String extractionStrategyType;

    @ApiModelProperty("是否启用")
    private Boolean isActive;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
} 