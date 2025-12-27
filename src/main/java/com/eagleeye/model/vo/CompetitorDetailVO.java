package com.eagleeye.model.vo;

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
    private LocalDateTime captureTime;
    
    /**
     * 相关标签
     */
    private List<CompetitorTagVO> tags;
    
    /**
     * 分析和建议列表
     */
    private List<String> analysisAndSuggestions;
    
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
} 