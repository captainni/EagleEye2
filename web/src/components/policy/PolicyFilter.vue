<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 items-end">
      <div class="col-span-1 md:col-span-3 lg:col-span-2">
        <label for="searchKeyword" class="block text-sm font-medium text-gray-700 mb-1">关键词搜索</label>
        <input 
          type="text" 
          id="searchKeyword" 
          v-model="filterData.keyword"
          placeholder="输入政策标题、文号或关键词" 
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm"
        >
      </div>
      <div>
        <label for="policySource" class="block text-sm font-medium text-gray-700 mb-1">政策来源</label>
        <select 
          id="policySource" 
          v-model="filterData.source"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option 
            v-for="option in sourceOptions" 
            :key="option.value" 
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>
      <div>
        <label for="policyCategory" class="block text-sm font-medium text-gray-700 mb-1">政策分类</label>
        <select 
          id="policyCategory" 
          v-model="filterData.category"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option 
            v-for="option in categoryOptions" 
            :key="option.value" 
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>
      <div>
        <label for="policyImportance" class="block text-sm font-medium text-gray-700 mb-1">重要性</label>
        <select 
          id="policyImportance" 
          v-model="filterData.importance"
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
      <!-- 占位符让按钮右对齐 -->
      <div class="hidden lg:block"></div>
      <div class="hidden lg:block"></div>
      <div class="hidden lg:block"></div>

      <div class="flex space-x-3 col-span-1 lg:col-span-2 justify-end">
        <button 
          class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
          @click="resetFilter"
        >
          <i class="fas fa-undo mr-1"></i> 重置
        </button>
        <button 
          class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm whitespace-nowrap"
          @click="applyFilter"
        >
          <i class="fas fa-search mr-1"></i> 查询
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits, ref, reactive, watch } from 'vue';
import { policySourceOptions as sourceOptions, policyCategoryOptions as categoryOptions, policyImportanceOptions as importanceOptions } from '@/utils/defaultData';

interface FilterOptions {
  keyword: string;
  source: string;
  category: string;
  importance: string;
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
  source: props.initialFilter?.source || '',
  category: props.initialFilter?.category || '',
  importance: props.initialFilter?.importance || ''
});

// 重置过滤条件
const resetFilter = () => {
  filterData.keyword = '';
  filterData.source = '';
  filterData.category = '';
  filterData.importance = '';
  emit('filter', { ...filterData });
};

// 应用过滤条件
const applyFilter = () => {
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