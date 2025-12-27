<template>
  <div>
    <nav-bar :active-menu="'/competitor-tracking'" :user="userData" />

    <main class="max-w-[1200px] mx-auto px-6 pt-24 pb-12">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-12">
        <i class="fas fa-spinner fa-spin text-gray-300 text-4xl mb-4"></i>
        <p class="text-gray-500">加载中...</p>
      </div>
      <!-- 详情内容 -->
      <competitor-detail 
        v-else-if="competitor" 
        :competitor="competitor" 
      />
      <!-- 错误提示 -->
      <div v-else class="text-center py-12">
        <i class="fas fa-exclamation-circle text-red-300 text-4xl mb-4"></i>
        <p class="text-red-500">加载竞品详情失败</p>
      </div>
    </main>

    <footer-comp :copyright="'© 2024 金融资讯智能跟踪平台. All rights reserved.'" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from '../../components/common/NavBar.vue';
import FooterComp from '../../components/common/Footer.vue';
import CompetitorDetail from '../../components/competitor/CompetitorDetail.vue';
import { defaultUserData } from '@/utils/defaultData';
import { getCompetitorDetail, CompetitorVO } from '@/api/competitor';

const route = useRoute();
const competitor = ref<CompetitorVO | null>(null);
const loading = ref(true);
const userData = defaultUserData;

const loadCompetitorDetail = async () => {
  const id = route.params.id as string;
  if (!id) {
    console.error('未能从路由获取竞品ID');
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    const data = await getCompetitorDetail(Number(id));
    competitor.value = data;
  } catch (error) {
    console.error('获取竞品详情失败:', error);
    competitor.value = null;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCompetitorDetail();
});
</script> 