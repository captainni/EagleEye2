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
    // 更安全地获取建议信息
    let suggestionText = '暂无建议';
    
    // 优先检查analysisAndSuggestions数组 (竞品详情页使用的格式)
    if (comp.analysisAndSuggestions && Array.isArray(comp.analysisAndSuggestions) && comp.analysisAndSuggestions.length > 0) {
      // 获取以"建议："开头的条目，如果没有则取第一条
      const suggestionEntry = comp.analysisAndSuggestions.find(item => 
        typeof item === 'string' && item.includes('建议：')
      ) || comp.analysisAndSuggestions[0];
      
      if (suggestionEntry && typeof suggestionEntry === 'string') {
        // 从条目中提取建议部分
        let text = suggestionEntry;
        if (text.includes('建议：')) {
          text = text.split('建议：')[1];
        }
        // 提取纯文本 (移除可能的HTML标记)
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = text;
        suggestionText = tempDiv.textContent || tempDiv.innerText || text;
      }
    }
    // 如果analysisAndSuggestions不存在，则检查suggestion直接属性
    else if (comp.suggestion && typeof comp.suggestion === 'string') {
      suggestionText = comp.suggestion;
    }
    // 最后检查suggestions数组 (如果前两种方式都没找到)
    else if (comp.suggestions && Array.isArray(comp.suggestions) && comp.suggestions.length > 0) {
      const firstSuggestion = comp.suggestions[0];
      if (typeof firstSuggestion === 'string') {
        suggestionText = firstSuggestion;
      } else if (firstSuggestion && typeof firstSuggestion === 'object') {
        if ('suggestion' in firstSuggestion && typeof firstSuggestion.suggestion === 'string') {
          suggestionText = firstSuggestion.suggestion;
        } else if ('content' in firstSuggestion && typeof firstSuggestion.content === 'string') {
          suggestionText = firstSuggestion.content;
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