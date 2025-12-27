package com.eagleeye.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求更新DTO
 */
@Data
public class RequirementUpdateDTO {

    /**
     * 需求ID
     */
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
     * 优先级：HIGH-高，MEDIUM-中，LOW-低
     */
    private String priority;

    /**
     * 状态：NEW-新建，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已拒绝
     */
    private String status;

    /**
     * 计划实施时间
     */
    private LocalDateTime planTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
} 