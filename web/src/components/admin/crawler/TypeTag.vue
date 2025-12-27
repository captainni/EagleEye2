<template>
  <span :class="tagClass" class="px-2 py-1 text-xs rounded-full whitespace-nowrap">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  type: string;
}>();

const tagClass = computed(() => {
  const lowerType = props.type?.toLowerCase() || '';
  if (lowerType.includes('policy') || lowerType === 'new_source') {
    return 'bg-blue-100 text-blue-600';
  } else if (lowerType.includes('competitor')) {
    return 'bg-purple-100 text-purple-600';
  } else if (lowerType.includes('error') || lowerType.includes('config')) {
    return 'bg-orange-100 text-orange-600';
  } else {
    return 'bg-gray-100 text-gray-600';
  }
});

const label = computed(() => {
  const lowerType = props.type?.toLowerCase() || '';
  if (lowerType.includes('policy') || lowerType === 'new_source') {
    return '政策来源';
  } else if (lowerType.includes('competitor')) {
    return '竞品来源';
  } else if (lowerType.includes('error')) {
    return '配置错误';
  } else if (lowerType.includes('config')) {
    return '配置建议';
  } else {
    return props.type || '未知';
  }
});
</script> 