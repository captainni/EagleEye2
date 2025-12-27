<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script lang="ts" setup>
// 空的setup脚本
</script>

<style>
body {
  margin: 0;
  padding: 0;
  background-color: #F8FAFC;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}

.app-container {
  min-height: 100vh;
  width: 100%;
}

/* 引入Tailwind基础样式 */
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 定义一些自定义的全局CSS变量 */
:root {
  --color-primary: #3b82f6;
  --color-secondary: #93c5fd;
  --color-warning: #f97316;
  --color-danger: #ef4444;
  --color-success: #10b981;
}
</style> 