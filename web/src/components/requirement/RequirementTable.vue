<template>
  <div class="bg-white rounded-lg shadow-sm">
    <table class="w-full table-fixed">
      <thead>
        <tr class="bg-gray-50 border-b border-gray-200">
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-10">
            <input 
              type="checkbox" 
              class="rounded text-primary focus:ring-primary border-gray-300"
              :checked="allSelected"
              @change="toggleSelectAll"
            >
          </th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">需求标题</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-20 md:w-24">状态</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-16 md:w-20">优先级</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider hidden md:table-cell md:w-32">来源</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider hidden md:table-cell md:w-28 whitespace-nowrap">创建日期</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider hidden md:table-cell md:w-28 whitespace-nowrap">计划完成</th>
          <th class="p-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-24">操作</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200">
        <template v-if="loading">
          <tr>
            <td colspan="8" class="p-6 text-center text-gray-500">
              <i class="fas fa-spinner fa-spin mr-2"></i> 加载中...
            </td>
          </tr>
        </template>
        <template v-else-if="requirements.length === 0">
          <tr>
            <td colspan="8" class="p-6 text-center text-gray-500">
              暂无匹配的需求记录
            </td>
          </tr>
        </template>
        <template v-else>
          <tr v-for="item in requirements" :key="item.id" class="hover:bg-gray-50">
            <!-- 复选框 -->
            <td class="p-3 whitespace-nowrap">
              <input 
                type="checkbox" 
                class="rounded text-primary focus:ring-primary border-gray-300"
                :checked="selectedItems.includes(item.id)"
                @change="toggleSelect(item.id)"
              >
            </td>
            
            <!-- 需求标题 -->
            <td class="p-3 text-sm text-gray-700 font-medium truncate">
              <a 
                @click="$emit('view', item.id)"
                class="hover:text-primary cursor-pointer"
                :title="item.title"
              >{{ item.title }}</a>
            </td>
            
            <!-- 状态 -->
            <td class="p-3 whitespace-nowrap">
              <StatusTag :status="item.statusType" :text="item.statusText" />
            </td>
            
            <!-- 优先级 -->
            <td class="p-3 whitespace-nowrap">
              <PriorityTag :priority="item.priorityLevel" :text="item.priority" />
            </td>
            
            <!-- 来源 -->
            <td class="p-3 text-sm text-gray-500 hidden md:table-cell truncate md:w-32">
              <span :title="item.sourceType + (item.sourceDetail ? '：' + item.sourceDetail : '')">
                {{ item.sourceType }}{{ item.sourceDetail ? '：' + item.sourceDetail : '' }}
              </span>
            </td>
            
            <!-- 创建日期 -->
            <td class="p-3 text-sm text-gray-500 hidden md:table-cell md:w-28 whitespace-nowrap">{{ formatDate(item.createTime) }}</td>
            
            <!-- 计划完成 -->
            <td class="p-3 text-sm text-gray-500 hidden md:table-cell md:w-28 whitespace-nowrap">{{ formatDate(item.planTime) }}</td>
            
            <!-- 操作 -->
            <td class="p-3 whitespace-nowrap">
              <div class="flex space-x-1">
                <a 
                  @click="$emit('view', item.id)" 
                  class="!rounded-button p-1 text-gray-500 hover:text-primary tooltip cursor-pointer"
                >
                  <i class="fas fa-eye text-xs"></i>
                  <span class="tooltiptext">查看详情</span>
                </a>
                <a 
                  @click="$emit('edit', item.id)" 
                  class="!rounded-button p-1 text-gray-500 hover:text-primary tooltip cursor-pointer"
                >
                  <i class="fas fa-edit text-xs"></i>
                  <span class="tooltiptext">编辑需求</span>
                </a>
                <button 
                  @click="$emit('delete', item.id)" 
                  class="!rounded-button p-1 text-gray-500 hover:text-danger tooltip cursor-pointer"
                >
                  <i class="fas fa-trash text-xs"></i>
                  <span class="tooltiptext">删除需求</span>
                </button>
              </div>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import StatusTag from './StatusTag.vue';
import PriorityTag from './PriorityTag.vue';
import type { RequirementVO } from '@/types/requirement';

// 定义属性
const props = defineProps({
  requirements: {
    type: Array as () => RequirementVO[],
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

// 定义事件
const emit = defineEmits(['view', 'edit', 'delete', 'select']);

// 选中的项目ID列表
const selectedItems = ref<number[]>([]);

// 是否全选
const allSelected = computed(() => {
  return props.requirements.length > 0 && selectedItems.value.length === props.requirements.length;
});

// 切换全选
const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedItems.value = [];
  } else {
    selectedItems.value = props.requirements.map((item: RequirementVO) => item.id);
  }
  emit('select', selectedItems.value);
};

// 切换单选
const toggleSelect = (id: number) => {
  const index = selectedItems.value.indexOf(id);
  if (index === -1) {
    selectedItems.value.push(id);
  } else {
    selectedItems.value.splice(index, 1);
  }
  emit('select', selectedItems.value);
};

// 日期格式化函数
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error('Error formatting date:', e);
    return '-';
  }
};

// 当需求列表变化时，清空选择
watch(() => props.requirements, () => {
  selectedItems.value = [];
}, { deep: true });
</script>

<style scoped>
/* 表格样式 */
table {
  border-collapse: separate; /* 需要分离的边框，为圆角做准备 */
  border-spacing: 0;
}
thead th:first-child {
  border-top-left-radius: 8px;
}
thead th:last-child {
  border-top-right-radius: 8px;
}
tbody tr:last-child td:first-child {
  border-bottom-left-radius: 8px;
}
tbody tr:last-child td:last-child {
  border-bottom-right-radius: 8px;
}

/* 工具提示样式 */
.tooltip {
  position: relative;
  display: inline-block;
}
.tooltip .tooltiptext {
  visibility: hidden;
  width: 140px;
  background-color: #555;
  color: #fff;
  text-align: center;
  border-radius: 6px;
  padding: 5px;
  position: absolute;
  z-index: 1;
  bottom: 125%;
  left: 50%;
  margin-left: -70px;
  opacity: 0;
  transition: opacity 0.3s;
}
.tooltip .tooltiptext::after {
  content: "";
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: #555 transparent transparent transparent;
}
.tooltip:hover .tooltiptext {
  visibility: visible;
  opacity: 1;
}
</style>