<template>
  <div class="bg-white rounded-lg shadow-sm overflow-x-auto">
    <el-table
      :data="configs"
      style="width: 100%"
      v-loading="loading"
      row-key="configId"
      :row-class-name="tableRowClassName"
    >
      <el-table-column prop="targetName" label="配置名称" min-width="180">
        <template #default="{ row }">
          <span class="font-medium">{{ row.targetName }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="crawlerService" label="爬虫服务" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.crawlerService === 'eagleeye' ? 'success' : 'info'" size="small">
            {{ row.crawlerService === 'eagleeye' ? 'EagleEye' : '传统' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="extractionStrategyType" label="提取策略" min-width="100">
        <template #default="{ row }">
          <StrategyTypeTag :type="row.extractionStrategyType" />
        </template>
      </el-table-column>

      <el-table-column prop="triggerSchedule" label="计划" min-width="120">
         <template #default="{ row }">
           <span>{{ displaySchedule(row.triggerSchedule) }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="isActive" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
            {{ row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="updateTime" label="更新时间" min-width="160">
        <template #default="{ row }">
           <span>{{ formatDateTime(row.updateTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="space-x-1 whitespace-nowrap">
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" @click="$emit('edit', row)" class="p-1 text-xs">
                <i class="fas fa-edit"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
               <el-button link type="danger" @click="$emit('delete', row.configId)" class="p-1 text-xs">
                 <i class="fas fa-trash"></i>
               </el-button>
            </el-tooltip>
            <el-tooltip :content="getTriggerButtonTooltip(row)" placement="top">
               <el-button
                 link
                 :type="getTriggerButtonType(row)"
                 @click="handleTrigger(row)"
                 :disabled="isTaskRunning(row.configId)"
                 class="p-1 text-xs trigger-button"
                 :class="{ 'spinning': isTaskRunning(row.configId) }"
               >
                  <i :class="getTriggerButtonIcon(row)" class="trigger-icon"></i>
                </el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
       <template #empty>
         <el-empty description="暂无配置数据"></el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';
import { CrawlerConfigVO } from '@/types/admin/crawler';
import TypeTag from './TypeTag.vue';
import StrategyTypeTag from './StrategyTypeTag.vue';
import { format } from 'date-fns';

const props = defineProps({
  configs: {
    type: Array as PropType<CrawlerConfigVO[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  runningTaskIds: {
    type: Array as PropType<number[]>,
    default: () => [],
  },
  completedTaskIds: {
    type: Array as PropType<number[]>,
    default: () => [],
  },
});

const emit = defineEmits(['edit', 'delete', 'toggleStatus', 'trigger']);

const tableRowClassName = ({ row }: { row: CrawlerConfigVO }) => {
  if (!row.isActive) {
    return 'opacity-60';
  }
  return '';
};

const displaySchedule = (schedule?: string): string => {
  return schedule || '-';
};

const formatDateTime = (dateTimeString?: string): string => {
  if (!dateTimeString) return '-';
  try {
    return format(new Date(dateTimeString), 'yyyy-MM-dd HH:mm');
  } catch (e) {
    console.error("Error formatting date:", dateTimeString, e);
    return dateTimeString;
  }
};

const isTaskRunning = (configId: number): boolean => {
  return (props.runningTaskIds || []).includes(configId);
};

const isTaskCompleted = (configId: number): boolean => {
  return (props.completedTaskIds || []).includes(configId);
};

const getTriggerButtonTooltip = (row: CrawlerConfigVO): string => {
  if (isTaskRunning(row.configId)) {
    return '正在抓取中...';
  }
  if (isTaskCompleted(row.configId)) {
    return '抓取完成';
  }
  return '立即触发';
};

const getTriggerButtonType = (row: CrawlerConfigVO): string => {
  if (isTaskCompleted(row.configId)) {
    return 'success';
  }
  return 'info';
};

const getTriggerButtonIcon = (row: CrawlerConfigVO): string => {
  if (isTaskRunning(row.configId)) {
    return 'fas fa-circle-notch';
  }
  if (isTaskCompleted(row.configId)) {
    return 'fas fa-check-circle';
  }
  return 'fas fa-play-circle';
};

const handleTrigger = (row: CrawlerConfigVO) => {
  emit('trigger', row.configId);
};


</script>

<style>
.el-table .el-button.p-1 {
    padding: 0.25rem;
    font-size: 1rem;
}

/* 触发按钮旋转动画 */
.trigger-button.spinning .trigger-icon {
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

.el-table thead th {
  background-color: #f3f4f6 !important;
  border-bottom: 1px solid #e5e7eb !important;
  padding: 12px 16px !important;
  text-align: left !important;
  font-size: 0.875rem !important;
  font-weight: 500 !important;
  color: #4b5563 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.05em !important;
}

.el-table tbody td {
  padding: 12px 16px !important;
  font-size: 0.875rem !important;
  color: #374151 !important;
  border-bottom: 1px solid #e5e7eb !important;
}

.el-table tbody tr:hover > td {
  background-color: #f9fafb !important;
}

.el-table .opacity-60 {
    opacity: 0.6;
}

.text-success {
  color: #10b981;
}
.text-danger {
  color: #ef4444;
}

</style> 