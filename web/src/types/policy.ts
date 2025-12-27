import type { SuggestionVO } from './suggestion';

/**
 * 政策信息VO
 */
export interface PolicyVO {
  id: number | string;
  title: string;
  source?: string;
  sourceUrl?: string;
  publishTime?: string;
  policyType?: string;
  importance?: string | number | { label?: string; color?: string };
  areas?: string[];
  summary?: string;
  keyPoints?: string[];
  suggestions?: SuggestionVO[];
  content?: string;
  impactAnalysis?: string;
  tags?: string[];
  categories?: any[];
  [key: string]: any;
}