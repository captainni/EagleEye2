<template>
  <el-drawer
    v-model="visible"
    title="分析结果"
    size="50%"
    :before-close="handleClose"
  >
    <div v-loading="loading" class="analysis-results">
      <!-- 分析状态 -->
      <div class="status-bar mb-4">
        <el-tag :type="getStatusType(results.analysisStatus)" size="large">
          {{ getStatusText(results.analysisStatus) }}
        </el-tag>
        <span class="ml-3 text-gray-500">任务ID: {{ results.taskId }}</span>
      </div>

      <!-- 分析摘要 -->
      <div v-if="results.summary" class="summary-card mb-4">
        <el-card>
          <template #header>
            <span class="font-semibold">分析摘要</span>
          </template>
          <div class="grid grid-cols-4 gap-4">
            <div class="text-center">
              <div class="text-2xl font-bold text-blue-600">{{ results.summary.total || 0 }}</div>
              <div class="text-sm text-gray-500">总计</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-green-600">{{ results.summary.success || 0 }}</div>
              <div class="text-sm text-gray-500">成功</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-yellow-600">{{ results.summary.skipped || 0 }}</div>
              <div class="text-sm text-gray-500">跳过</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-red-600">{{ results.summary.failed || 0 }}</div>
              <div class="text-sm text-gray-500">失败</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分类统计 -->
      <div v-if="results.categoryStats" class="category-stats mb-4">
        <el-card>
          <template #header>
            <span class="font-semibold">文章分类</span>
          </template>
          <div class="flex space-x-4">
            <el-tag size="large" type="info">政策: {{ getCategoryCount('policy') }}</el-tag>
            <el-tag size="large" type="warning">竞品: {{ getCategoryCount('competitor') }}</el-tag>
            <el-tag size="large" type="default">总计: {{ totalArticles }}</el-tag>
          </div>
        </el-card>
      </div>

      <!-- 详细结果 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="政策分析" name="policy">
          <div v-if="getCategoryCount('policy') > 0" class="policy-content">
            <el-alert
              title="政策文章分析结果"
              type="info"
              :closable="false"
              show-icon
              class="mb-3"
            >
              <p>共分析 {{ getCategoryCount('policy') }} 篇政策文章</p>
              <p class="text-sm text-gray-600 mt-1">详细结果请前往「监管政策」页面查看</p>
            </el-alert>
            <el-button type="primary" @click="goToPolicyPage">
              <i class="fas fa-external-link-alt"></i> 前往监管政策页面
            </el-button>
          </div>
          <!-- 空状态：友好展示 -->
          <div v-else class="empty-state">
            <el-empty description="此任务没有政策文章">
              <template #image>
                <i class="fas fa-file-alt" style="font-size: 48px; color: #dcdfe6;"></i>
              </template>
              <p class="text-sm text-gray-500 mt-2">
                <span v-if="getCategoryCount('competitor') > 0">
                  任务共抓取 {{ totalArticles }} 篇文章，全部为竞品类型
                </span>
                <span v-else>
                  此任务没有可分析的文章
                </span>
              </p>
            </el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane label="竞品分析" name="competitor">
          <div v-if="getCategoryCount('competitor') > 0" class="competitor-content">
            <el-alert
              title="竞品文章分析结果"
              type="success"
              :closable="false"
              show-icon
              class="mb-3"
            >
              <p>共分析 {{ getCategoryCount('competitor') }} 篇竞品文章</p>
              <p class="text-sm text-gray-600 mt-1">详细结果请前往「竞品动态」页面查看</p>
            </el-alert>
            <el-button type="success" @click="goToCompetitorPage">
              <i class="fas fa-external-link-alt"></i> 前往竞品动态页面
            </el-button>
          </div>
          <!-- 空状态：友好展示 -->
          <div v-else class="empty-state">
            <el-empty description="此任务没有竞品文章">
              <template #image>
                <i class="fas fa-chart-line" style="font-size: 48px; color: #dcdfe6;"></i>
              </template>
              <p class="text-sm text-gray-500 mt-2">
                <span v-if="getCategoryCount('policy') > 0">
                  任务共抓取 {{ totalArticles }} 篇文章，全部为政策类型
                </span>
                <span v-else>
                  此任务没有可分析的文章
                </span>
              </p>
            </el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getAnalysisResults, type AnalysisResultsData } from '@/api/admin/crawler';
