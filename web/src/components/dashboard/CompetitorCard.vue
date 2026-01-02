<template>
  <Card title="最新竞品动态" icon="lightbulb" moreLink="/competitor-tracking">
    <div class="space-y-4">
      <div v-for="competitor in mappedCompetitors" :key="competitor.id" class="border-b pb-3" :class="{ 'border-b-0': competitor.id === mappedCompetitors.length }">
        <router-link :to="`/competitor-detail/${competitor.id}`" class="hover:text-primary cursor-pointer">
          <p class="text-sm font-medium text-gray-700 mb-1">{{ competitor.title }}</p>
        </router-link>
        <p class="text-xs text-gray-500 mb-1">竞品：{{ competitor.company }} | 类型：{{ competitor.type }} | 时间：{{ competitor.date }}</p>
        <p class="text-xs text-secondary">建议：{{ competitor.suggestion }}</p>
      </div>
    </div>
  </Card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import Card from '../common/Card.vue';
import type { CompetitorVO } from '../../types/competitor';
import type { SuggestionVO } from '../../types/suggestion';
import { format } from 'date-fns';

interface Competitor {
  id: number | string;
  title: string;
  company: string;
  type: string;
  date: string;
  suggestion: string;
  link: string;
}

const props = defineProps<{
  competitors: CompetitorVO[];
}>();

// 将API返回的数据格式转换为组件所需格式
const mappedCompetitors = computed(() => {
  return props.competitors.map(comp => {
    // 简化建议获取逻辑，与政策卡片保持一致
    let suggestionText = '暂无建议';

    // 从 suggestion 字段中提取简短建议（格式：## 应对建议\n\n- **建议**: xxx\n  **理由**: xxx）
    if (comp.suggestion && typeof comp.suggestion === 'string') {
      // 查找"应对建议"部分
      const match = comp.suggestion.match(/## 应对建议\s*\n\s*-\s*\*\*建议\*\*:\s*([^\n]+)/);
      if (match && match[1]) {
        suggestionText = match[1].trim();
      } else {
        // 如果没有找到应对建议格式，尝试提取第一条建议
        const firstSuggestion = comp.suggestion.match(/-\s*\*\*建议\*\*:\s*([^\n]+)/);
        if (firstSuggestion && firstSuggestion[1]) {
          suggestionText = firstSuggestion[1].trim();
        }
      }
    }

    // 更安全地格式化日期
    let date = '未知日期';
    // 尝试不同可能的日期字段名
    const dateField = comp.updateTime || comp.captureTime || comp.publishTime;
    if (dateField) {
        const parsedDate = new Date(dateField);
        // 检查 Date 对象是否有效 (非 Invalid Date)
        if (!isNaN(parsedDate.getTime())) {
            date = format(parsedDate, 'yyyy-MM-dd');
        }
    }

    return {
      id: comp.id,
      title: comp.title || '未知标题',
      company: comp.bankName || comp.company || comp.competitor || '未知银行',
      type: comp.updateType || comp.type || '未知类型',
      date: date,
      suggestion: suggestionText,
      link: `/competitor-detail/${comp.id}`
    } as Competitor;
  });
});
</script> 