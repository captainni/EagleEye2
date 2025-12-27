package com.eagleeye.model.dto;

import lombok.Data;

/**
 * 需求查询条件DTO
 */
@Data
public class RequirementQueryDTO {

    /**
     * 查询关键词（标题或描述）
     */
    private String keyword;

    /**
     * 状态筛选：NEW-新建，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已拒绝
     */
    private String status;

    /**
     * 优先级筛选：HIGH-高，MEDIUM-中，LOW-低
     */
    private String priority;

    /**
     * 来源类型筛选：POLICY-政策建议，COMPETITOR-竞品分析，MANUAL-手动创建
     */
    private String sourceType;

    /**
     * 用户ID筛选
     */
    private Long userId;

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
} 