<template>
  <div class="bg-white rounded-lg shadow-sm p-8">
    <!-- 标题和元数据 -->
    <div class="mb-6 pb-4 border-b border-gray-200">
      <h2 class="text-2xl font-semibold text-gray-800 mb-3">{{ policy.title }}</h2>
      <div class="flex items-center text-sm text-gray-500 space-x-4">
        <span><i class="fas fa-landmark mr-1"></i> 来源：{{ policy.source || '未知' }}</span>
        <span><i class="fas fa-calendar-alt mr-1"></i> 发布日期：{{ policy.publishTime || '未知' }}</span>
        <!-- 显示重要性 -->
        <span
          v-if="policy.importance"
          class="px-2 py-0.5 text-xs font-medium rounded-full"
          :class="importanceClass"
        >
          {{ importanceLabel }}
        </span>
        <!-- 显示相关度（新增） -->
        <span
          v-if="policy.relevance && policy.relevance !== 'undefined'"
          class="px-2 py-0.5 text-xs font-medium rounded-full"
          :class="relevanceClass"
        >
          相关度: {{ policy.relevance }}
        </span>
        <!-- 显示领域/标签 -->
        <template v-if="policy.areas && policy.areas.length">
          <span 
            v-for="(area, index) in policy.areas" 
            :key="index" 
            class="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-100 text-blue-600"
          >
            {{ area }}
          </span>
        </template>
         <template v-else-if="policy.tags && policy.tags.length">
          <span 
            v-for="(tag, index) in policy.tags" 
            :key="index" 
            class="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-100 text-blue-600"
          >
            {{ tag }}
          </span>
        </template>
      </div>
    </div>

    <!-- 核心建议 -->
    <div v-if="policy.suggestions && policy.suggestions.length" class="mb-8 p-4 bg-blue-50 border-l-4 border-primary rounded-r-md">
      <h4 class="text-base font-semibold text-primary mb-2">核心建议点：</h4>
      <ul class="list-disc list-inside space-y-1 text-sm text-gray-700">
        <li v-for="(suggestion, index) in policy.suggestions" :key="suggestion.id || index">
          {{ suggestion.suggestion }}
          <p v-if="suggestion.reason" class="text-xs text-gray-500 pl-4">理由：{{ suggestion.reason }}</p>
        </li>
      </ul>
    </div>
    
    <!-- 摘要 -->
     <div v-if="policy.summary" class="mb-8 p-4 bg-gray-50 border-l-4 border-gray-400 rounded-r-md">
      <h4 class="text-base font-semibold text-gray-600 mb-2">摘要：</h4>
      <p class="text-sm text-gray-700">{{ policy.summary }}</p>
    </div>
    
    <!-- 影响分析 -->
    <div v-if="policy.impactAnalysis" class="mb-8 p-4 bg-orange-50 border-l-4 border-warning rounded-r-md">
      <h4 class="text-base font-semibold text-warning mb-2">影响分析：</h4>
      <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed" v-html="policy.impactAnalysis"></div>
    </div>

    <!-- 原文内容 -->
    <div v-if="policy.content" class="mb-6">
      <h4 class="text-base font-semibold text-gray-700 mb-3">政策原文：</h4>
      <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed border border-gray-200 rounded-md p-4 max-h-96 overflow-y-auto" v-html="highlightedContent"></div>
      <p v-if="policy.sourceUrl" class="mt-2 text-xs">
        <a :href="policy.sourceUrl" target="_blank" class="text-primary hover:underline">查看原文链接</a>
      </p>
    </div>

    <!-- 操作按钮 -->
    <div class="flex justify-end space-x-3 mt-8 pt-4 border-t border-gray-200">
      <router-link 
        :to="{ name: 'PolicyMonitoring' }" 
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
import { defineProps, computed } from 'vue';
import { PolicyVO, SuggestionVO } from '@/api/policy';
import MarkdownIt from 'markdown-it';
import { convertPolicyToRequirement } from '@/services/requirementService';
import { ElMessage } from 'element-plus';

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动转换 URL 为链接
  typographer: true  // 启用一些语言中立的替换和引号美化
});

