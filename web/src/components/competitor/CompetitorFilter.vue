<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 items-end">
      <div class="col-span-1 md:col-span-3 lg:col-span-2">
        <label for="searchKeyword" class="block text-sm font-medium text-gray-700 mb-1">关键词搜索</label>
        <input 
          type="text" 
          id="searchKeyword" 
          v-model="keyword"
          placeholder="输入动态标题、内容或竞品名称" 
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm"
          @input="handleFilter"
        >
      </div>
      <div>
        <label for="competitorName" class="block text-sm font-medium text-gray-700 mb-1">竞品机构</label>
        <select 
          id="competitorName" 
          v-model="selectedCompany"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
          @change="handleFilter"
        >
          <option v-for="(option, index) in competitorOptions" :key="index" :value="option">
            {{ option }}
          </option>
        </select>
      </div>
      <div>
        <label for="dynamicType" class="block text-sm font-medium text-gray-700 mb-1">动态类型</label>
        <select 
          id="dynamicType" 
          v-model="selectedType"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
          @change="handleFilter"
        >
          <option v-for="(option, index) in typeOptions" :key="index" :value="option">
            {{ option }}
          </option>
        </select>
      </div>
      <div>
        <label for="timeRange" class="block text-sm font-medium text-gray-700 mb-1">时间范围</label>
        <select 
          id="timeRange" 
          v-model="selectedTime"
          class="w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm bg-white"
          @change="handleFilter"
        >
          <option v-for="(option, index) in timeOptions" :key="index" :value="option">
            {{ option }}
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
          @click="handleFilter"
        >
          <i class="fas fa-search mr-1"></i> 查询
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, defineProps, defineEmits } from 'vue';

interface FilterProps {
  competitorOptions: string[];
  typeOptions: string[];
  timeOptions: string[];
}

const props = defineProps<FilterProps>();
const emit = defineEmits(['filter']);

// 筛选条件
const keyword = ref('');
const selectedCompany = ref(props.competitorOptions[0]);
const selectedType = ref(props.typeOptions[0]);
const selectedTime = ref(props.timeOptions[0]);

// 筛选处理
const handleFilter = () => {
  emit('filter', {
    keyword: keyword.value,
    company: selectedCompany.value,
    type: selectedType.value,
    time: selectedTime.value
  });
};

// 重置筛选
const resetFilter = () => {
  keyword.value = '';
  selectedCompany.value = props.competitorOptions[0];
  selectedType.value = props.typeOptions[0];
  selectedTime.value = props.timeOptions[0];
  
  handleFilter();
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