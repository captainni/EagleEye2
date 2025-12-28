<template>
  <div>
    <NavBar :active-menu="activeMenu" :user="userData" />
    <div class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- Page Title or Header if needed -->
      <!-- <h2 class="text-2xl font-semibold text-gray-800 mb-6">设置中心</h2> -->

      <ProductManagement />

      <PushNotifications />

      <!-- Maybe a general save button if settings across components need unified saving -->
      <!-- Or rely on individual component save buttons -->

    </div>
     <!-- Footer component if needed -->
     <!-- <Footer /> -->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import NavBar from '@/components/common/NavBar.vue';
import { defaultUserData } from '@/utils/defaultData';
import ProductManagement from '@/components/settings/ProductManagement.vue';
import PushNotifications from '@/components/settings/PushNotifications.vue';

// Define props for NavBar
const activeMenu = '/settings'; // Current page path
const userData = ref(defaultUserData);

// No specific page-level logic needed for now as components handle their own data.

</script>

<style scoped>
/* Add page-specific styles if necessary */
/* Adjust pt-24 in the main content div if NavBar height is different */
</style> 