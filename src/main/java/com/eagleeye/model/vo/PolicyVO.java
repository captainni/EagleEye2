package com.eagleeye.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 政策信息VO
 */
@Data
@ApiModel(value = "PolicyVO对象", description = "政策列表项视图对象")
public class PolicyVO {
    
    /**
     * 政策ID
     */
    @ApiModelProperty(value = "政策ID")
    private Long id;
    
    /**
     * 政策标题
     */
    @ApiModelProperty(value = "政策标题")
    private String title;
    
    /**
     * 政策来源
     */
    @ApiModelProperty(value = "政策来源")
    private String source;
    
    /**
     * 原文链接
     */
    private String sourceUrl;
    
    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Or just yyyy-MM-dd
    private LocalDateTime publishTime;
    
    /**
     * 政策类型
     */
    @ApiModelProperty(value = "政策类型")
    private String policyType;
    
    /**
     * 重要程度：高、中、低
     */
    @ApiModelProperty(value = "重要程度")
    private String importance;

    /**
     * 与产品的相关度：高、中、低
     */
    @ApiModelProperty(value = "与产品的相关度")
    private String relevance;

    /**
     * 相关领域
     */
    @ApiModelProperty(value = "相关领域列表")
    private List<String> areas;
    
    /**
     * 政策摘要
     */
    @ApiModelProperty(value = "政策摘要")
    private String summary;
    
    /**
     * 关键条款
     */
    @ApiModelProperty(value = "关键条款列表")
    private List<String> keyPoints;
    
    /**
     * 建议列表
     */
    @ApiModelProperty(value = "政策建议列表")
    private List<PolicySuggestionVO> suggestions;
} 