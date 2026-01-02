package com.eagleeye.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞品详情VO
 */
@Data
public class CompetitorDetailVO {

    /**
     * 竞品ID
     */
    private Long id;

    /**
     * 竞品动态标题
     */
    private String title;

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

    /**
     * 相关标签
     */
    private List<CompetitorTagVO> tags;

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
     * 关键要点列表
     */
    private List<String> keyPoints;

    /**
     * 市场影响分析
     */
    private String marketImpact;

    /**
     * 竞争态势分析
     */
    private String competitiveAnalysis;

    /**
     * 分析和建议列表
     */
    private List<String> analysisAndSuggestions;

    /**
     * 应对建议列表（来自 our_suggestions JSON 字段）
     */
    private List<PolicySuggestionVO> ourSuggestions;

    /**
     * 相关信息/详细说明
     */
    private String relatedInfo;

    /**
     * 来源链接
     */
    private List<CompetitorSourceVO> sources;

    /**
     * 截图链接列表
     */
    private List<String> screenshots;

    /**
     * 详细内容
     */
    private String content;
} 