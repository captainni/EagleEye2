<template>
  <div class="bg-white rounded-lg shadow-sm p-8">
    <!-- 标题和元数据 -->
    <div class="mb-6 pb-4 border-b border-gray-200">
      <h2 class="text-2xl font-semibold text-gray-800 mb-3">{{ competitor.title }}</h2>
      <div class="flex items-center text-sm text-gray-500 space-x-4 mb-3">
        <span><i class="fas fa-building mr-1"></i> 竞品：{{ competitor.company }}</span>
        <span><i class="fas fa-tags mr-1"></i> 类型：{{ competitor.type }}</span>
        <span><i class="fas fa-clock mr-1"></i> 捕获时间：{{ competitor.captureTime }}</span>
      </div>
      <div class="flex flex-wrap gap-2">
        <span 
          v-for="(tag, index) in competitor.tags" 
          :key="index" 
          class="px-2 py-0.5 text-xs font-medium rounded-full"
          :class="`bg-${tag.color}-100 text-${tag.color}-600`"
        >
          {{ tag.label }}
        </span>
      </div>
    </div>

    <!-- 分析摘要与建议 -->
    <div class="mb-8 p-4 bg-orange-50 border-l-4 border-warning rounded-r-md">
      <h4 class="text-base font-semibold text-warning mb-2">分析摘要与建议：</h4>
      <ul class="list-disc list-inside space-y-1 text-sm text-gray-700">
        <li v-for="(suggestion, index) in competitor.analysisAndSuggestions" :key="index" v-html="suggestion"></li>
      </ul>
    </div>

    <!-- 相关信息/原文链接 -->
    <div class="mb-6">
      <h4 class="text-base font-semibold text-gray-700 mb-3">相关信息/原文链接：</h4>
      <div 
        class="prose prose-sm max-w-none text-gray-600 leading-relaxed border border-gray-200 rounded-md p-4"
        v-html="competitor.relatedInfo"
      ></div>
      <div class="flex space-x-4 mt-2">
        <img 
          v-for="(screenshot, index) in competitor.screenshots" 
          :key="index" 
          :src="screenshot" 
          :alt="`截图 ${index + 1}`" 
          class="rounded border border-gray-300"
        >
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="flex justify-end space-x-3 mt-8 pt-4 border-t border-gray-200">
      <router-link 
        :to="{ name: 'CompetitorTracking' }" 
        class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
      >
        <i class="fas fa-arrow-left mr-1"></i> 返回列表
      </router-link>
      <button 
        class="!rounded-button px-4 py-2 bg-primary text-white text-sm whitespace-nowrap hover:bg-primary/90"
        @click="convertToRequirement"
      >
        <i class="fas fa-exchange-alt mr-1"></i> 转为需求
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';
// import { CompetitorDetail } from '../../mock/competitor/competitorDetail'; // 不再需要 Mock 类型
import { CompetitorVO } from '@/api/competitor'; // 导入更新后的 API VO 类型

const props = defineProps<{
  competitor: CompetitorVO; // 使用 API VO 类型
}>();

const convertToRequirement = () => {
  // 这里应该是转为需求的逻辑，暂时使用alert模拟
  alert('需求已成功转化！');
};
</script>

<style>
mark {
  background-color: #fef08a; /* Yellow-200 */
  padding: 0.1em 0.2em;
  border-radius: 3px;
}
</style> 