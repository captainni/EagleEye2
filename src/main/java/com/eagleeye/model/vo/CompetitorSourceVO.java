package com.eagleeye.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 竞品资源链接VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorSourceVO {
    
    /**
     * 资源标题
     */
    private String title;
    
    /**
     * 资源URL
     */
    private String url;
} 