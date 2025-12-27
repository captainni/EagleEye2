package com.eagleeye.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞品信息VO (用于列表和仪表盘摘要)
 */
@Data
public class CompetitorVO {
    
    /**
     * 竞品ID
     */
    private Long id;
    
    /**
     * 竞品动态标题
     */
    private String title;
    
    /**
     * 银行名称 (原 company)
     */
    private String bankName;
    
    /**
     * 更新类型 (原 type)
     */
    private String updateType;
    
    /**
     * 更新时间 (原 captureTime)
     * 注意：字段名修改，确保与前端一致。如果后端实际表示的是抓取时间而非更新时间，需澄清。
     * 暂时假定 captureTime 就是前端需要的 updateTime。
     */
    private LocalDateTime updateTime;
    
    /**
     * 相关标签
     */
    private List<CompetitorTagVO> tags;
    
    /**
     * 第一条建议内容 (从 CompetitorAnalysis 获取)
     */
    private String suggestion;
} 