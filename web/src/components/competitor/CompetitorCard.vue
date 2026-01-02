<template>
  <div class="bg-white rounded-lg shadow-sm p-5 card-hover flex flex-col relative">
    <h4 class="text-base font-semibold text-gray-800 mb-2 hover:text-primary cursor-pointer pr-16 line-clamp-2" @click="goToDetail">
      {{ competitor.title }}
    </h4>
    <div class="flex items-center text-xs text-gray-500 mb-3 space-x-3">
      <span>{{ competitor.company || competitorName }}</span>
      <span>{{ competitor.type || '未知' }}</span>
      <span>{{ formatDate }}</span>
    </div>

    <!-- 重要性和相关度标签 -->
    <div class="flex flex-wrap gap-1.5 mb-3">
      <span v-if="competitor.importance" class="inline-flex items-center px-2 py-0.5 text-xs font-medium rounded-full"
        :class="getImportanceClass(competitor.importance)">
        重要性: {{ competitor.importance }}
      </span>
      <span v-if="competitor.relevance" class="inline-flex items-center px-2 py-0.5 text-xs font-medium rounded-full"
        :class="getRelevanceClass(competitor.relevance)">
        相关度: {{ competitor.relevance }}
      </span>
    </div>

    <!-- 标签显示 -->
    <div class="flex flex-wrap gap-1.5 mb-3">
      <!-- 处理标签显示 -->
      <template v-if="Array.isArray(competitor.tags)">
        <!-- 对象数组格式的标签 -->
        <template v-if="competitor.tags.length > 0 && typeof competitor.tags[0] === 'object'">
          <span
            v-for="(tag, index) in displayTags"
            :key="index"
            class="px-2 py-0.5 text-xs font-medium rounded-full"
            :class="`bg-${tag.color}-100 text-${tag.color}-600`"
          >
            {{ tag.label }}
          </span>
          <span v-if="hasMoreTags" class="px-2 py-0.5 text-xs font-medium rounded-full bg-gray-100 text-gray-500">
            +{{ remainingTagsCount }}
          </span>
        </template>

        <!-- 字符串数组格式的标签 -->
        <template v-else>
          <span
            v-for="(tag, index) in displayTags"
            :key="index"
            class="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-100 text-blue-600"
          >
            {{ tag }}
          </span>
          <span v-if="hasMoreTags" class="px-2 py-0.5 text-xs font-medium rounded-full bg-gray-100 text-gray-500">
            +{{ remainingTagsCount }}
          </span>
        </template>
      </template>
    </div>
    <p class="text-sm text-gray-600 mb-4 flex-grow line-clamp-3 leading-relaxed">
      {{ cleanMarkdown(competitor.suggestion || competitor.summary || '暂无建议') }}
    </p>
    <div class="flex justify-end items-center space-x-2 mt-auto pt-4 border-t border-gray-100">
      <router-link
        :to="{ name: 'CompetitorDetail', params: { id: competitor.id } }"
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
  importance?: string;
  relevance?: string;
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

// 显示的标签（最多3个）
const displayTags = computed(() => {
  if (!Array.isArray(props.competitor.tags)) return [];
  return props.competitor.tags.slice(0, 3);
});

// 是否有更多标签
const hasMoreTags = computed(() => {
  return Array.isArray(props.competitor.tags) && props.competitor.tags.length > 3;
});

// 剩余标签数量
const remainingTagsCount = computed(() => {
  if (!Array.isArray(props.competitor.tags)) return 0;
  return props.competitor.tags.length - 3;
});

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

// 清理 Markdown 符号
function cleanMarkdown(text: string): string {
  if (!text) return '';
  return text
    .replace(/^#{1,6}\s+/gm, '') // 移除标题标记
    .replace(/\*\*/g, '')        // 移除加粗标记
    .replace(/\*/g, '')           // 移除斜体标记
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1') // 移除链接格式，保留文本
    .replace(/`/g, '')            // 移除代码标记
    .replace(/^-\s+/gm, '')       // 移除列表标记
    .replace(/^\d+\.\s+/gm, '')   // 移除有序列表标记
    .replace(/\n+/g, ' ')         // 将换行符替换为空格
    .trim();
}

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