<template>
  <Card title="快速入口" icon="compass">
    <div class="grid grid-cols-2 gap-4">
      <router-link 
        v-for="entry in entries" 
        :key="entry.title" 
        :to="entry.link" 
        class="flex flex-col items-center justify-center p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors duration-200 cursor-pointer"
      >
        <i :class="['fas', `fa-${entry.icon}`, 'text-primary', 'text-2xl', 'mb-2']"></i>
        <span class="text-sm text-gray-700 font-medium">{{ entry.title }}</span>
      </router-link>
    </div>
  </Card>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';
import Card from '../common/Card.vue';

interface Entry {
  title: string;
  icon: string;
  link: string;
}

const props = defineProps<{
  entries: Entry[];
}>();
</script> 