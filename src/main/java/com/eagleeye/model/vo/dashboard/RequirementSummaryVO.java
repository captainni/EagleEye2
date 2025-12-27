package com.eagleeye.model.vo.dashboard;

import lombok.Data;
import com.eagleeye.model.vo.RequirementVO;
import java.util.List;
import java.util.Map;

/**
 * 需求摘要VO
 */
@Data
public class RequirementSummaryVO {
    
    /**
     * 最新需求列表
     */
    private List<RequirementVO> latestRequirements;
    
    /**
     * 需求优先级分布
     */
    private Map<String, Integer> priorityDistribution;
    
    /**
     * 需求状态分布
     */
    private Map<String, Integer> statusDistribution;
    
    /**
     * 需求来源分布 (政策转化、竞品转化、手动创建)
     */
    private Map<String, Integer> sourceDistribution;
} 