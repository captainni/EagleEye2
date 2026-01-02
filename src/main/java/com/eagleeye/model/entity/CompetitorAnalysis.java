package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞品分析建议实体类
 */
@Data
@TableName("competitor_analysis")
public class CompetitorAnalysis {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的竞品信息ID
     */
    private Long competitorId;
    
    /**
     * 分析和建议内容
     */
    private String content;

    /**
     * 重要程度: 高|中|低
     */
    private String importance;

    /**
     * 相关度: 高|中|低
     */
    private String relevance;

    /**
     * 关键要点 (JSON格式存储)
     */
    private String keyPoints;

    /**
     * 市场影响分析
     */
    private String marketImpact;

    /**
     * 竞争态势分析
     */
    private String competitiveAnalysis;

    /**
     * 针对性建议列表 (JSON格式存储)
     */
    private String ourSuggestions;

    /**
     * 排序序号
     */
    private Integer sortOrder;
    
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