package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求实体类
 */
@Data
@TableName("requirement")
public class Requirement {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 需求描述
     */
    private String description;

    /**
     * 需求背景
     */
    private String background;

    /**
     * 优先级：高、中、低
     */
    private String priority;

    /**
     * 状态：NEW-新建，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已拒绝
     */
    private String status;

    /**
     * 来源类型：POLICY-政策建议，COMPETITOR-竞品分析，MANUAL-手动创建
     */
    private String sourceType;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 计划实施时间
     */
    private LocalDateTime planTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

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