<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 items-end">
      <div class="col-span-1 md:col-span-3 lg:col-span-2">
        <label for="searchKeyword" class="block text-sm font-medium text-gray-700 mb-1">关键词搜索</label>
        <input
          type="text"
          id="searchKeyword"
          v-model="filterData.keyword"
          @input="onKeywordInput"
          placeholder="输入动态标题、内容或竞品名称"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm"
        >
      </div>
      <div>
        <label for="competitorImportance" class="block text-sm font-medium text-gray-700 mb-1">重要程度</label>
        <select
          id="competitorImportance"
          v-model="filterData.importance"
          @change="emitFilter"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option
            v-for="option in importanceOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>
      <div>
        <label for="competitorRelevance" class="block text-sm font-medium text-gray-700 mb-1">相关度</label>
        <select
          id="competitorRelevance"
          v-model="filterData.relevance"
          @change="emitFilter"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option
            v-for="option in relevanceOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>
      <!-- 占位符让按钮右对齐 -->
      <div class="hidden lg:block"></div>
      <div class="hidden lg:block"></div>

      <div class="flex space-x-3 col-span-1 lg:col-span-2 justify-end">
        <button
          class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
          @click="resetFilter"
        >
          <i class="fas fa-undo mr-1"></i> 重置
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits, reactive } from 'vue';
import { competitorImportanceOptions as importanceOptions, competitorRelevanceOptions as relevanceOptions } from '@/utils/defaultData';

interface FilterOptions {
  keyword: string;
  importance: string;
  relevance: string;
}

const props = defineProps<{
  initialFilter?: FilterOptions;
}>();

const emit = defineEmits<{
  (e: 'filter', filters: FilterOptions): void;
}>();

// 初始化过滤条件
const filterData = reactive<FilterOptions>({
  keyword: props.initialFilter?.keyword || '',
  importance: props.initialFilter?.importance || '',
  relevance: props.initialFilter?.relevance || ''
});

// 防抖定时器
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

// 关键词输入处理（带防抖）
const onKeywordInput = () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  debounceTimer = setTimeout(() => {
    emit('filter', { ...filterData });
  }, 500); // 500ms 防抖
};

// 触发筛选
const emitFilter = () => {
  emit('filter', { ...filterData });
};

// 重置过滤条件
const resetFilter = () => {
  filterData.keyword = '';
  filterData.importance = '';
  filterData.relevance = '';
  emit('filter', { ...filterData });
};
</script>

<style scoped>
/* 自定义下拉箭头 */
select {
  appearance: none;
  background-image: url('data:image/svg+xml;charset=UTF-8,%3csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="%236b7280" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"%3e%3cpath d="M6 8l4 4 4-4"/%3e%3c/svg%3e');
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 2.5rem;
}
</style>
