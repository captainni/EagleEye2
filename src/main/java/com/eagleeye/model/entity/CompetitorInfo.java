package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞品信息实体类
 */
@Data
@TableName("competitor_info")
public class CompetitorInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 动态类型：产品更新、营销活动、财报数据、APP更新、利率调整、合作动态等
     */
    private String type;
    
    /**
     * 抓取时间
     */
    private LocalDateTime captureTime;
    
    /**
     * 相关标签 (JSON格式存储)
     */
    private String tags;
    
    /**
     * 详细内容
     */
    private String content;
    
    /**
     * 来源链接(多个链接用JSON数组存储)
     */
    private String sources;
    
    /**
     * 分析摘要
     */
    private String summary;
    
    /**
     * 相关信息/详细说明
     */
    private String relatedInfo;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Boolean isDeleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 