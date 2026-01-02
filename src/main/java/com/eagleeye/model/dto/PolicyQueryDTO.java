package com.eagleeye.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 政策查询条件DTO
 */
@Data
public class PolicyQueryDTO {
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 政策类型
     */
    private String policyType;
    
    /**
     * 政策来源
     */
    private String source;
    
    /**
     * 重要程度
     */
    private String importance;

    /**
     * 相关度
     */
    private String relevance;
    
    /**
     * 相关领域
     */
    private String area;
    
    /**
     * 发布开始时间
     */
    private LocalDateTime publishStartTime;
    
    /**
     * 发布结束时间
     */
    private LocalDateTime publishEndTime;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;
    
    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
} 