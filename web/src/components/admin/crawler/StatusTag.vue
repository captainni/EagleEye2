<template>
  <span :class="tagClass" class="px-2 py-1 text-xs font-medium rounded-full whitespace-nowrap">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  status: string;
}>();

const tagClass = computed(() => {
  const upperStatus = props.status?.toUpperCase() || '';
  
  // Config Status
  if (upperStatus === 'ENABLED' || upperStatus === 'ENABLED' || upperStatus === 'TRUE') {
    return 'bg-success/10 text-success';
  } else if (upperStatus === 'DISABLED' || upperStatus === 'DISABLED' || upperStatus === 'FALSE') {
    return 'bg-gray-100 text-gray-600';
  }
  // Suggestion Status
  else if (upperStatus === 'PENDING') {
    return 'bg-yellow-100 text-yellow-600';
  } else if (upperStatus === 'APPROVED') {
    return 'bg-green-100 text-green-600';
  } else if (upperStatus === 'REJECTED') {
    return 'bg-red-100 text-red-600';
  }
  // Task Status & Execution Status
  else if (upperStatus === 'SUCCESS') {
    return 'text-success'; // Text color only for task status
  } else if (upperStatus === 'FAILURE') {
    return 'text-danger'; // Text color only for task status
  } else {
    return 'bg-gray-100 text-gray-600';
  }
});

const label = computed(() => {
  const upperStatus = props.status?.toUpperCase() || '';
  
  if (upperStatus === 'ENABLED' || upperStatus === 'ENABLED' || upperStatus === 'TRUE') {
    return '启用';
  } else if (upperStatus === 'DISABLED' || upperStatus === 'DISABLED' || upperStatus === 'FALSE') {
    return '禁用';
  } else if (upperStatus === 'PENDING') {
    return '待处理';
  } else if (upperStatus === 'APPROVED') {
    return '已采纳';
  } else if (upperStatus === 'REJECTED') {
    return '已拒绝';
  } else if (upperStatus === 'SUCCESS') {
    return '成功';
  } else if (upperStatus === 'FAILURE') {
    return '失败';
  } else {
    return props.status || '未知';
  }
});
</script>

<style scoped>
/* Use Tailwind utility classes directly */
.text-success {
  color: #10b981; /* Corresponds to text-success */
}
.text-danger {
  color: #ef4444; /* Corresponds to text-danger */
}
</style> 