package com.eagleeye.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 竞品标签VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorTagVO {
    
    /**
     * 标签名称
     */
    private String label;
    
    /**
     * 标签颜色
     */
    private String color;
} 