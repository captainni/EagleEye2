package com.eagleeye.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 政策建议视图对象
 */
@Data
@ApiModel(value = "PolicySuggestionVO对象", description = "政策建议视图对象")
public class PolicySuggestionVO {

    @ApiModelProperty(value = "建议ID")
    private Long id;

    @ApiModelProperty(value = "建议内容")
    private String suggestion;

    @ApiModelProperty(value = "建议理由")
    private String reason;

} 