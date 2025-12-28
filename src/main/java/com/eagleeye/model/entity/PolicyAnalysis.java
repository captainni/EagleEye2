package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 政策分析实体类
 */
@Data
@TableName("policy_analysis")
public class PolicyAnalysis {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 政策ID
     */
    private Long policyId;
    
    /**
     * 政策摘要
     */
    private String summary;
    
    /**
     * 关键条款 (JSON格式存储)
     */
    private String keyPoints;
    
    /**
     * 影响分析
     */
    private String impactAnalysis;

    /**
     * 与产品的相关度：高、中、低
     */
    private String relevance;

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