import request from './request';
import { CommonPage } from '@/types/common'; // 假设通用类型定义在此

// 竞品来源链接接口 (与Mock一致)
export interface CompetitorSourceVO {
  title: string;
  url: string;
}

// 定义竞品VO接口 (需要根据后端CompetitorVO调整)
// 更新：添加详情页需要的字段
export interface CompetitorVO {
  id: number | string;
  title: string;
  company?: string;
  competitor?: string;
  type?: string;
  captureTime?: string;
  publishTime?: string;
  tags?: any[]; // 可以是字符串数组，也可以是对象数组 { label, color }
  summary?: string;
  suggestion?: string; // 列表页可能使用
  content?: string; // 详情页的详细内容
  relatedInfo?: string; // 详情页的相关信息（HTML）
  analysisAndSuggestions?: string[]; // 分析与建议
  sources?: CompetitorSourceVO[]; // 来源链接
  screenshots?: string[]; // 截图
  // ... 其他字段
  [key: string]: any; // 允许其他未知字段
}

// 定义竞品查询参数接口
export interface CompetitorQueryParams {
  keyword?: string;
  competitor?: string;
  type?: string;
  publishTimeStart?: string;
  publishTimeEnd?: string;
  tag?: string;
  importance?: number;
  pageNum: number;
  pageSize: number;
}

// 获取竞品列表 - 明确返回类型为分页对象
export function getCompetitorList(params: CompetitorQueryParams): Promise<CommonPage<CompetitorVO>> {
  return request({
    url: '/v1/competitors',
    method: 'get',
    params
  });
}

// 获取竞品详情 - 明确返回类型为单个竞品对象
export function getCompetitorDetail(id: number): Promise<CompetitorVO> {
  return request({
    url: `/v1/competitors/${id}`,
    method: 'get'
  });
}

// 将竞品动态转为需求
export function competitorToRequirement(id: number, data: any) {
  return request({
    url: `/v1/competitors/${id}/to-requirement`,
    method: 'post',
    data
  });
} 