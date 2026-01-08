<template>
  <div>
    <NavBar :active-menu="activeMenu" :user="mockUser" />
    <div class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">

      <!-- Tabs Navigation -->
      <el-tabs v-model="activeTab" class="admin-crawler-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="配置管理" name="config"></el-tab-pane>
        <el-tab-pane label="任务监控" name="monitor"></el-tab-pane>
      </el-tabs>

      <!-- Tab Content -->
      <div class="mt-6">
        <ConfigManagementTab v-if="activeTab === 'config'" />
        <TaskMonitoringTab v-if="activeTab === 'monitor'" />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { TabsPaneContext } from 'element-plus';
import ConfigManagementTab from './ConfigManagementTab.vue';
import TaskMonitoringTab from './TaskMonitoringTab.vue';
import NavBar from '@/components/common/NavBar.vue';

const activeTab = ref('config');

const activeMenu = '/admin/crawler';
const mockUser = ref({
  name: '管理员',
  avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
  notifications: 0,
});

const handleTabClick = (tab: TabsPaneContext) => {
  // console.log('Tab clicked:', tab.paneName);
  // Optional: Add logic if needed when tab changes, like pre-fetching data
};
</script>

<style>
/* Customize el-tabs to match prototype's horizontal line style */
.admin-crawler-tabs .el-tabs__header {
  margin-bottom: 0; /* Remove default bottom margin */
}

.admin-crawler-tabs .el-tabs__nav-wrap::after {
  content: none; /* Remove the default bottom border of the header */
}

.admin-crawler-tabs .el-tabs__active-bar {
  height: 2px; /* Match prototype border thickness */
  background-color: var(--el-color-primary); /* Use Element Plus primary color */
}

.admin-crawler-tabs .el-tabs__item {
  padding: 0 0.25rem !important; /* py-4 px-1 - Adjust padding as needed */
  margin-right: 2rem; /* space-x-8 */
  height: auto !important; /* Allow natural height */
  line-height: normal !important; /* Allow natural line height */
  padding-bottom: 1rem; /* Match prototype py-4 */
  color: #4b5563; /* text-gray-600 */
  font-weight: 400; /* Match prototype default weight */
  border-bottom: 2px solid transparent; /* Add transparent border for spacing */
  transition: all 0.3s ease;
}

.admin-crawler-tabs .el-tabs__item:hover {
  color: var(--el-color-primary); /* text-primary on hover */
}

.admin-crawler-tabs .el-tabs__item.is-active {
  color: var(--el-color-primary); /* text-primary */
  font-weight: 500; /* font-medium */
  /* Active bar handles the bottom border color */
}

/* Ensure content has some top margin after tabs */
/* .mt-6 class is added in the template */

</style> 