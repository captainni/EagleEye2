/**
 * 附件视图对象
 */
export interface AttachmentVO {
    id: number;
    name: string;
    type: string; // 图标类型，如 'file-alt', 'file-word'
    url: string;
}

/**
 * 需求列表视图对象
 */
export interface RequirementVO {
    id: number;
    title: string;
    status: string;       // 状态码 (NEW, PROCESSING, ...)
    statusText: string;   // 状态文本 (待处理, 处理中, ...)
    statusType: string;   // 状态类型，用于前端显示样式 (orange, warning, ...)
    priority: string;     // 优先级文本 (高, 中, 低)
    priorityLevel: string;// 优先级等级 (high, medium, low)
    sourceType: string;   // 来源类型 (POLICY, COMPETITOR, MANUAL)
    sourceDetail?: string; // 来源详情 (如政策来源机构，竞品公司名称)
    createTime?: string;   // 创建时间 (ISO 格式字符串)
    planTime?: string;   // 计划完成时间 (ISO 格式字符串, 可选)
    owner?: string;       // 负责人 (可选)
    description?: string; // 简短描述 (可选)
    attachments?: any[]; // 附件列表 (可选)
}

/**
 * 需求详情视图对象
 */
export interface RequirementDetailVO extends Omit<RequirementVO, 'briefDescription'> {
    description: string;          // 详细描述
    background?: string;         // 需求背景 (可选)
    completeTime?: string;       // 完成时间 (ISO 格式字符串, 可选)
    attachments?: AttachmentVO[]; // 附件列表 (可选)
    sourceId?: number;          // 来源ID (可选)
}

/**
 * 需求查询条件DTO
 */
export interface RequirementQueryDTO {
    keyword?: string;
    status?: string;
    priority?: string; // 使用优先级等级 (high, medium, low)
    sourceType?: string;
    userId?: number;
    pageNum?: number;
    pageSize?: number;
}

/**
 * 需求创建DTO
 */
export interface RequirementCreateDTO {
    title: string;
    description?: string;
    background?: string;
    priority?: string; // 使用优先级等级 (high, medium, low)
    status?: string;   // 状态码 (NEW, PROCESSING, ...)
    sourceType?: string;
    sourceId?: number;
    userId?: number; // 实际应用中应从当前登录用户获取
    planTime?: string; // ISO 格式字符串
}

/**
 * 需求更新DTO
 */
export interface RequirementUpdateDTO {
    id: number; // id 必须存在于路径中，但DTO中也包含以便于后端处理
    title?: string;
    description?: string;
    background?: string;
    priority?: string; // 使用优先级等级 (high, medium, low)
    status?: string;   // 状态码 (NEW, PROCESSING, ...)
    planTime?: string; // ISO 格式字符串
    completeTime?: string; // ISO 格式字符串
} 