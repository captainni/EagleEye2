<template>
  <nav class="fixed top-0 left-0 right-0 bg-white shadow-sm z-50">
    <div class="max-w-[1440px] mx-auto px-6 h-16 flex items-center justify-between">
      <div class="flex items-center">
        <div class="flex flex-col">
          <h1 class="text-2xl font-['Pacifico'] text-primary leading-none mb-1">EagleEye</h1>
          <p class="text-xs text-gray-500 mt-0 leading-tight font-semibold">金融资讯跟踪平台</p>
        </div>
        <div class="ml-12 flex space-x-8">
          <router-link
            v-for="menu in menuItems" 
            :key="menu.path" 
            :to="menu.path" 
            :class="['nav-item', { 'text-primary font-medium': activeMenu === menu.path, 'text-gray-600': activeMenu !== menu.path }]"
          >
            {{ menu.title }}
          </router-link>
        </div>
      </div>
      <div class="flex items-center space-x-6">
        <div class="relative tooltip">
          <i class="fas fa-bell text-gray-600 text-xl cursor-pointer"></i>
          <span class="absolute -top-1 -right-1 w-4 h-4 bg-danger text-white rounded-full flex items-center justify-center text-xs">{{ user.notifications }}</span>
          <span class="tooltiptext">您有{{ user.notifications }}条紧急预警</span>
        </div>
        <div class="flex items-center space-x-2">
          <img :src="user.avatar" class="w-10 h-10 rounded-full object-cover border border-gray-200">
          <span class="text-gray-800 font-medium">{{ user.name }}</span>
        </div>
      </div>
    </div>
  </nav>
</template>

<script lang="ts" setup>
import { defineProps } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

interface Menu {
  title: string;
  path: string;
}

interface User {
  name: string;
  avatar: string;
  notifications: number;
}

const props = defineProps<{
  activeMenu: string;
  user: User;
}>();

const menuItems: Menu[] = [
  { title: '仪表盘', path: '/' },
  { title: '监管政策', path: '/policy-monitoring' },
  { title: '竞品动态', path: '/competitor-tracking' },
  { title: '需求池', path: '/requirement-pool' },
  { title: '设置中心', path: '/settings' },
  { title: '后端管理', path: '/admin/crawler' },
];
</script>

<style scoped>
.nav-item:hover {
  color: #3b82f6; /* 主色 */
  transition: color 0.3s ease;
}
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