<template>
  <span :class="tagClass">{{ text }}</span>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  text: {
    type: String,
    required: true
  }
});

const tagClass = computed(() => {
  const baseClass = 'px-2 py-0.5 text-xs font-medium rounded-full';
  
  switch (props.status) {
    case 'pending':
      return `${baseClass} bg-orange-100 text-orange-600`;
    case 'processing':
      return `${baseClass} bg-blue-100 text-blue-600`;
    case 'completed':
      return `${baseClass} bg-green-100 text-green-600`;
    case 'rejected':
      return `${baseClass} bg-gray-100 text-gray-600`;
    default:
      return `${baseClass} bg-gray-100 text-gray-600`;
  }
});
</script>