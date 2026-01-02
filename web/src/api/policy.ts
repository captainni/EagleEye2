import request from './request';
import { CommonPage } from '@/types/common';

// 定义政策建议VO接口 (根据后端调整)
export interface SuggestionVO {
  id: number | string;
  suggestion: string;
  reason?: string;
}

// 定义统一的政策VO接口，包含列表和详情所需字段
export interface PolicyVO {
  id: number | string;
  title: string;
  source?: string;
  sourceUrl?: string;
  publishTime?: string; // 后端是 LocalDateTime，前端接收为字符串
  policyType?: string;
  importance?: string | number | { label?: string; color?: string }; // 兼容不同类型
  relevance?: string; // 与产品的相关度：高、中、低
  areas?: string[];
  summary?: string; // 分析摘要
  suggestions?: SuggestionVO[]; // 建议列表
  // --- 以下为详情页特有字段 ---
  content?: string; // 政策原文
  impactAnalysis?: string; // 影响分析
  // --- 可能的其他字段 ---
  tags?: string[]; // 如果有的话
  categories?: any[]; // 如果有的话
  key_points?: string[]; // 新增：关键点数组
  [key: string]: any; // 允许其他未知字段
}

// 定义政策查询参数接口
export interface PolicyQueryParams {
  keyword?: string; // 对应后端的 keyword，可能搜索 title 或 content
  policyType?: string;
  source?: string;
  importance?: string; // 前端用字符串，传递给后端前可能需要转换
  relevance?: string; // 相关度：高、中、低
  area?: string;
  publishStartTime?: string;
  publishEndTime?: string;
  pageNum: number;
  pageSize: number;
}

// 获取政策列表 - 返回分页的 PolicyVO
export function getPolicyList(params: PolicyQueryParams): Promise<CommonPage<PolicyVO>> {
  // 注意：如果前端importance是中文，后端是字符串，需要转换
  // const apiParams = { ...params };
  // if (params.importance) { ... convert ... }
  return request({
    url: '/v1/policies',
    method: 'get',
    params // : apiParams
  });
}

// 获取政策详情 - 返回单个 PolicyVO
export function getPolicyDetail(id: number): Promise<PolicyVO> {
  return request({
    url: `/v1/policies/${id}`,
    method: 'get'
  });
}

// 将政策转为需求
export function policyToRequirement(id: number, data: any) {
  return request({
    url: `/v1/policies/${id}/to-requirement`,
    method: 'post',
    data
  });
} 