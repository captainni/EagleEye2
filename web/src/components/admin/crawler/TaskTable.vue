<template>
  <div class="bg-white rounded-lg shadow-sm overflow-x-auto">
    <el-table
      :data="tasks"
      style="width: 100%"
      v-loading="loading"
      row-key="id"
    >
      <el-table-column prop="taskId" label="任务ID" min-width="150">
         <template #default="{ row }">
            <span class="font-mono">{{ row.taskId }}</span>
         </template>
      </el-table-column>

      <el-table-column prop="configName" label="配置名称" min-width="180" />

      <el-table-column prop="targetUrl" label="目标URL" min-width="250">
        <template #default="{ row }">
          <el-tooltip :content="row.targetUrl" placement="top" :show-after="500">
             <span class="truncate max-w-xs inline-block">{{ row.targetUrl }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column prop="startTime" label="开始时间" width="180" />

      <el-table-column prop="endTime" label="结束时间" width="180" />

      <el-table-column prop="status" label="抓取状态" min-width="100">
        <template #default="{ row }">
           <el-tag
             v-if="row.status === 'processing'"
             type="warning"
             size="small">
               处理中
           </el-tag>
           <el-tag
             v-else-if="row.status === 'success'"
             type="success"
             size="small">
               成功
           </el-tag>
           <el-tag
             v-else
             type="danger"
             size="small">
               失败
           </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="analysisStatus" label="分析状态" min-width="100">
        <template #default="{ row }">
           <el-tag
             v-if="row.analysisStatus"
             :type="getAnalysisStatusType(row.analysisStatus)"
             size="small"
           >
               {{ getAnalysisStatusText(row.analysisStatus) }}
           </el-tag>
           <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column prop="errorMessage" label="错误信息" min-width="200">
         <template #default="{ row }">
           <el-tooltip :content="row.errorMessage || '-'" placement="top" :show-after="500">
              <span :class="{'text-danger': row.status === 'failure'}" class="truncate max-w-xs inline-block">
                 {{ row.errorMessage || '-' }}
              </span>
          </el-tooltip>
         </template>
      </el-table-column>

      <el-table-column prop="categoryStats" label="分类统计" width="150">
        <template #default="{ row }">
          <el-tooltip v-if="row.categoryStats" :content="getCategoryStatsTooltip(row.categoryStats)" placement="top">
            <span class="text-xs">
              <el-tag size="small" type="info" class="mr-1">政策: {{ getCategoryCount(row.categoryStats, 'policy') }}</el-tag>
              <el-tag size="small" type="warning">竞品: {{ getCategoryCount(row.categoryStats, 'competitor') }}</el-tag>
            </span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="space-x-2 whitespace-nowrap flex items-center">
            <!-- 成功 + 未分析：智能分析按钮 + 再抓取 -->
            <template v-if="canAnalyze(row) && row.analysisStatus === 'pending'">
              <el-button link type="primary" @click="handleSmartAnalyze(row)" class="p-1 text-xs">
                <i :class="getAnalyzeIcon(row)"></i> {{ getAnalyzeButtonText(row) }}
              </el-button>
              <el-button
                link
                @click="handleReCrawl(row)"
                class="p-1 text-xs"
                :disabled="row.status === 'processing'"
              >
                <i :class="getReCrawlIcon(row)"></i> {{ getReCrawlText(row) }}
              </el-button>
            </template>

            <!-- 成功 + 分析中：禁用状态 + 再抓取 -->
            <template v-if="canAnalyze(row) && row.analysisStatus === 'analyzing'">
              <el-button disabled link class="p-1 text-xs">
                <i class="fas fa-brain fa-pulse"></i> 分析中...
              </el-button>
              <el-button
                link
                @click="handleReCrawl(row)"
                class="p-1 text-xs"
                :disabled="row.status === 'processing'"
              >
                <i :class="getReCrawlIcon(row)"></i> {{ getReCrawlText(row) }}
              </el-button>
            </template>

            <!-- 成功 + 已完成：再分析 + 再抓取 -->
            <template v-if="canAnalyze(row) && row.analysisStatus === 'completed'">
              <el-button
                link
                type="primary"
                @click="handleReAnalyze(row)"
                class="p-1 text-xs"
                :disabled="isAnalyzing(row.taskId)"
              >
                <i :class="getReAnalyzeIcon(row)"></i> {{ getReAnalyzeText(row) }}
              </el-button>
              <el-button
                link
                @click="handleReCrawl(row)"
                class="p-1 text-xs"
                :disabled="row.status === 'processing'"
              >
                <i :class="getReCrawlIcon(row)"></i> {{ getReCrawlText(row) }}
              </el-button>
            </template>

            <!-- 失败：重新执行 + 再抓取 -->
            <template v-if="row.status === 'failed'">
              <el-button link type="warning" @click="handleRetry(row)" class="p-1 text-xs">
                <i class="fas fa-redo-alt"></i> 重新执行
              </el-button>
              <el-button
                link
                @click="handleReCrawl(row)"
                class="p-1 text-xs"
                :disabled="row.status === 'processing'"
              >
                <i :class="getReCrawlIcon(row)"></i> {{ getReCrawlText(row) }}
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>

      <template #empty>
         <el-empty description="暂无任务日志数据"></el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';
import type { CrawlerTaskLogVO } from '@/types/admin/crawler';
import StatusTag from './StatusTag.vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({
  tasks: {
    type: Array as PropType<CrawlerTaskLogVO[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  analyzingTaskIds: {
    type: Array as PropType<string[]>,
    default: () => [],
  },
});

// 更新 emit 事件
const emit = defineEmits(['smartAnalyze', 'retry', 'reAnalyze', 'reCrawl']);

// 判断是否可以分析
const canAnalyze = (row: CrawlerTaskLogVO): boolean => {
  return row.status === 'success';
};

// 判断是否正在分析
const isAnalyzing = (taskId: string): boolean => {
  return props.analyzingTaskIds.includes(taskId);
};

// 获取分析按钮提示
const getAnalyzeButtonTooltip = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) {
    return '正在分析中...';
  }
  if (row.analysisStatus === 'completed') {
    return '重新分析';
  }
  if (row.analysisStatus === 'analyzing') {
    return '正在分析中...';
  }
  return '分析入库';
};

