<template>
  <div>
    <nav-bar :active-menu="'/competitor-tracking'" :user="userData" />

    <main class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- 筛选与搜索区域 -->
      <competitor-filter
        :initial-filter="filters"
        @filter="handleFilter"
      />

      <!-- 动态列表区域 -->
      <competitor-list :competitors="competitors" :loading="loading" />

      <!-- 分页控件 -->
      <competitor-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @update:current-page="handlePageChange"
      />
    </main>

    <footer-comp :copyright="'© 2026 金融资讯智能跟踪平台. All rights reserved.'" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import NavBar from '../../components/common/NavBar.vue';
import FooterComp from '../../components/common/Footer.vue';
import CompetitorFilter from '../../components/competitor/CompetitorFilter.vue';
import CompetitorList from '../../components/competitor/CompetitorList.vue';
import CompetitorPagination from '../../components/competitor/CompetitorPagination.vue';

// 导入默认数据
import { defaultUserData } from '@/utils/defaultData';

// 导入API服务
import { getCompetitorList, CompetitorQueryParams } from '@/api/competitor';

// 分页相关
const pageSize = ref(6);
const currentPage = ref(1);
const total = ref(0);
const loading = ref(false);
const competitors = ref([]);
const userData = defaultUserData;

// 筛选条件
const filters = reactive({
  keyword: '',
  importance: '',
  relevance: ''
});

// 加载竞品数据
const loadCompetitors = async () => {
  loading.value = true;
  try {
    // 构建API请求参数
    const queryParams: CompetitorQueryParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };

    // 添加筛选条件
    if (filters.keyword) {
      queryParams.keyword = filters.keyword;
    }

    if (filters.importance) {
      queryParams.importance = filters.importance;
    }

    if (filters.relevance) {
      queryParams.relevance = filters.relevance;
    }

    // 调用API获取数据
    const result = await getCompetitorList(queryParams);
    if (result && typeof result === 'object') {
      competitors.value = result.list || [];
      total.value = result.total || 0;
    } else {
      console.warn('获取竞品列表响应格式不符预期:', result);
      competitors.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取竞品列表失败:', error);
    competitors.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理筛选
const handleFilter = (filterData: any) => {
  Object.assign(filters, filterData);
  currentPage.value = 1;
  loadCompetitors();
};

// 处理分页
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadCompetitors();
};

// 组件挂载时加载数据
onMounted(() => {
  loadCompetitors();
});
</script>
