<template>
  <div class="bg-white rounded-lg shadow-sm p-6 card-hover flex flex-col">
    <h4 class="text-base font-semibold text-gray-800 mb-2 hover:text-primary cursor-pointer" @click="goToDetail">
      {{ policy.title }}
    </h4>
    <div class="flex items-center text-xs text-gray-500 mb-3">
      <span>来源：{{ policy.source || '未知' }}</span>
      <span class="mx-2">|</span>
      <span>发布日期：{{ formatDate }}</span>
    </div>
    <div class="flex flex-wrap gap-2 mb-3">
      <!-- 显示重要性 (移到最前) -->
      <span 
        v-if="importanceLabel" 
        class="px-2 py-0.5 text-xs font-medium rounded-full"
        :class="importanceClass"
      >
        {{ importanceLabel }}
      </span>
      
      <!-- 显示政策类型 -->
      <template v-if="policy.policyType">
        <span class="px-2 py-0.5 text-xs font-medium rounded-full bg-purple-100 text-purple-600">
          {{ policy.policyType }}
        </span>
      </template>
      
      <!-- 显示相关领域 -->
      <template v-if="policy.areas && policy.areas.length">
        <span 
          v-for="(area, index) in policy.areas" 
          :key="`area-${index}`" 
          class="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-100 text-blue-600"
        >
          {{ area }}
        </span>
      </template>
    </div>
    <p class="text-sm text-gray-600 mb-4 flex-grow">
      {{ policy.suggestion || policy.summary || '暂无建议' }}
    </p>
    <div class="flex justify-end space-x-3 mt-auto pt-4 border-t border-gray-100">
      <router-link 
        :to="{ name: 'PolicyDetail', params: { id: policy.id }}" 
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
import { convertPolicyToRequirement } from '@/services/requirementService';
import { ElMessage } from 'element-plus';

const router = useRouter();

const props = defineProps<{
  policy: Record<string, any>;
}>();

// 格式化日期
const formatDate = computed(() => {
  const dateStr = props.policy.publishTime;
  if (!dateStr) return '未知';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString();
  } catch (e) {
    console.error("Error formatting date:", e);
    return dateStr;
  }
});

// 重要性相关计算属性
const importanceLabel = computed(() => {
  const map: Record<string, string> = {
    '高': '高',
    '中': '中',
    '低': '低'
    };
  return props.policy.importance ? (map[props.policy.importance] || props.policy.importance) : '';
});

const importanceClass = computed(() => {
  if (!props.policy.importance) return '';
  
  const colorMap: Record<string, string> = {
    '高': 'bg-red-100 text-red-600',
    '中': 'bg-yellow-100 text-yellow-600',
    '低': 'bg-green-100 text-green-600'
    };
    return colorMap[props.policy.importance] || 'bg-gray-100 text-gray-600';
});

const goToDetail = () => {
  router.push({ name: 'PolicyDetail', params: { id: props.policy.id } });
};

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
