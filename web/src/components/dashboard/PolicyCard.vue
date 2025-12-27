<template>
  <Card title="最新政策提醒" icon="bullhorn" moreLink="/policy-monitoring">
    <div class="space-y-4">
      <div v-for="policy in mappedPolicies" :key="policy.id" class="border-b pb-3" :class="{ 'border-b-0': policy.id === mappedPolicies.length }">
        <router-link :to="`/policy-detail/${policy.id}`" class="hover:text-primary cursor-pointer">
          <p class="text-sm font-medium text-gray-700 mb-1">{{ policy.title }}</p>
        </router-link>
        <p class="text-xs text-gray-500 mb-1">来源：{{ policy.source }} | 时间：{{ policy.date }}</p>
        <p class="text-xs text-secondary">建议：{{ policy.suggestion }}</p>
      </div>
    </div>
  </Card>
</template>

<script lang="ts" setup>
import { computed, onMounted, watch } from 'vue';
import Card from '../common/Card.vue';
import type { PolicyVO } from '../../types/policy';
import type { SuggestionVO } from '../../types/suggestion';
import { format } from 'date-fns';

interface Policy {
  id: number | string;
  title: string;
  source?: string;
  date: string;
  suggestion: string;
  link: string;
}

const props = defineProps<{
  policies: PolicyVO[];
}>();

// 调试用：监控policies
watch(() => props.policies, (newVal) => {
  console.log('PolicyCard received policies:', newVal);
  console.log('PolicyCard policies count:', newVal.length);
}, { immediate: true });

onMounted(() => {
  console.log('PolicyCard mounted, policies:', props.policies);
  console.log('PolicyCard policies count:', props.policies.length);
});

// 将API返回的数据格式转换为组件所需格式
const mappedPolicies = computed(() => {
  console.log('Mapping policies:', props.policies);
  console.log('Policies length:', props.policies.length);
  
  return props.policies.map(policy => {
    // 更安全地获取第一条建议
    const suggestionText = 
      policy.suggestions && 
      policy.suggestions.length > 0 && 
      policy.suggestions[0] && 
      (policy.suggestions[0] as SuggestionVO).suggestion
      ? (policy.suggestions[0] as SuggestionVO).suggestion
      : '暂无建议';

    // 更安全地格式化日期
    let date = '未知日期';
    if (policy.publishTime) {
      const parsedDate = new Date(policy.publishTime);
      // 检查 Date 对象是否有效 (非 Invalid Date)
      if (!isNaN(parsedDate.getTime())) { 
        date = format(parsedDate, 'yyyy-MM-dd');
      }
    }

    return {
      id: policy.id,
      title: policy.title,
      source: policy.source || '未知来源',
      date: date,
      suggestion: suggestionText,
      link: `/policy-detail/${policy.id}`
    } as Policy;
  });
});
</script>