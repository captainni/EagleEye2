<template>
  <div class="mb-6">
    <div class="bg-white border-l-4 border-amber-500 shadow-sm rounded-lg p-4 flex items-center space-x-3 card-hover">
      <div class="bg-amber-100 p-2 rounded-full">
        <i class="fas fa-triangle-exclamation text-amber-600 text-lg flex-shrink-0"></i>
      </div>
      <div>
        <div class="flex items-center mb-1">
          <span class="bg-amber-100 text-amber-700 text-[10px] font-medium px-2 py-0.5 rounded mr-2">紧急预警</span>
          <h3 class="text-sm font-semibold text-gray-800">{{ alert.title }}</h3>
        </div>
        <p class="text-xs text-gray-600 mb-1">重要性：<span class="text-amber-700 font-medium">{{ alert.importance || '高' }}</span> | 影响范围：{{ alert.impactArea || '暂无' }} | 建议：{{ alert.suggestion || '查看详情评估影响' }}</p>
        <router-link :to="alert.link || '#'" class="text-xs text-amber-700 hover:text-amber-900 font-medium mt-1 inline-block cursor-pointer hover:underline">查看详情 &raquo;</router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';
import type { Alert } from '../../types/common';

const props = defineProps<{
  alert: Alert;
}>();
</script>

<style scoped>
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.06), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}
.card-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
</style> 