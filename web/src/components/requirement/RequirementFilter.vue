<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <!-- 筛选条件行 -->
    <div class="flex items-end gap-4 mb-4">
      <!-- 关键词搜索 - 占一半 -->
      <div class="w-1/2">
        <label for="searchKeyword" class="block text-sm font-medium text-gray-700 mb-1">关键词</label>
        <input
          type="text"
          id="searchKeyword"
          v-model="filterData.keyword"
          @input="onKeywordInput"
          placeholder="搜索需求标题或描述"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm"
        >
      </div>

      <!-- 状态筛选 -->
      <div class="flex-1">
        <label for="reqStatus" class="block text-sm font-medium text-gray-700 mb-1">状态</label>
        <select
          id="reqStatus"
          v-model="filterData.status"
          @change="emitFilter"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <!-- 优先级筛选 -->
      <div class="flex-1">
        <label for="reqPriority" class="block text-sm font-medium text-gray-700 mb-1">优先级</label>
        <select
          id="reqPriority"
          v-model="filterData.priority"
          @change="emitFilter"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in priorityOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>

      <!-- 来源筛选 -->
      <div class="flex-1">
        <label for="reqSource" class="block text-sm font-medium text-gray-700 mb-1">来源</label>
        <select
          id="reqSource"
          v-model="filterData.sourceType"
          @change="emitFilter"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in sourceOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- 按钮行 -->
    <div class="flex items-center gap-3 justify-end">
      <!-- 重置按钮 -->
      <button
        @click="resetFilter"
        class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
      >
        <i class="fas fa-undo mr-1"></i> 重置
      </button>

      <!-- 新增需求按钮 -->
      <router-link
        to="/requirement-add"
        class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm whitespace-nowrap"
      >
        <i class="fas fa-plus mr-1"></i> 新增需求
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { requirementStatusOptions as statusOptions, requirementPriorityOptions as priorityOptions, requirementSourceOptions as sourceOptions } from '@/utils/defaultData';

interface FilterOptions {
  keyword: string;
  status: string;
  priority: string;
  sourceType: string;
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
  status: props.initialFilter?.status || '',
  priority: props.initialFilter?.priority || '',
  sourceType: props.initialFilter?.sourceType || ''
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
  filterData.status = '';
  filterData.priority = '';
  filterData.sourceType = '';
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
