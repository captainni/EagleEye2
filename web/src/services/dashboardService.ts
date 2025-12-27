import axiosInstance from '@/utils/axiosInstance';
import type { CommonResult } from '@/types/common';
import type {
  DashboardStatsVO,
  PolicySummaryVO,
  CompetitorSummaryVO,
  RequirementSummaryVO
} from '@/types/dashboard';

const BASE_URL = '/v1/dashboard';

/**
 * 获取仪表盘统计数据
 * @returns 仪表盘统计数据
 */
export const getDashboardStats = (): Promise<CommonResult<DashboardStatsVO>> => {
  return axiosInstance.get(`${BASE_URL}/stats`);
};

/**
 * 获取政策摘要
 * @param limit 限制条数
 * @returns 政策摘要数据
 */
export const getPolicySummary = (limit: number = 3): Promise<CommonResult<PolicySummaryVO>> => {
  console.log('Getting policy summary with limit:', limit);
  // 确保传入的limit至少为3
  const actualLimit = limit < 3 ? 3 : limit;
  return axiosInstance.get(`${BASE_URL}/policy-summary`, { params: { limit: actualLimit } });
};

/**
 * 获取竞品摘要
 * @param limit 限制条数
 * @returns 竞品摘要数据
 */
export const getCompetitorSummary = (limit: number = 3): Promise<CommonResult<CompetitorSummaryVO>> => {
  return axiosInstance.get(`${BASE_URL}/competitor-summary`, { params: { limit } });
};

/**
 * 获取需求摘要
 * @param limit 限制条数
 * @returns 需求摘要数据
 */
export const getRequirementSummary = (limit: number = 4): Promise<CommonResult<RequirementSummaryVO>> => {
  return axiosInstance.get(`${BASE_URL}/requirement-summary`, { params: { limit } });
};
 