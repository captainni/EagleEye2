<template>
  <div>
    <NavBar :active-menu="'/'" :user="userData" />
    
    <main class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- 紧急预警区域 -->
      <AlertBanner v-if="emergencyPolicy" :alert="emergencyPolicyToAlertFormat(emergencyPolicy)" />

      <div v-if="loading" class="flex justify-center items-center p-16">
        <div v-loading="true" style="width: 100%; height: 200px;"></div>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 最新政策提醒 -->
        <PolicyCard :policies="policySummary?.latestImportantPolicies || []" />

        <!-- 最新竞品动态 -->
        <CompetitorCard :competitors="competitorSummary?.latestCompetitorUpdates || []" />

        <!-- 待处理需求概览 -->
        <RequirementCard :requirements="requirementSummary?.latestRequirements || []" />

        <!-- 快速入口 -->
        <QuickEntryCard :entries="quickEntryData" />
      </div>

      <!-- 数据统计图表区域 -->
      <h3 class="text-xl font-semibold text-gray-800 mt-8 mb-4">数据分析</h3>
      <div v-if="loadingChart" class="flex justify-center items-center p-16">
        <div v-loading="true" style="width: 100%; height: 200px;"></div>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 趋势图 -->
        <TrendChart :chart-data="trendChartData" />

        <!-- 饼图 -->
        <PieChart :chart-data="pieChartData" />
      </div>
    </main>

    <Footer :copyright="'© 2026 金融资讯智能跟踪平台. All rights reserved.'" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import NavBar from '../components/common/NavBar.vue';
import Footer from '../components/common/Footer.vue';
import AlertBanner from '../components/common/AlertBanner.vue';
import PolicyCard from '../components/dashboard/PolicyCard.vue';
import CompetitorCard from '../components/dashboard/CompetitorCard.vue';
import RequirementCard from '../components/dashboard/RequirementCard.vue';
import QuickEntryCard from '../components/dashboard/QuickEntryCard.vue';
import TrendChart from '../components/dashboard/TrendChart.vue';
import PieChart from '../components/dashboard/PieChart.vue';

// 仪表盘服务
import { 
  getDashboardStats,
  getPolicySummary,
  getCompetitorSummary,
  getRequirementSummary
} from '../services/dashboardService';
import { getPolicyList, PolicyQueryParams } from '@/api/policy'; // 导入用于获取紧急政策

// 类型定义
import type { 
  DashboardStatsVO,
  PolicySummaryVO,
  CompetitorSummaryVO,
  RequirementSummaryVO
} from '../types/dashboard';
import type { PolicyVO } from '@/types/policy'; // 导入政策类型
import type { Alert } from '@/types/common'; // 导入 Alert 类型

// 默认数据
import { defaultUserData, defaultQuickEntryData } from '@/utils/defaultData';

// 加载状态
const loading = ref(true);
const loadingChart = ref(true);

// 各种数据
const userData = defaultUserData;
const quickEntryData = defaultQuickEntryData;
const dashboardStats = ref<DashboardStatsVO | null>(null);
const policySummary = ref<PolicySummaryVO | null>(null);
const competitorSummary = ref<CompetitorSummaryVO | null>(null);
const requirementSummary = ref<RequirementSummaryVO | null>(null);
const emergencyPolicy = ref<PolicyVO | null>(null);

// 将 PolicyVO 转换为 AlertBanner 需要的格式
const emergencyPolicyToAlertFormat = (policy: PolicyVO | null): Alert => {
  if (!policy) {
    // 返回默认警报以避免类型错误
    return {
      id: 0,
      type: '紧急预警',
      title: '暂无紧急预警',
      description: '暂无紧急预警信息',
      importance: '高',
      impactArea: '暂无',
      suggestion: '暂无建议',
      link: '#'
    };
  }
  
  // 获取第一条建议
  let suggestionText = '查看详情评估影响';
  if (policy.suggestions && Array.isArray(policy.suggestions) && policy.suggestions.length > 0) {
    const firstSuggestion = policy.suggestions[0];
    if (typeof firstSuggestion === 'string') {
      suggestionText = firstSuggestion;
    } else if (firstSuggestion && typeof firstSuggestion === 'object' && 'suggestion' in firstSuggestion) {
      suggestionText = String(firstSuggestion.suggestion || '');
    }
  }
  
  // 安全获取importance
  const importanceValue = policy.importance ? String(policy.importance) : '高';
  
  return {
    id: policy.id,
    type: '紧急预警',
    title: policy.title,
    importance: importanceValue,
    impactArea: policy.areas?.join('、') || '暂无',
    suggestion: suggestionText,
    description: `重要性：${importanceValue} | 影响范围：${policy.areas?.join('、') || '暂无'} | 建议：${suggestionText}`,
    link: `/policy-detail/${policy.id}` // 修正链接路径，与路由配置一致
  };
};

