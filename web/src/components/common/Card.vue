<template>
  <div class="bg-white rounded-lg shadow-sm p-6 card-hover">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-800 flex items-center">
        <i :class="['fas', 'mr-2', 'text-primary', `fa-${icon}`]"></i> {{ title }}
      </h3>
      <router-link v-if="moreLink" :to="moreLink" class="text-sm text-primary hover:underline cursor-pointer">查看全部</router-link>
    </div>
    <slot></slot>
  </div>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';

const props = defineProps<{
  title: string;
  icon: string;
  moreLink?: string;
}>();
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