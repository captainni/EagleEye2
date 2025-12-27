<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 items-end">
      <!-- 关键词搜索 -->
      <div class="col-span-1 md:col-span-3 lg:col-span-2">
        <label for="searchKeyword" class="block text-sm font-medium text-gray-700 mb-1">关键词</label>
        <input 
          type="text" 
          id="searchKeyword" 
          v-model="filters.keyword"
          placeholder="搜索需求标题或描述" 
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm"
        >
      </div>
      
      <!-- 状态筛选 -->
      <div>
        <label for="reqStatus" class="block text-sm font-medium text-gray-700 mb-1">状态</label>
        <select 
          id="reqStatus"
          v-model="filters.status"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <!-- 优先级筛选 -->
      <div>
        <label for="reqPriority" class="block text-sm font-medium text-gray-700 mb-1">优先级</label>
        <select 
          id="reqPriority"
          v-model="filters.priority"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in priorityOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <!-- 来源筛选 -->
      <div>
        <label for="reqSource" class="block text-sm font-medium text-gray-700 mb-1">来源</label>
        <select 
          id="reqSource"
          v-model="filters.source"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
        >
          <option v-for="option in sourceOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <!-- 按钮行 -->
      <div class="col-span-full mt-4 flex justify-between items-center">
        <!-- 左对齐按钮 -->
        <router-link 
          to="/requirement-add" 
          class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm whitespace-nowrap"
        >
          <i class="fas fa-plus mr-1"></i> 新增需求
        </router-link>

        <!-- 右对齐按钮组 -->
        <div class="flex space-x-3">
          <button 
            @click="handleReset"
            class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
          >
            <i class="fas fa-undo mr-1"></i> 重置
          </button>
          <button 
            @click="handleSearch"
            class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm whitespace-nowrap"
          >
            <i class="fas fa-search mr-1"></i> 查询
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { requirementStatusOptions as statusOptions, requirementPriorityOptions as priorityOptions, requirementSourceOptions as sourceOptions } from '@/utils/defaultData';

// 定义事件
const emit = defineEmits(['search', 'reset']);

// 筛选条件
const filters = ref({
  keyword: '',
  status: '',
  priority: '',
  source: ''
});

// 处理搜索
const handleSearch = () => {
  emit('search', { ...filters.value });
};

// 处理重置
const handleReset = () => {
  filters.value = {
    keyword: '',
    status: '',
    priority: '',
    source: ''
  };
  emit('reset');
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