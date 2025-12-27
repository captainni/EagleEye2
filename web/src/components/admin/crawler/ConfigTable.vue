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

      <el-table-column prop="targetType" label="目标类型" min-width="100">
        <template #default="{ row }">
          <TypeTag :type="row.targetType" />
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
            <el-tooltip :content="row.isActive ? '禁用' : '启用'" placement="top">
              <el-button link :type="row.isActive ? 'warning' : 'success'" @click="$emit('toggleStatus', row.configId, !row.isActive)" class="p-1 text-xs">
                <i :class="row.isActive ? 'fas fa-toggle-off' : 'fas fa-toggle-on'"></i>
              </el-button>
            </el-tooltip>
             <el-tooltip content="立即触发" placement="top">
               <el-button link type="info" @click="$emit('trigger', row.configId)" :disabled="!row.isActive" class="p-1 text-xs">
                  <i class="fas fa-play-circle"></i>
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

defineProps({
  configs: {
    type: Array as PropType<CrawlerConfigVO[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['edit', 'delete', 'toggleStatus', 'trigger']);

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

</script>

<style>
.el-table .el-button.p-1 {
    padding: 0.25rem;
    font-size: 1rem;
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