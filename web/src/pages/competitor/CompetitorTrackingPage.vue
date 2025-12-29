<template>
  <div>
    <nav-bar :active-menu="'/competitor-tracking'" :user="userData" />

    <main class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- 筛选与搜索区域 -->
      <competitor-filter 
        :competitor-options="competitorOptions"
        :type-options="typeOptions"
        :time-options="timeOptions"
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
import { defaultUserData, competitorTypeArray, competitorTimeArray, competitorCompanyArray } from '@/utils/defaultData';

// 导入API服务
import { getCompetitorList, CompetitorQueryParams } from '@/api/competitor';

// 分页相关
const pageSize = ref(6);
const currentPage = ref(1);
const total = ref(0);
const loading = ref(false);
const competitors = ref([]);
const userData = defaultUserData;

// 筛选选项
const competitorOptions = competitorCompanyArray;
const typeOptions = competitorTypeArray;
const timeOptions = competitorTimeArray;

// 筛选条件
const filterOptions = ref({
  keyword: '',
  company: competitorOptions[0],
  type: typeOptions[0],
  time: timeOptions[0]
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
  if (filterOptions.value.keyword) {
      queryParams.keyword = filterOptions.value.keyword;
  }

  if (filterOptions.value.company !== competitorOptions[0]) {
      queryParams.competitor = filterOptions.value.company;
  }

  if (filterOptions.value.type !== typeOptions[0]) {
      queryParams.type = filterOptions.value.type;
    }

    // 根据timeOptions处理时间筛选
    if (filterOptions.value.time !== timeOptions[0]) {
      const now = new Date();
      let startDate = new Date();
      
      switch (filterOptions.value.time) {
        case '近7天':
          startDate.setDate(now.getDate() - 7);
          break;
        case '近30天':
          startDate.setDate(now.getDate() - 30);
          break;
        case '近90天':
          startDate.setDate(now.getDate() - 90);
          break;
      }
      
      queryParams.publishTimeStart = startDate.toISOString().split('T')[0];
      queryParams.publishTimeEnd = now.toISOString().split('T')[0];
    }
    
    // 调用API获取数据
    const result = await getCompetitorList(queryParams);
    // result 现在是分页对象 { list, total, ... }
    if (result && typeof result === 'object') {
      competitors.value = result.list || [];
      total.value = result.total || 0;
    } else {
      // 处理非预期响应格式
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
const handleFilter = (filters: any) => {
  filterOptions.value = filters;
  currentPage.value = 1; // 重置为第一页
  loadCompetitors(); // 重新加载数据
};

// 处理分页
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadCompetitors(); // 重新加载数据
};

// 组件挂载时加载数据
onMounted(() => {
  loadCompetitors();
});
</script> 