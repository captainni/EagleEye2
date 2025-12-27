import type { SuggestionVO } from './suggestion';
import type { CompetitorTagVO } from './competitorTag';

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
} 