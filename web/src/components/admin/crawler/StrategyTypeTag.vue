<template>
  <span :class="tagClass" class="px-2 py-1 text-xs rounded-full">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  type: 'css' | 'llm' | 'none';
}>();

const tagClass = computed(() => {
  switch (props.type) {
    case 'css':
      return 'bg-green-100 text-green-600';
    case 'llm':
      return 'bg-indigo-100 text-indigo-600';
    case 'none':
      return 'bg-gray-100 text-gray-600';
    default:
      return 'bg-gray-100 text-gray-600';
  }
});

const label = computed(() => {
  switch (props.type) {
    case 'css':
      return 'CSS';
    case 'llm':
      return 'LLM';
    case 'none':
      return '仅抓取';
    default:
      return '未知';
  }
});
</script> 