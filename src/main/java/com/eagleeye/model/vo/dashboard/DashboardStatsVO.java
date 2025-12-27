package com.eagleeye.model.vo.dashboard;

import lombok.Data;
import java.util.Map;
import java.util.List;

/**
 * 仪表盘统计数据VO
 */
@Data
public class DashboardStatsVO {
    
    /**
     * 政策统计
     */
    private PolicyStatVO policyStats;
    
    /**
     * 竞品统计
     */
    private CompetitorStatVO competitorStats;
    
    /**
     * 需求统计
     */
    private RequirementStatVO requirementStats;
    
    /**
     * 时间趋势数据
     */
    private TrendStatVO trendStats;
    
    /**
     * 政策统计数据
     */
    @Data
    public static class PolicyStatVO {
        /**
         * 政策总数
         */
        private Integer total;
        
        /**
         * 重要程度分布
         */
        private Map<String, Integer> importanceDistribution;
        
        /**
         * 政策类型分布
         */
        private Map<String, Integer> typeDistribution;
    }
    
    /**
     * 竞品统计数据
     */
    @Data
    public static class CompetitorStatVO {
        /**
         * 竞品动态总数
         */
        private Integer total;
        
        /**
         * 银行分类分布
         */
        private Map<String, Integer> bankDistribution;
        
        /**
         * 更新类型分布
         */
        private Map<String, Integer> updateTypeDistribution;
    }
    
    /**
     * 需求统计数据
     */
    @Data
    public static class RequirementStatVO {
        /**
         * 需求总数
         */
        private Integer total;
        
        /**
         * 优先级分布
         */
        private Map<String, Integer> priorityDistribution;
        
        /**
         * 状态分布
         */
        private Map<String, Integer> statusDistribution;
    }
    
    /**
     * 时间趋势数据
     */
    @Data
    public static class TrendStatVO {
        /**
         * 最近7天每日数据
         */
        private List<DailyDataVO> lastWeekData;
        
        /**
         * 最近30天每日数据
         */
        private List<DailyDataVO> lastMonthData;
    }
    
    /**
     * 每日数据统计
     */
    @Data
    public static class DailyDataVO {
        /**
         * 日期
         */
        private String date;
        
        /**
         * 政策数量
         */
        private Integer policyCount;
        
        /**
         * 竞品数量
         */
        private Integer competitorCount;
        
        /**
         * 需求数量
         */
        private Integer requirementCount;
    }
} 