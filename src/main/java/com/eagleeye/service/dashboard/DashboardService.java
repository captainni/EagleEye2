package com.eagleeye.service.dashboard;

import com.eagleeye.model.vo.dashboard.DashboardStatsVO;
import com.eagleeye.model.vo.dashboard.PolicySummaryVO;
import com.eagleeye.model.vo.dashboard.CompetitorSummaryVO;
import com.eagleeye.model.vo.dashboard.RequirementSummaryVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {
    
    /**
     * 获取仪表盘统计数据
     * 
     * @return 仪表盘统计数据
     */
    DashboardStatsVO getDashboardStats();
    
    /**
     * 获取政策摘要
     * 
     * @param limit 限制条数
     * @return 政策摘要
     */
    PolicySummaryVO getPolicySummary(Integer limit);
    
    /**
     * 获取竞品摘要
     * 
     * @param limit 限制条数
     * @return 竞品摘要
     */
    CompetitorSummaryVO getCompetitorSummary(Integer limit);
    
    /**
     * 获取需求摘要
     * 
     * @param limit 限制条数
     * @return 需求摘要
     */
    RequirementSummaryVO getRequirementSummary(Integer limit);
} 