import { ElMessage } from 'element-plus';

interface Props {
  modelValue: boolean;
  taskId: string;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const router = useRouter();
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const loading = ref(false);
const activeTab = ref('policy');
const results = ref<AnalysisResultsData>({
  taskId: '',
  analysisStatus: 'pending'
});

// 解析 categoryStats（兼容 JSON 和逗号分隔格式）
const parseCategoryStats = computed(() => {
  if (!results.value.categoryStats) return {};
  try {
    // 尝试 JSON 解析
    return JSON.parse(results.value.categoryStats);
  } catch {
    // 兼容逗号分隔格式：政策:3,竞品:0
    try {
      const result: Record<string, number> = {};
      const stats = results.value.categoryStats;
      // 分割逗号分隔的字符串
      const parts = stats.split(',');
      for (const part of parts) {
        // 匹配 "xxx:数字" 格式
        const kvMatch = part.match(/([^:]+):(\d+)/);
        if (kvMatch) {
          const [, key, value] = kvMatch;
          result[key.trim()] = parseInt(value) || 0;
        }
      }
      return result;
    } catch {
      return {};
    }
  }
});

const getCategoryCount = (category: string): number => {
  return (parseCategoryStats.value as Record<string, number>)[category] || 0;
};

// 文章总数
const totalArticles = computed(() => {
  const policyCount = getCategoryCount('policy');
  const competitorCount = getCategoryCount('competitor');
  return policyCount + competitorCount;
});

const getStatusType = (status: string): string => {
  switch (status) {
    case 'pending': return 'info';
    case 'analyzing': return 'warning';
    case 'completed': return 'success';
    case 'failed': return 'danger';
    default: return 'info';
  }
};

const getStatusText = (status: string): string => {
  switch (status) {
    case 'pending': return '待分析';
    case 'analyzing': return '分析中';
    case 'completed': return '已完成';
    case 'failed': return '失败';
    default: return '-';
  }
};

const loadResults = async () => {
  if (!props.taskId) return;

  loading.value = true;
  try {
    const data = await getAnalysisResults(props.taskId);
    results.value = data;

    // 根据文章数量自动切换到有内容的标签
    const policyCount = getCategoryCount('policy');
    const competitorCount = getCategoryCount('competitor');
    if (policyCount > 0 && competitorCount === 0) {
      activeTab.value = 'policy';
    } else if (competitorCount > 0 && policyCount === 0) {
      activeTab.value = 'competitor';
    }
  } catch (error) {
    console.error('获取分析结果失败:', error);
    ElMessage.error('获取分析结果失败');
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
};

const goToPolicyPage = () => {
  router.push('/policy');
  handleClose();
};

const goToCompetitorPage = () => {
  router.push('/competitor');
  handleClose();
};

// 监听 drawer 打开，自动加载数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadResults();
  }
});
</script>

<style scoped>
.analysis-results {
  padding: 0 10px;
}

.status-bar {
  display: flex;
  align-items: center;
}

.summary-card :deep(.el-card__body) {
  padding: 20px;
}

.drawer-footer {
  text-align: right;
}

.mb-3 {
  margin-bottom: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-3 {
  margin-left: 12px;
}

.mt-1 {
  margin-top: 4px;
}

.text-gray-500 {
  color: #6b7280;
}

.text-gray-600 {
  color: #4b5563;
}

.font-semibold {
  font-weight: 600;
}

.grid {
  display: grid;
}

.grid-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.gap-4 {
  gap: 1rem;
}

.text-center {
  text-align: center;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.font-bold {
  font-weight: 700;
}

.text-blue-600 {
  color: #2563eb;
}

.text-green-600 {
  color: #16a34a;
}

.text-yellow-600 {
  color: #ca8a04;
}

.text-red-600 {
  color: #dc2626;
}

.flex {
  display: flex;
}

.space-x-4 > :not([hidden]) ~ :not([hidden]) {
  --tw-space-x-reverse: 0;
  margin-right: calc(1rem * var(--tw-space-x-reverse));
  margin-left: calc(1rem * calc(1 - var(--tw-space-x-reverse)));
}

.detail-tabs {
  margin-top: 20px;
}

.policy-content,
.competitor-content {
  padding: 20px 0;
}
</style>
