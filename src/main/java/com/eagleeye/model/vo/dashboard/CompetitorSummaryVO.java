package com.eagleeye.model.vo.dashboard;

import lombok.Data;
import com.eagleeye.model.vo.CompetitorVO;
import java.util.List;
import java.util.Map;

/**
 * 竞品摘要VO
 */
@Data
public class CompetitorSummaryVO {
    
    /**
     * 最新竞品动态列表
     */
    private List<CompetitorVO> latestCompetitorUpdates;
    
    /**
     * 竞品银行分布
     */
    private Map<String, Integer> bankDistribution;
    
    /**
     * 更新类型分布
     */
    private Map<String, Integer> updateTypeDistribution;
    
    /**
     * 标签分布
     */
    private Map<String, Integer> tagDistribution;
} 