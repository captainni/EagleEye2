/**
 * 建议信息VO
 */
export interface SuggestionVO {
  id: number | string;
  suggestion: string;
  reason?: string;
  // 根据实际后端或API返回结构，可能还需要其他字段
  // 例如：content?: string; // 如果PolicyVO.suggestions[0].content 引用的是这个
} 