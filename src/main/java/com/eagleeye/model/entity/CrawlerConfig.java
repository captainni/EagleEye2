package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫任务配置表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("crawler_config")
public class CrawlerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置主键ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 目标名称 (e.g., \'中国人民银行政策发布\')
     */
    private String targetName;

    /**
     * 起始/入口 URL 列表 (建议JSON格式)
     */
    private String sourceUrls;

    /**
     * 抓取深度 (可选, 默认1)
     */
    private Integer crawlDepth;

    /**
     * 爬虫服务类型 ('legacy'=旧服务, 'eagleeye'=新服务)
     */
    private String crawlerService;

    /**
     * 最新爬取结果文件夹路径
     */
    private String resultPath;

    /**
     * 触发计划 (Cron 表达式)
     */
    private String triggerSchedule;

    /**
     * 提取策略类型 (\'css\', \'llm\')
     */
    private String extractionStrategyType;

    /**
     * 提取Schema (JSON格式: CSS选择器或LLM的Pydantic Schema)
     */
    private String extractionSchema;

    /**
     * LLM提取指令 (当策略为LLM时)
     */
    private String llmInstruction;

    /**
     * LLM提供商配置 (当策略为LLM时, JSON格式)
     */
    private String llmProviderConfig;

    /**
     * Crawl4AI配置覆盖 (JSON格式, 用于覆盖CrawlerRunConfig)
     */
    private String crawl4aiConfigOverride;

    /**
     * 是否启用: 0-禁用, 1-启用
     */
    private Boolean isActive; // 使用 Boolean 对应 tinyint(1)

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    private Boolean isDeleted; // 使用 Boolean 对应 tinyint(1)

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

} 