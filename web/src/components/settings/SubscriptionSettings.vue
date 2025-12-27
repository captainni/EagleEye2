<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 pb-4 border-b border-gray-200">信息源订阅</h3>
    <p class="text-sm text-gray-500 mb-6">勾选您希望接收更新和分析摘要的信息来源。</p>

    <div v-if="loading" class="text-center text-gray-500">
      加载中...
    </div>

    <div v-if="!loading && error" class="text-center text-red-500">
      加载失败: {{ error }}
    </div>

    <div v-if="!loading && !error">
      <div class="mb-8">
        <h4 class="text-base font-medium text-gray-700 mb-3">监管政策来源</h4>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
          <label v-for="source in policySources" :key="source.id" class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" v-model="source.checked">
            <span class="text-sm text-gray-600">{{ source.name }}</span>
          </label>
        </div>
      </div>

      <hr class="my-6 border-gray-200">

      <div>
        <h4 class="text-base font-medium text-gray-700 mb-3">竞品信息来源</h4>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
          <label v-for="source in competitorSources" :key="source.id" class="flex items-center space-x-2 cursor-pointer">
            <input type="checkbox" class="rounded text-primary border-gray-300 focus:ring-primary" v-model="source.checked">
            <span class="text-sm text-gray-600">{{ source.name }}</span>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getSettingsData } from '@/api/settings';
import type { Source } from '@/types/settings';

const policySources = ref<Source[]>([]);
const competitorSources = ref<Source[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const loadSources = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await getSettingsData();
    policySources.value = data.policySources;
    competitorSources.value = data.competitorSources;
  } catch (err) {
    console.error("Error loading sources:", err);
    error.value = err instanceof Error ? err.message : '获取信息源数据失败';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadSources();
});
</script>
