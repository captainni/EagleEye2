<template>
  <div>
    <nav-bar :active-menu="'/policy-monitoring'" :user="userData" />
    
    <main class="max-w-[1200px] mx-auto px-6 pt-24 pb-12">
      <div v-if="policy">
        <policy-detail :policy="policy" />
      </div>
      <div v-else class="flex justify-center items-center py-20">
        <div class="text-center">
          <i class="fas fa-spinner fa-spin text-primary text-3xl mb-4"></i>
          <p class="text-gray-500">加载政策详情中...</p>
        </div>
      </div>
    </main>
    
    <app-footer :copyright="'© 2024 金融资讯智能跟踪平台. All rights reserved.'" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from '@/components/common/NavBar.vue';
import AppFooter from '@/components/common/Footer.vue';
import PolicyDetail from '@/components/policy/PolicyDetail.vue';
import { defaultUserData } from '@/utils/defaultData';
import { getPolicyDetail, PolicyVO } from '@/api/policy';

const route = useRoute();
const userData = defaultUserData;
const policy = ref<PolicyVO | null>(null);
const loading = ref(true);

const loadPolicyDetail = async () => {
  const id = route.params.id as string;
  console.log('[PolicyDetailPage] Loading detail for ID:', id); // 打印 ID
  if (!id) {
    console.error('[PolicyDetailPage] Failed to get ID from route.');
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    console.log('[PolicyDetailPage] Calling getPolicyDetail API...');
    const data = await getPolicyDetail(Number(id));
    console.log('[PolicyDetailPage] API returned data:', JSON.stringify(data, null, 2)); // 打印 API 返回的原始数据
    policy.value = data; // 赋值
    console.log('[PolicyDetailPage] policy.value after assignment:', JSON.stringify(policy.value, null, 2)); // 打印赋值后的 policy.value
  } catch (error) {
    console.error('[PolicyDetailPage] Failed to fetch policy detail:', error);
    policy.value = null;
  } finally {
    loading.value = false;
    console.log('[PolicyDetailPage] Loading finished.');
  }
};

onMounted(() => {
  loadPolicyDetail();
});
</script> 