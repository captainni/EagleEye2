import type { PolicyVO } from './policy';
import type { CompetitorVO } from './competitor';
import type { RequirementVO } from './requirement';

/**
 * 仪表盘统计数据
 */
export interface DashboardStatsVO {
  policyStats: PolicyStatVO;
  competitorStats: CompetitorStatVO;
  requirementStats: RequirementStatVO;
  trendStats: TrendStatVO;
}

/**
 * 政策统计数据
 */
export interface PolicyStatVO {
  total: number;
  importanceDistribution: Record<string, number>;
  typeDistribution: Record<string, number>;
}

/**
 * 竞品统计数据
 */
export interface CompetitorStatVO {
  total: number;
  bankDistribution: Record<string, number>;
  updateTypeDistribution: Record<string, number>;
}

/**
 * 需求统计数据
 */
export interface RequirementStatVO {
  total: number;
  priorityDistribution: Record<string, number>;
  statusDistribution: Record<string, number>;
}

/**
 * 时间趋势数据
 */
export interface TrendStatVO {
  lastWeekData: DailyDataVO[];
  lastMonthData: DailyDataVO[];
}

/**
 * 每日数据
 */
export interface DailyDataVO {
  date: string;
  policyCount: number;
  competitorCount: number;
  requirementCount: number;
}

/**
 * 政策摘要
 */
export interface PolicySummaryVO {
  latestImportantPolicies: PolicyVO[];
  policyTypeDistribution: Record<string, number>;
  policySourceDistribution: Record<string, number>;
  policyAreaDistribution: Record<string, number>;
}

/**
 * 竞品摘要
 */
export interface CompetitorSummaryVO {
  latestCompetitorUpdates: CompetitorVO[];
  bankDistribution: Record<string, number>;
  updateTypeDistribution: Record<string, number>;
  tagDistribution: Record<string, number>;
}

/**
 * 需求摘要
 */
export interface RequirementSummaryVO {
  latestRequirements: RequirementVO[];
  priorityDistribution: Record<string, number>;
  statusDistribution: Record<string, number>;
  sourceDistribution: Record<string, number>;
} 