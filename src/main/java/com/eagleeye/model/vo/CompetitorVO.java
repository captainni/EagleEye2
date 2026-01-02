package com.eagleeye.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞品信息VO (用于列表和仪表盘摘要)
 */
@Data
public class CompetitorVO {

    /**
     * 竞品ID
     */
    private Long id;

    /**
     * 竞品动态标题
     */
    private String title;

    /**
     * 银行名称 (原 company)
     */
    private String bankName;

    /**
     * 更新类型 (原 type)
     */
    private String updateType;

    /**
     * 更新时间 (原 captureTime)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 相关标签
     */
    private List<CompetitorTagVO> tags;

    /**
     * 第一条建议内容 (从 CompetitorAnalysis 获取)
     */
    private String suggestion;

    /**
     * 重要程度: 高|中|低
     */
    private String importance;

    /**
     * 相关度: 高|中|低
     */
    private String relevance;

    /**
     * 分析摘要
     */
    private String summary;

    /**
     * 竞品公司/机构名称
     */
    private String company;

    /**
     * 动态类型
     */
    private String type;

    /**
     * 抓取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime captureTime;
} 