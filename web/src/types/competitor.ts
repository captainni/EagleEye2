import type { SuggestionVO } from './suggestion';
import type { CompetitorTagVO } from './competitorTag';

/**
 * 竞品来源链接VO
 */
export interface CompetitorSourceVO {
  title: string;
  url: string;
}

/**
 * 竞品动态VO
 */
export interface CompetitorVO {
  id: number;
  title: string;
  bankName?: string;
  updateType?: string;
  updateTime?: string;
  description?: string;
  screenshots?: string[];
  suggestions?: SuggestionVO[];
  tags?: CompetitorTagVO[];

  // 添加额外可能存在的字段
  company?: string;
  competitor?: string;
  type?: string;
  captureTime?: string;
  publishTime?: string;
  suggestion?: string;
  analysisAndSuggestions?: string[];
  summary?: string;
  content?: string;
  relatedInfo?: string;

  // 竞品分析相关字段
  importance?: string;          // 重要程度: 高|中|低
  relevance?: string;           // 与我方产品的相关度: 高|中|低
  keyPoints?: string[];         // 关键要点数组
  marketImpact?: string;        // 市场影响分析
  competitiveAnalysis?: string; // 竞争态势分析
  ourSuggestions?: SuggestionVO[]; // 应对建议列表

  // 来源链接
  sources?: CompetitorSourceVO[];
} 