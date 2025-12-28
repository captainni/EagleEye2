package com.eagleeye.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 政策分析结果 DTO
 * 来自 policy-analyzer skill 的返回结构
 *
 * @author eagleeye
 */
@Data
public class AnalysisResult {

    /**
     * 政策类型 (监管政策|行业指引|通知公告|其他)
     */
    @JsonProperty("policyType")
    private String policyType;

    /**
     * 重要程度 (高|中|低)
     */
    @JsonProperty("importance")
    private String importance;

    /**
     * 与产品的相关度 (高|中|低)
     * 新增字段，用于表示政策与用户产品的相关程度
     */
    @JsonProperty("relevance")
    private String relevance;

    /**
     * 相关领域标签
     */
    @JsonProperty("areas")
    private List<String> areas;

    /**
     * 政策摘要 (200-300字)
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * 关键条款 (用于高亮显示)
     */
    @JsonProperty("keyPoints")
    private List<String> keyPoints;

    /**
     * 影响分析
     */
    @JsonProperty("impactAnalysis")
    private String impactAnalysis;

    /**
     * 可执行建议列表
     */
    @JsonProperty("suggestions")
    private List<Suggestion> suggestions;

    /**
     * 单个建议项
     */
    @Data
    public static class Suggestion {
        /**
         * 具体建议
         */
        @JsonProperty("suggestion")
        private String suggestion;

        /**
         * 建议理由
         */
        @JsonProperty("reason")
        private String reason;
    }
}
