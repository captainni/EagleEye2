<template>
  <div>
    <nav-bar :active-menu="'/policy-monitoring'" :user="userData" />
    
    <main class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- 筛选与搜索区域 -->
      <policy-filter :initial-filter="filters" @filter="handleFilter" />
      
      <!-- 政策列表区域 -->
      <policy-list :policies="policies" :filters="filters" :loading="loading" />
      
      <!-- 分页控件 -->
      <policy-pagination 
        :current-page="currentPage" 
        :total-items="totalPolicies" 
        :items-per-page="itemsPerPage" 
        @page-change="handlePageChange"
      />
    </main>
    
    <app-footer :copyright="'© 2026 金融资讯智能跟踪平台. All rights reserved.'" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import NavBar from '@/components/common/NavBar.vue';
import AppFooter from '@/components/common/Footer.vue';
import PolicyFilter from '@/components/policy/PolicyFilter.vue';
import PolicyList from '@/components/policy/PolicyList.vue';
import PolicyPagination from '@/components/policy/PolicyPagination.vue';
import { defaultUserData } from '@/utils/defaultData';
import { getPolicyList, PolicyQueryParams } from '@/api/policy';

const userData = defaultUserData;

// 分页相关数据
const currentPage = ref(1);
const itemsPerPage = ref(6);
const totalPolicies = ref(0);
const loading = ref(false);
const policies = ref([]);

// 过滤条件
const filters = reactive({
  keyword: '',
  importance: '',
  relevance: ''
});

// 加载政策数据
const loadPolicies = async () => {
  loading.value = true;
  try {
    // 构建API请求参数
    const queryParams: PolicyQueryParams = {
      pageNum: currentPage.value,
      pageSize: itemsPerPage.value
    };
    
    // 添加筛选条件
    if (filters.keyword) queryParams.keyword = filters.keyword;
    if (filters.importance) queryParams.importance = filters.importance;
    if (filters.relevance) queryParams.relevance = filters.relevance;
    
    // 调用API获取数据
    const result = await getPolicyList(queryParams);
    if (result && typeof result === 'object') {
      policies.value = result.list || [];
      totalPolicies.value = result.total || 0;
    }
  } catch (error) {
    console.error('获取政策列表失败:', error);
    // 加载失败时，使用空数组
    policies.value = [];
    totalPolicies.value = 0;
  } finally {
    loading.value = false;
  }
};

// 过滤器变化处理
const handleFilter = (newFilters: any) => {
  Object.assign(filters, newFilters);
  // 重置为第一页
  currentPage.value = 1;
  // 重新加载数据
  loadPolicies();
};

// 分页变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadPolicies();
};

// 组件挂载时加载数据
onMounted(() => {
  loadPolicies();
});
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