const props = defineProps<{
  policy: PolicyVO;
}>();

// 重要性Label计算
const importanceLabel = computed(() => {
  const importance = props.policy.importance;
  if (!importance) return '未知';
  let value: string;
  if (typeof importance === 'object' && importance !== null) {
    value = String((importance as { label?: string }).label || importance);
  } else {
    value = String(importance);
  }
  return `重要性: ${value}`;
});

// 重要性样式计算 - 兼容字符串和对象
const importanceClass = computed(() => {
  const importance = props.policy.importance;
  if (!importance) return 'bg-gray-100 text-gray-600';
  
  let value: string | number;
  let color: string | undefined = undefined;

  if (typeof importance === 'object' && importance !== null) {
    const impObj = importance as { label?: string; color?: string };
    value = String(impObj.label || importance).toLowerCase();
    if (impObj.color) {
       color = impObj.color;
    }
  } else {
    value = typeof importance === 'number' ? importance : String(importance).toLowerCase();
  }

  // 优先使用对象中的颜色
  if (color) {
     return `bg-${color}-100 text-${color}-600`;
  }
  
  // 否则根据值判断
  if (value === '高' || value === 'high' || value === 3) {
    return 'bg-red-100 text-red-600';
  } else if (value === '中' || value === 'medium' || value === 2) {
    return 'bg-yellow-100 text-yellow-600';
  } else if (value === '低' || value === 'low' || value === 1) {
    return 'bg-green-100 text-green-600';
  } else {
    return 'bg-gray-100 text-gray-600';
  }
});

// 相关度样式（新增）
const relevanceClass = computed(() => {
  const relevance = props.policy.relevance;
  if (!relevance) return 'bg-gray-100 text-gray-500';

  const value = String(relevance).toLowerCase();

  if (value === '高' || value === 'high') {
    return 'bg-orange-100 text-orange-600';
  } else if (value === '中' || value === 'medium') {
    return 'bg-blue-100 text-blue-600';
  } else if (value === '低' || value === 'low') {
    return 'bg-gray-100 text-gray-500';
  } else {
    return 'bg-gray-100 text-gray-500';
  }
});

// 新增：计算高亮后的原文内容（支持 Markdown 渲染）
const highlightedContent = computed(() => {
  console.log('计算 highlightedContent...');
  console.log('原始 content:', props.policy.content);
  console.log('原始 keyPoints:', props.policy.keyPoints);
  console.log('keyPoints 类型:', typeof props.policy.keyPoints);

  const originalContent = props.policy.content || '';
  const keyPoints = props.policy.keyPoints;

  // 1. 先将 Markdown 转换为 HTML
  let htmlContent = md.render(originalContent);

  // 2. 检查 keyPoints 是否是有效数组且有内容
  if (Array.isArray(keyPoints) && keyPoints.length > 0 && htmlContent) {
    // 对每个关键点进行高亮处理
    keyPoints.forEach(point => {
      if (point) { // 确保关键点不是空字符串
        // 转义正则表达式特殊字符
        const escapedPoint = point.replace(/[*+?^${}()|[\]\\]/g, '\\$&');
        // 创建正则表达式，进行全局、不区分大小写替换
        const regex = new RegExp(escapedPoint, 'gi');
        // 使用 <mark> 标签包裹匹配到的文本
        htmlContent = htmlContent.replace(regex, (match) => `<mark>${match}</mark>`);
      }
    });
  }

  console.log('最终 HTML 内容:', htmlContent);
  return htmlContent;
});

const convertToRequirement = async () => {
  if (!props.policy.id) {
    ElMessage.error('无法获取政策ID');
    return;
  }
  try {
    await convertPolicyToRequirement(props.policy.id);
    ElMessage.success('需求已成功转化！');
  } catch (error: any) {
    console.error('Error converting policy to requirement:', error);
    ElMessage.error(error.response?.data?.message || '需求转化失败，请稍后重试');
  }
};
</script>

<style>
mark {
  background-color: #fef08a; /* Yellow-200 */
  padding: 0.1em 0.2em;
  border-radius: 3px;
}
</style> 