// 计算图表数据
const trendChartData = computed(() => {
  if (!dashboardStats.value) return { labels: [], datasets: [] };
  
  const weekData = dashboardStats.value.trendStats.lastWeekData;
  return {
    labels: weekData.map(item => item.date),
    datasets: [
      {
        label: '政策数量',
        data: weekData.map(item => item.policyCount),
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1
      },
      {
        label: '竞品数量',
        data: weekData.map(item => item.competitorCount),
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgb(54, 162, 235)',
        tension: 0.1
      },
      {
        label: '需求数量',
        data: weekData.map(item => item.requirementCount),
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderColor: 'rgb(255, 99, 132)',
        tension: 0.1
      }
    ]
  };
});

const pieChartData = computed(() => {
  if (!dashboardStats.value) return { labels: [], datasets: [] };
  
  const { policyStats, competitorStats, requirementStats } = dashboardStats.value;
  
  return {
    // 政策重要性分布
    policyImportance: {
      labels: Object.keys(policyStats.importanceDistribution),
      datasets: [{
        data: Object.values(policyStats.importanceDistribution),
        backgroundColor: [
          'rgb(255, 99, 132)',
          'rgb(54, 162, 235)',
          'rgb(255, 205, 86)'
        ]
      }]
    },
    // 竞品银行分布
    competitorBank: {
      labels: Object.keys(competitorStats.bankDistribution),
      datasets: [{
        data: Object.values(competitorStats.bankDistribution),
        backgroundColor: [
          'rgb(255, 99, 132)',
          'rgb(54, 162, 235)',
          'rgb(255, 205, 86)'
        ]
      }]
    },
    // 需求状态分布
    requirementStatus: {
      labels: Object.keys(requirementStats.statusDistribution),
      datasets: [{
        data: Object.values(requirementStats.statusDistribution),
        backgroundColor: [
          'rgb(255, 99, 132)',
          'rgb(54, 162, 235)',
          'rgb(255, 205, 86)'
        ]
      }]
    }
  };
});

// 获取所有数据
const fetchData = async () => {
  loading.value = true;
  loadingChart.value = true;
  try {
    // 导入政策适配器
    const { normalizePolicy, normalizePolicies } = await import('@/utils/policyAdapter');
    
    // 打印请求前的日志
    console.log('[Dashboard] 即将请求政策数据，limit=3');
    
    // 并行获取摘要数据和统计数据
    const summaryPromise = Promise.all([
      getPolicySummary(3), // 确保请求3条政策数据
      getCompetitorSummary(3),
      getRequirementSummary(4),
      getPolicyList({ importance: '高', pageNum: 1, pageSize: 1 })
    ]);
    
    const statsPromise = getDashboardStats();

    const [[policyRes, competitorRes, requirementRes, emergencyRes], statsRes] = await Promise.all([summaryPromise, statsPromise]);
    
    // 详细记录政策数据
    console.log('[Dashboard] 收到政策数据响应:', JSON.stringify(policyRes));
    console.log('[Dashboard] 政策数据状态码:', policyRes.code);
    console.log('[Dashboard] 政策数据消息:', policyRes.message);
    
    if (policyRes.data && policyRes.data.latestImportantPolicies) {
      console.log('[Dashboard] 收到政策条数:', policyRes.data.latestImportantPolicies.length);
      console.log('[Dashboard] 政策数据详情:', JSON.stringify(policyRes.data.latestImportantPolicies));
      
      // 更新摘要数据，并统一处理importance字段
      policyRes.data.latestImportantPolicies = normalizePolicies(policyRes.data.latestImportantPolicies);
      console.log('[Dashboard] 规范化后政策条数:', policyRes.data.latestImportantPolicies.length);
      
      // 保存到localStorage以便排查
      try {
        localStorage.setItem('dashboard_policies', JSON.stringify(policyRes.data.latestImportantPolicies));
      } catch (e) {
        console.warn('无法保存政策数据到localStorage:', e);
      }
    } else {
      console.warn('[Dashboard] 未收到政策数据或数据格式不正确');
    }
    
    policySummary.value = policyRes.data;
    
    // 检查组件接收数据后的状态
    console.log('[Dashboard] 设置到policySummary后:', policySummary.value?.latestImportantPolicies?.length);
    
    competitorSummary.value = competitorRes.data;
    requirementSummary.value = requirementRes.data;
    
    // 处理紧急政策
    if (emergencyRes.list && emergencyRes.list.length > 0) {
      const normalizedPolicy = normalizePolicy(emergencyRes.list[0]);
      emergencyPolicy.value = normalizedPolicy;
    }
    
    loading.value = false;

    // 更新统计数据
    dashboardStats.value = statsRes.data;
    loadingChart.value = false;

  } catch (error) {
    console.error('[Dashboard] 获取仪表盘数据失败：', error);
    ElMessage.error('获取仪表盘数据失败，请稍后重试');
    loading.value = false;
    loadingChart.value = false;
  } 
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
/* 页面特定样式 */
body {
  min-height: 1024px;
  background-color: #F8FAFC; /* 页面背景色 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}
</style> 