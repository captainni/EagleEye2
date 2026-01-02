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

      <!-- 重要性和相关度标签 -->
      <div class="flex flex-wrap gap-2 mb-3">
        <span v-if="competitor.importance" class="px-2 py-0.5 text-xs font-medium rounded-full"
          :class="getImportanceClass(competitor.importance)">
          重要性: {{ competitor.importance }}
        </span>
        <span v-if="competitor.relevance" class="px-2 py-0.5 text-xs font-medium rounded-full"
          :class="getRelevanceClass(competitor.relevance)">
          相关度: {{ competitor.relevance }}
        </span>
      </div>

      <!-- 标签 -->
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

    <!-- 摘要区域 -->
    <div v-if="competitor.summary" class="mb-8 p-4 bg-gray-50 border-l-4 border-gray-400 rounded-r-md">
      <h4 class="text-base font-semibold text-gray-700 mb-2">摘要</h4>
      <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed" v-html="renderMarkdown(competitor.summary)"></div>
    </div>

    <!-- 关键要点 -->
    <div v-if="competitor.keyPoints && competitor.keyPoints.length > 0" class="mb-8 p-4 bg-blue-50 border-l-4 border-primary rounded-r-md">
      <h4 class="text-base font-semibold text-primary mb-2">关键要点</h4>
      <ul class="list-disc list-inside space-y-1 text-sm text-gray-700">
        <li v-for="(point, index) in competitor.keyPoints" :key="index">{{ point }}</li>
      </ul>
    </div>

    <!-- 市场影响分析 -->
    <div v-if="competitor.marketImpact" class="mb-8 p-4 bg-orange-50 border-l-4 border-warning rounded-r-md">
      <h4 class="text-base font-semibold text-warning mb-2">市场影响分析</h4>
      <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed" v-html="renderAndHighlight(competitor.marketImpact)"></div>
    </div>

    <!-- 竞争态势分析 -->
    <div v-if="competitor.competitiveAnalysis" class="mb-8 p-4 bg-orange-50 border-l-4 border-warning rounded-r-md">
      <h4 class="text-base font-semibold text-warning mb-2">竞争态势分析</h4>
      <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed" v-html="renderAndHighlight(competitor.competitiveAnalysis)"></div>
    </div>

    <!-- 分析摘要与建议 -->
    <div v-if="competitor.analysisAndSuggestions && competitor.analysisAndSuggestions.length > 0" class="mb-8 p-4 bg-orange-50 border-l-4 border-warning rounded-r-md">
      <h4 class="text-base font-semibold text-warning mb-2">分析摘要与建议：</h4>
      <ul class="list-disc list-inside space-y-2 text-sm text-gray-700">
        <li v-for="(suggestion, index) in competitor.analysisAndSuggestions" :key="index"
            v-html="renderMarkdown(suggestion)"
            class="leading-relaxed"></li>
      </ul>
    </div>

    <!-- 核心建议点 -->
    <div v-if="competitor.ourSuggestions && competitor.ourSuggestions.length > 0" class="mb-8 p-4 bg-blue-50 border-l-4 border-primary rounded-r-md">
      <h4 class="text-base font-semibold text-primary mb-2">核心建议点：</h4>
      <ul class="list-disc list-inside space-y-2 text-sm text-gray-700">
        <li v-for="(suggestion, index) in competitor.ourSuggestions" :key="suggestion.id || index">
          {{ suggestion.suggestion }}
          <p v-if="suggestion.reason" class="text-xs text-gray-500 pl-4 mt-1">理由：{{ suggestion.reason }}</p>
        </li>
      </ul>
    </div>

    <!-- 相关信息/原文链接（带关键词高亮） -->
    <div v-if="contentToDisplay" class="mb-6">
      <h4 class="text-base font-semibold text-gray-700 mb-3">详细信息：</h4>
      <div
        class="prose prose-sm max-w-none text-gray-600 leading-relaxed border border-gray-200 rounded-md p-4"
        v-html="renderAndHighlight(contentToDisplay)"
      ></div>

      <!-- 原文链接 -->
      <div v-if="competitor.sources && competitor.sources.length > 0" class="mt-3">
        <template v-for="(source, index) in competitor.sources" :key="index">
          <a :href="source.url" target="_blank" class="text-xs text-primary hover:underline inline-flex items-center mr-3">
            <i class="fas fa-external-link-alt mr-1"></i>
            {{ source.title || '查看原文链接' }}
          </a>
        </template>
      </div>

      <!-- 截图展示 -->
      <div v-if="competitor.screenshots && competitor.screenshots.length > 0" class="flex flex-wrap gap-4 mt-4">
        <img
          v-for="(screenshot, index) in competitor.screenshots"
          :key="index"
          :src="screenshot"
          :alt="`截图 ${index + 1}`"
          class="rounded border border-gray-300 max-w-md"
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
import { defineProps, computed } from 'vue';
import { useRouter } from 'vue-router';
import { convertCompetitorToRequirement } from '@/services/requirementService';
import { ElMessage } from 'element-plus';
import { CompetitorVO } from '@/api/competitor';
import MarkdownIt from 'markdown-it';

