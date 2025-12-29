package com.eagleeye.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 竞品分析结果 DTO
 * 来自 competitor-analyzer skill 的返回结构
 *
 * @author eagleeye
 */
@Data
public class CompetitorAnalysisResult {

    /**
     * 竞品公司/机构名称
     */
    @JsonProperty("company")
    private String company;

    /**
     * 动态类型 (产品更新|营销活动|财报数据|APP更新|利率调整|合作动态|政策响应)
     */
    @JsonProperty("type")
    private String type;

    /**
     * 重要程度 (高|中|低)
     */
    @JsonProperty("importance")
    private String importance;

    /**
     * 与我方产品的相关度 (高|中|低)
     */
    @JsonProperty("relevance")
    private String relevance;

    /**
     * 相关标签
     */
    @JsonProperty("tags")
    private List<String> tags;

    /**
     * 动态摘要 (200-300字)
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * 关键要点 (用于高亮显示，必须是原文语句)
     */
    @JsonProperty("keyPoints")
    private List<String> keyPoints;

    /**
     * 市场影响分析
     */
    @JsonProperty("marketImpact")
    private String marketImpact;

    /**
     * 竞争态势分析 (对竞争格局的影响，对我方的威胁或机会)
     */
    @JsonProperty("competitiveAnalysis")
    private String competitiveAnalysis;

    /**
     * 针对性建议列表
     */
    @JsonProperty("ourSuggestions")
    private List<Suggestion> ourSuggestions;

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
