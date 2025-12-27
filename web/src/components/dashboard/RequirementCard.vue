<template>
  <Card title="待处理需求概览" icon="tasks" moreLink="/requirement-pool">
    <div class="space-y-4">
      <div v-for="requirement in mappedRequirements" :key="requirement.id" class="flex items-start justify-between" :class="{ 'border-b pb-3': requirement.id !== mappedRequirements.length }">
        <div>
          <router-link :to="`/requirement-detail/${requirement.id}`" class="hover:text-primary cursor-pointer">
            <p class="text-sm font-medium text-gray-700 mb-1">{{ requirement.title }}</p>
          </router-link>
          <p class="text-xs text-gray-500">来源：{{ requirement.source }} | 优先级：{{ requirement.priority }}</p>
        </div>
        <span :class="`text-xs text-${requirement.status} font-medium ml-4 flex-shrink-0`">{{ requirement.statusText }}</span>
      </div>
    </div>
  </Card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import Card from '../common/Card.vue';
import type { RequirementVO } from '../../types/requirement';

interface Requirement {
  id: number | string;
  title: string;
  source: string;
  priority: string;
  status: string;
  statusText: string;
  link: string;
}

const props = defineProps<{
  requirements: RequirementVO[];
}>();

// 将API返回的数据格式转换为组件所需格式
const mappedRequirements = computed(() => {
  return props.requirements.map(req => {
    // 处理状态文本映射
    let statusText = '未知';
    let statusClass = 'gray-500';

    switch (req.status) {
      case 'NEW':
        statusText = '待处理';
        statusClass = 'red-500';
        break;
      case 'PROCESSING':
        statusText = '进行中';
        statusClass = 'blue-500';
        break;
      case 'COMPLETED':
        statusText = '已完成';
        statusClass = 'green-500';
        break;
      case 'REJECTED':
        statusText = '已拒绝';
        statusClass = 'gray-500';
        break;
    }

    // 处理来源映射
    let source = '未知';
    switch (req.sourceType) {
      case 'POLICY':
        source = '政策转化';
        break;
      case 'COMPETITOR':
        source = '竞品转化';
        break;
      case 'MANUAL':
        source = '手动创建';
        break;
    }

    // 处理优先级映射
    let priority = '未知';
    priority = req.priority || '未知';

    return {
      id: req.id,
      title: req.title,
      source: source,
      priority: priority,
      status: statusClass,
      statusText: statusText,
      link: `/requirement-detail/${req.id}`
    } as Requirement;
  });
});
</script> 