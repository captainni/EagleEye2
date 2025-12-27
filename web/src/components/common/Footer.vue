<template>
  <footer class="bg-white mt-8 border-t border-gray-200">
    <div class="max-w-[1440px] mx-auto px-6 py-6 text-center text-gray-500 text-sm">
      <p>{{ copyright }}</p>
    </div>
  </footer>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';

const props = defineProps<{
  copyright: string;
}>();
</script> 