<template>
  <div>
    <!-- 加载状态 -->
    <div v-if="loading" class="col-span-1 lg:col-span-2 text-center py-12">
      <i class="fas fa-spinner fa-spin text-gray-300 text-4xl mb-4"></i>
      <p class="text-gray-500">加载中...</p>
    </div>
    
    <!-- 竞品列表 -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <competitor-card 
        v-for="competitor in competitors" 
        :key="competitor.id" 
        :competitor="competitor" 
      />
      
      <!-- 无数据提示 -->
      <div v-if="competitors.length === 0" class="col-span-1 lg:col-span-2 text-center py-12">
        <i class="fas fa-search text-gray-300 text-4xl mb-4"></i>
        <p class="text-gray-500">暂无符合条件的竞品动态</p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';
import CompetitorCard from './CompetitorCard.vue';

// 定义通用竞品动态接口，兼容API返回的数据格式
interface CompetitorItem {
  id: string | number;
  title: string;
  company?: string;
  competitor?: string;
  type?: string;
  publishDate?: string;
  publishTime?: string;
  tags?: string[];
  importance?: any;
  suggestion?: string;
  summary?: string;
  [key: string]: any;
}

defineProps({
  competitors: {
    type: Array as () => CompetitorItem[],
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});
</script> 