// 获取分析按钮类型
const getAnalyzeButtonType = (row: CrawlerTaskLogVO): string => {
  if (row.analysisStatus === 'completed') {
    return 'warning';
  }
  return 'primary';
};

// ============ 动态图标和文本方法 ============

// 获取智能分析按钮图标
const getAnalyzeIcon = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) {
    return 'fas fa-brain fa-pulse';
  }
  return 'fas fa-brain';
};

// 获取智能分析按钮文本
const getAnalyzeButtonText = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) {
    return '分析中...';
  }
  return '智能分析';
};

// 获取再分析按钮图标
const getReAnalyzeIcon = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) {
    return 'fas fa-circle-notch fa-spin';
  }
  return 'fas fa-brain';
};

// 获取再分析按钮文本
const getReAnalyzeText = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) {
    return '分析中...';
  }
  return '再分析';
};

// 获取再抓取按钮图标
const getReCrawlIcon = (row: CrawlerTaskLogVO): string => {
  if (row.status === 'processing') {
    return 'fas fa-circle-notch fa-spin';
  }
  return 'fas fa-sync';
};

// 获取再抓取按钮文本
const getReCrawlText = (row: CrawlerTaskLogVO): string => {
  if (row.status === 'processing') {
    return '抓取中...';
  }
  return '再抓取';
};

// 获取分析状态类型
const getAnalysisStatusType = (status: string | undefined): string => {
  switch (status) {
    case 'pending': return 'info';
    case 'analyzing': return 'warning';
    case 'completed': return 'success';
    case 'failed': return 'danger';
    default: return 'info';
  }
};

// 获取分析状态文本
const getAnalysisStatusText = (status: string | undefined): string => {
  switch (status) {
    case 'pending': return '待分析';
    case 'analyzing': return '分析中';
    case 'completed': return '已完成';
    case 'failed': return '失败';
    default: return '-';
  }
};

