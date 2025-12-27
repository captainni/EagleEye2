<template>
  <div>
    <!-- 加载状态 -->
    <div v-if="loading" class="col-span-1 lg:col-span-2 text-center py-12">
      <i class="fas fa-spinner fa-spin text-gray-300 text-4xl mb-4"></i>
      <p class="text-gray-500">加载中...</p>
    </div>
    
    <!-- 政策列表 -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <policy-card 
        v-for="policy in filteredPolicies" 
        :key="policy.id" 
        :policy="policy"
      />
      
      <div v-if="filteredPolicies.length === 0" class="col-span-1 lg:col-span-2 text-center py-12">
        <i class="fas fa-search text-gray-300 text-4xl mb-4"></i>
        <p class="text-gray-500">暂无符合条件的政策</p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, computed } from 'vue';
import PolicyCard from './PolicyCard.vue';

interface CategoryType {
  label: string;
  color: string;
}

interface ImportanceType {
  label: string;
  color: string;
}

interface PolicyType {
  id: string | number;
  title: string;
  source: string;
  publishDate?: string;
  issueTime?: string;
  categories?: CategoryType[];
  tags?: string[];
  importance?: ImportanceType | number;
  suggestion?: string;
  [key: string]: any;
}

interface FilterOptions {
  keyword: string;
  source: string;
  category: string;
  importance: string;
  [key: string]: any;
}

const props = defineProps({
  policies: {
    type: Array as () => PolicyType[],
    required: true
  },
  filters: {
    type: Object as () => FilterOptions,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

// 在前端页面进行本地过滤（在API对接完成前）
const filteredPolicies = computed(() => {
  // API已经处理了过滤，这里可以直接返回
  return props.policies;
});
</script> 