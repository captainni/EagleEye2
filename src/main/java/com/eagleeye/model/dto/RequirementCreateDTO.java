package com.eagleeye.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求创建DTO
 */
@Data
public class RequirementCreateDTO {

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
     * 优先级：HIGH-高，MEDIUM-中，LOW-低
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
} 