package com.eagleeye.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞品查询条件DTO
 */
@Data
public class CompetitorQueryDTO {
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 竞品公司/机构
     */
    private String company;
    
    /**
     * 动态类型
     */
    private String type;
    
    /**
     * 标签
     */
    private String tag;
    
    /**
     * 抓取开始时间
     */
    private LocalDateTime captureStartTime;
    
    /**
     * 抓取结束时间
     */
    private LocalDateTime captureEndTime;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;
    
    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
} 