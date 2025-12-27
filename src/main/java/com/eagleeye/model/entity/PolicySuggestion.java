package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 政策建议实体类
 */
@Data
@TableName("policy_suggestion")
public class PolicySuggestion {
    
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
     * 分析ID
     */
    private Long analysisId;
    
    /**
     * 建议内容
     */
    private String suggestion;
    
    /**
     * 建议理由
     */
    private String reason;
    
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