<template>
  <span :class="tagClass">{{ text }}</span>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
  priority: {
    type: String,
    required: true
  },
  text: {
    type: String,
    required: true
  }
});

const tagClass = computed(() => {
  const baseClass = 'px-2 py-0.5 text-xs font-medium text-white rounded-full';
  
  switch (props.priority) {
    case 'high':
      return `${baseClass} bg-danger`;
    case 'medium':
      return `${baseClass} bg-warning`;
    case 'low':
      return `${baseClass} bg-success`;
    default:
      return `${baseClass} bg-gray-500`;
  }
});
</script> 