// 创建 Markdown 实例
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动转换 URL 为链接
  typographer: true  // 启用一些语言中立的替换和引号美化
});

const router = useRouter();
const props = defineProps<{
  competitor: CompetitorVO;
}>();

// 获取要显示的内容
const contentToDisplay = computed(() => {
  return props.competitor.content || props.competitor.relatedInfo || '';
});

// Markdown 渲染方法
function renderMarkdown(content: string): string {
  if (!content) return '';
  return md.render(content);
}

// 渲染并高亮关键词
function renderAndHighlight(content: string): string {
  if (!content) return '';

  // 先渲染 Markdown
  let htmlContent = md.render(content);

  // 如果有关键词，进行高亮
  if (props.competitor.keyPoints && props.competitor.keyPoints.length > 0) {
    props.competitor.keyPoints.forEach((point: string) => {
      if (point) {
        const escapedPoint = point.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const regex = new RegExp(escapedPoint, 'gi');
        htmlContent = htmlContent.replace(regex, (match: string) =>
          `<mark class="bg-yellow-200 px-0.5 rounded">${match}</mark>`
        );
      }
    });
  }

  return htmlContent;
}

// 获取重要性标签样式
function getImportanceClass(importance: string): string {
  const map: Record<string, string> = {
    '高': 'bg-red-100 text-red-600',
    '中': 'bg-yellow-100 text-yellow-600',
    '低': 'bg-green-100 text-green-600'
  };
  return map[importance] || 'bg-gray-100 text-gray-600';
}

// 获取相关度标签样式
function getRelevanceClass(relevance: string): string {
  const map: Record<string, string> = {
    '高': 'bg-orange-100 text-orange-600',
    '中': 'bg-blue-100 text-blue-600',
    '低': 'bg-gray-100 text-gray-500'
  };
  return map[relevance] || 'bg-gray-100 text-gray-600';
}

const convertToRequirement = async () => {
  if (!props.competitor.id) {
    ElMessage.error('无法获取竞品ID');
    return;
  }
  try {
    const competitorId = Number(props.competitor.id);
    if (isNaN(competitorId)) {
      ElMessage.error('无效的竞品ID');
      return;
    }
    await convertCompetitorToRequirement(competitorId);
    ElMessage.success('需求已成功转化！');
  } catch (error: any) {
    console.error('Error converting competitor to requirement:', error);
    ElMessage.error(error.response?.data?.message || '需求转化失败，请稍后重试');
  }
};
</script>

<style scoped>
.prose {
  color: #374151;
  line-height: 1.75;
}

.prose h1, .prose h2, .prose h3, .prose h4 {
  font-weight: 600;
  line-height: 1.25;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.prose h1 { font-size: 1.5em; }
.prose h2 { font-size: 1.25em; }
.prose h3 { font-size: 1.125em; }
.prose h4 { font-size: 1em; }

.prose p {
  margin-top: 0.75em;
  margin-bottom: 0.75em;
}

.prose ul, .prose ol {
  margin-top: 0.75em;
  margin-bottom: 0.75em;
  padding-left: 1.5em;
}

.prose li {
  margin-top: 0.25em;
  margin-bottom: 0.25em;
}

.prose strong {
  font-weight: 600;
  color: #1f2937;
}

.prose a {
  color: #2563eb;
  text-decoration: underline;
}

.prose a:hover {
  color: #1d4ed8;
}

.prose code {
  background-color: #f3f4f6;
  color: #ef4444;
  padding: 0.125em 0.25em;
  border-radius: 0.25rem;
  font-size: 0.875em;
}

.prose pre {
  background-color: #1f2937;
  color: #f9fafb;
  padding: 1em;
  border-radius: 0.375rem;
  overflow-x: auto;
}

.prose blockquote {
  border-left: 4px solid #e5e7eb;
  padding-left: 1em;
  color: #6b7280;
  font-style: italic;
}

mark {
  background-color: #fef08a; /* Yellow-200 */
  padding: 0.1em 0.2em;
  border-radius: 3px;
}
</style> 