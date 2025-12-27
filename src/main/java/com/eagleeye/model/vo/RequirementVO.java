package com.eagleeye.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求列表视图对象
 */
@Data
public class RequirementVO {

    /**
     * 需求ID
     */
    private Long id;

    /**
     * 需求标题
     */
    private String title;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 状态类型（用于前端展示不同颜色）
     */
    private String statusType;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 优先级等级
     */
    private String priorityLevel;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源详情
     */
    private String sourceDetail;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 计划完成时间
     */
    private LocalDateTime planTime;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 简短描述（截取描述的前100个字符）
     */
    private String briefDescription;
} 