<template>
  <div class="bg-white rounded-lg shadow-sm p-6 card-hover flex flex-col">
    <h4 class="text-base font-semibold text-gray-800 mb-2 hover:text-primary cursor-pointer" @click="goToDetail">
      {{ competitor.title }}
    </h4>
    <div class="flex items-center justify-between text-xs text-gray-500 mb-3">
      <div>
        <span class="font-medium text-gray-600">竞品：{{ competitorName }}</span>
        <span class="mx-2">|</span>
        <span>类型：{{ competitor.type || '未知' }}</span>
      </div>
      <span>捕获时间：{{ formatDate }}</span>
    </div>
    <div class="flex flex-wrap gap-2 mb-3">
      <!-- 处理标签显示 -->
      <template v-if="Array.isArray(competitor.tags)">
        <!-- 对象数组格式的标签 -->
        <template v-if="competitor.tags.length > 0 && typeof competitor.tags[0] === 'object'">
          <span 
            v-for="(tag, index) in competitor.tags" 
            :key="index" 
            class="px-2 py-0.5 text-xs font-medium rounded-full"
            :class="`bg-${tag.color}-100 text-${tag.color}-600`"
          >
            {{ tag.label }}
          </span>
        </template>
        
        <!-- 字符串数组格式的标签 -->
        <template v-else>
          <span 
            v-for="(tag, index) in competitor.tags" 
            :key="index" 
            class="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-100 text-blue-600"
          >
            {{ tag }}
          </span>
        </template>
      </template>
    </div>
    <p class="text-sm text-gray-600 mb-4 flex-grow">
      {{ competitor.suggestion || competitor.summary || '暂无建议' }}
    </p>
    <div class="flex justify-end space-x-3 mt-auto pt-4 border-t border-gray-100">
      <router-link 
        :to="{ name: 'CompetitorDetail', params: { id: competitor.id }}" 
        class="!rounded-button text-sm text-primary hover:underline whitespace-nowrap cursor-pointer"
      >
        查看详情
      </router-link>
      <button 
        class="!rounded-button px-3 py-1 bg-primary text-white text-sm whitespace-nowrap hover:bg-primary/90"
        @click="convertToRequirement"
      >
        转为需求
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, computed } from 'vue';
import { useRouter } from 'vue-router';
import { convertCompetitorToRequirement } from '@/services/requirementService';
import { ElMessage } from 'element-plus';

// 定义竞品动态接口
interface CompetitorItem {
  id: string | number;
  title: string;
  company?: string;
  competitor?: string;
  type?: string;
  publishDate?: string;
  publishTime?: string;
  captureTime?: string;
  tags?: any[];
  importance?: any;
  suggestion?: string;
  summary?: string;
  [key: string]: any;
}

const router = useRouter();

const props = defineProps<{
  competitor: CompetitorItem;
}>();

// 获取竞品名称
const competitorName = computed(() => {
  return props.competitor.company || props.competitor.competitor || '未知竞品';
});

// 格式化日期
const formatDate = computed(() => {
  return props.competitor.captureTime || 
         props.competitor.publishTime || 
         props.competitor.publishDate || 
         '未知';
});

const goToDetail = () => {
  router.push({ name: 'CompetitorDetail', params: { id: props.competitor.id } });
};

const convertToRequirement = async () => {
  if (!props.competitor.id) {
    ElMessage.error('无法获取竞品ID');
    return;
  }
  try {
    // Call the API service function - ensure ID is a number
    const competitorId = Number(props.competitor.id);
    if (isNaN(competitorId)) {
        ElMessage.error('无效的竞品ID');
        return;
    }
    await convertCompetitorToRequirement(competitorId);
    // Assuming a successful call means conversion is done
    ElMessage.success('需求已成功转化！'); 
    // Optionally, you might want to redirect or refresh data here
  } catch (error: any) {
    console.error('Error converting competitor to requirement:', error);
    // Display error message from backend if available, otherwise a generic message
    ElMessage.error(error.response?.data?.message || '需求转化失败，请稍后重试');
  }
};
</script>

<style scoped>
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}
.card-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
</style> 