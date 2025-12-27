<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 pb-4 border-b border-gray-200">推送设置</h3>

     <div v-if="loading" class="text-center text-gray-500">
      加载中...
    </div>
    <div v-if="!loading && error" class="text-center text-red-500">
      加载失败: {{ error }}
    </div>

    <form v-if="!loading && !error" @submit.prevent="saveSettings">
      <div class="mb-6">
        <label for="notification-frequency" class="block text-base font-medium text-gray-700 mb-2">接收频率</label>
        <select id="notification-frequency" v-model="currentSettings.frequency" class="block w-full sm:w-60 px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm">
          <option value="每日精选摘要">每日精选摘要</option>
          <option value="每周重点回顾">每周重点回顾</option>
          <option value="关闭推送">关闭推送</option>
        </select>
      </div>

      <div>
        <label class="block text-base font-medium text-gray-700 mb-2">紧急预警通知方式</label>
        <p class="text-sm text-gray-500 mb-3">选择接收重大政策或竞品动态紧急预警的渠道（可多选）：</p>
        <div class="space-y-2">
          <label class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" value="wechat" v-model="currentSettings.channels">
            <span class="text-sm text-gray-600">微信推送</span>
          </label>
          <label class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" value="sms" v-model="currentSettings.channels">
            <span class="text-sm text-gray-600">短信通知</span>
          </label>
          <label class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" value="email" v-model="currentSettings.channels">
            <span class="text-sm text-gray-600">邮件提醒</span>
          </label>
          <label class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" value="site" v-model="currentSettings.channels">
            <span class="text-sm text-gray-600">站内信</span>
          </label>
        </div>
      </div>

      <div class="mt-8 pt-6 border-t border-gray-200 flex justify-end">
            <button type="submit" class="!rounded-button px-6 py-2 bg-primary text-white hover:bg-primary/90 text-sm" :disabled="submittingSettings">
              <i v-if="submittingSettings" class="fas fa-spinner fa-spin mr-1"></i>
              {{ submittingSettings ? '保存中...' : '保存设置' }}
            </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { getSettingsData, savePushSettings } from '@/api/settings';
import type { PushSettings } from '@/types/settings';

const loading = ref(true);
const error = ref<string | null>(null);
const submittingSettings = ref(false);

const currentSettings = reactive<PushSettings>({
  frequency: '每日精选摘要', // Default value matching one option
  channels: []
});

const loadSettings = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await getSettingsData();
    // Use Object.assign to update reactive object
    Object.assign(currentSettings, data.pushSettings);
  } catch (err) {
    console.error("Error loading push settings:", err);
    error.value = err instanceof Error ? err.message : '获取推送设置失败';
  } finally {
    loading.value = false;
  }
};

const saveSettings = async () => {
  submittingSettings.value = true;
  try {
    // Pass a plain object copy to the API
    const settingsToSave = JSON.parse(JSON.stringify(currentSettings));
    const response = await savePushSettings(settingsToSave);
    if (response.success) {
      alert(response.message);
      // Optionally re-fetch settings or assume success
    } else {
      alert(`保存失败: ${response.message}`);
    }
  } catch (err) {
    console.error("Error saving push settings:", err);
    alert('保存推送设置时发生错误');
  } finally {
    submittingSettings.value = false;
  }
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped>
/* Add any component-specific styles here if needed */
</style> 