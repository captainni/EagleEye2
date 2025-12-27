import type { PolicyVO } from '@/types/policy';

/**
 * 统一处理政策对象的重要性字段，确保其为字符串类型
 * @param policy 原始政策对象
 * @returns 处理后的政策对象
 */
export function normalizePolicy(policy: PolicyVO): PolicyVO {
  const normalized = { ...policy };
  
  // 处理importance字段
  if (typeof normalized.importance === 'object' && normalized.importance?.label) {
    normalized.importance = normalized.importance.label;
  } else if (typeof normalized.importance === 'number') {
    normalized.importance = String(normalized.importance);
  }
  
  return normalized;
}

/**
 * 规范化政策列表
 * @param policies 原始政策列表
 * @returns 处理后的政策列表
 */
export function normalizePolicies(policies: PolicyVO[]): PolicyVO[] {
  return policies.map(normalizePolicy);
} 