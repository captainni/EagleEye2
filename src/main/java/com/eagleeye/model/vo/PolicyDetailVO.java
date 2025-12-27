package com.eagleeye.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 政策详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "PolicyDetailVO对象", description = "政策详情视图对象")
public class PolicyDetailVO extends PolicyVO {
    
    @ApiModelProperty(value = "政策原文")
    private String content;
    
    @ApiModelProperty(value = "影响分析")
    private String impactAnalysis;
    
    @ApiModelProperty(value = "政策建议列表")
    private List<PolicySuggestionVO> suggestions;
} 