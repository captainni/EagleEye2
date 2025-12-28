<template>
  <div class="bg-white rounded-lg shadow-sm overflow-x-auto">
    <el-table
      :data="tasks"
      style="width: 100%"
      v-loading="loading"
      row-key="id"
    >
      <el-table-column prop="id" label="任务ID" min-width="150">
         <template #default="{ row }">
            <span class="font-mono">{{ row.id }}</span>
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

      <el-table-column prop="status" label="爬取状态" min-width="100">
        <template #default="{ row }">
           <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
               {{ row.status === 'success' ? '成功' : '失败' }}
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

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <div class="space-x-1 whitespace-nowrap">
            <!-- 只对成功且有 batchPath 的任务显示分析按钮 -->
            <el-tooltip
              v-if="canAnalyze(row)"
              :content="getAnalyzeButtonTooltip(row)"
              placement="top"
            >
              <!-- 分析中状态：显示文字按钮 -->
              <el-button
                v-if="isAnalyzing(row.id) || row.analysisStatus === 'analyzing'"
                disabled
                loading
                size="small"
                class="text-xs"
              >
                分析中...
              </el-button>
              <!-- 正常状态：显示图标按钮 -->
              <el-button
                v-else
                link
                :type="getAnalyzeButtonType(row)"
                @click="handleAnalyze(row)"
                class="p-1 text-xs"
              >
                <i :class="getAnalyzeButtonIcon(row)" class="analyze-icon"></i>
              </el-button>
            </el-tooltip>

            <!-- 显示分析结果按钮（当分析完成时） -->
            <el-tooltip
              v-if="row.analysisStatus === 'completed' && row.analysisResult"
              content="查看分析结果"
              placement="top"
            >
              <el-button link type="success" @click="handleViewResult(row)" class="p-1 text-xs">
                <i class="fas fa-chart-bar"></i>
              </el-button>
            </el-tooltip>
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
import { ElMessage } from 'element-plus';

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

const emit = defineEmits(['analyze', 'viewResult']);

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
  if (isAnalyzing(row.id)) {
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

// 获取分析按钮图标
const getAnalyzeButtonIcon = (row: CrawlerTaskLogVO): string => {
  if (row.analysisStatus === 'analyzing' || isAnalyzing(row.id)) {
    return 'fas fa-spinner fa-spin';
  }
  if (row.analysisStatus === 'completed') {
    return 'fas fa-redo';
  }
  return 'fas fa-brain';
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

// 处理分析按钮点击
const handleAnalyze = (row: CrawlerTaskLogVO) => {
  emit('analyze', row.id);
};

// 处理查看结果按钮点击
const handleViewResult = (row: CrawlerTaskLogVO) => {
  if (row.analysisResult) {
    try {
      const result = JSON.parse(row.analysisResult);
      ElMessage.success(`分析完成: 共 ${result.total} 篇，成功 ${result.success} 篇，跳过 ${result.skipped} 篇，失败 ${result.failed} 篇`);
    } catch (e) {
      emit('viewResult', row);
    }
  } else {
    emit('viewResult', row);
  }
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

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
