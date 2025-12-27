package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 政策信息实体类
 */
@Data
@TableName("policy_info")
public class PolicyInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 政策标题
     */
    private String title;
    
    /**
     * 政策来源
     */
    private String source;
    
    /**
     * 原文链接
     */
    private String sourceUrl;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 政策原文
     */
    private String content;
    
    /**
     * 政策类型
     */
    private String policyType;
    
    /**
     * 重要程度：高、中、低
     */
    private String importance;
    
    /**
     * 相关领域 (JSON格式存储)
     */
    private String areas;
    
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