// 解析 categoryStats JSON（兼容旧格式和新格式）
const parseCategoryStats = (stats: string | undefined): Record<string, number> => {
  if (!stats) return {};
  try {
    // 尝试 JSON 解析
    return JSON.parse(stats);
  } catch {
    // 兼容旧格式："??:0,??:3" 或 "政策:0,竞品:3"
    try {
      const result: Record<string, number> = {};
      // 正则匹配数字
      const numbers = stats.match(/\d+/g);
      if (numbers && numbers.length >= 2) {
        // 旧格式假设第一个是政策，第二个是竞品
        result.policy = parseInt(numbers[0]) || 0;
        result.competitor = parseInt(numbers[1]) || 0;
      }
      return result;
    } catch {
      return {};
    }
  }
};

// 判断是否有政策文章
const hasPolicyArticles = (row: CrawlerTaskLogVO): boolean => {
  const stats = parseCategoryStats(row.categoryStats);
  return (stats.policy || 0) > 0;
};

// 判断是否有竞品文章
const hasCompetitorArticles = (row: CrawlerTaskLogVO): boolean => {
  const stats = parseCategoryStats(row.categoryStats);
  return (stats.competitor || 0) > 0;
};

// 获取分类数量
const getCategoryCount = (stats: string | undefined, category: string): number => {
  const parsed = parseCategoryStats(stats);
  return parsed[category] || 0;
};

// 获取分类统计 tooltip
const getCategoryStatsTooltip = (stats: string | undefined): string => {
  const parsed = parseCategoryStats(stats);
  const policyCount = parsed.policy || 0;
  const competitorCount = parsed.competitor || 0;
  return `政策类: ${policyCount} 篇，竞品类: ${competitorCount} 篇`;
};

// 获取政策分析按钮提示
const getPolicyAnalyzeButtonTooltip = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) return '正在分析中...';
  if (row.analysisStatus === 'completed') return '重新分析政策文章';
  return '分析政策文章入库';
};

// 获取竞品分析按钮提示
const getCompetitorAnalyzeButtonTooltip = (row: CrawlerTaskLogVO): string => {
  if (isAnalyzing(row.taskId)) return '正在分析中...';
  if (row.analysisStatus === 'completed') return '重新分析竞品文章';
  return '分析竞品文章入库';
};

// ============ 新的处理方法 ============

// 处理智能分析按钮点击
const handleSmartAnalyze = (row: CrawlerTaskLogVO) => {
  emit('smartAnalyze', row.taskId);
};

// 处理重新执行按钮点击
const handleRetry = (row: CrawlerTaskLogVO) => {
  emit('retry', row.taskId);
};

// 处理再分析按钮点击
const handleReAnalyze = (row: CrawlerTaskLogVO) => {
  emit('reAnalyze', row.taskId);
};

// 处理再抓取操作（直接弹出确认）
const handleReCrawl = (row: CrawlerTaskLogVO) => {
  ElMessageBox.confirm(
    '将用原配置重新抓取，确定吗？',
    '确认再抓取',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('reCrawl', row.taskId);
  }).catch(() => {
    // 用户取消
  });
};

// ============ 保留旧方法供兼容 ============

// 处理分析按钮点击（保留兼容）
const handleAnalyze = (row: CrawlerTaskLogVO) => {
  emit('smartAnalyze', row.taskId);
};

// 处理政策分析按钮点击（保留兼容，转发到智能分析）
const handlePolicyAnalyze = (row: CrawlerTaskLogVO) => {
  emit('smartAnalyze', row.taskId);
};

// 处理竞品分析按钮点击（保留兼容，转发到智能分析）
const handleCompetitorAnalyze = (row: CrawlerTaskLogVO) => {
  emit('smartAnalyze', row.taskId);
};
</script>

<style>
/* Use styles defined in ConfigTable.vue for consistency */
.text-danger {
  color: #ef4444;
}
.max-w-xs {
    max-width: 20rem; /* Corresponds to max-w-xs */
}
.truncate {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.inline-block {
    display: inline-block;
}

/* 分析图标旋转动画 */
.analyze-icon.fa-spin {
  animation: spin 1s linear infinite;
}

/* 旋转动画 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 脉冲动画 */
.fa-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
