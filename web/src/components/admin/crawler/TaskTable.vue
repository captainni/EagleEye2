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

      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
           <span :class="row.status === 'success' ? 'text-success' : 'text-danger'">
               {{ row.status === 'success' ? '成功' : '失败' }}
           </span>
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
      <template #empty>
         <el-empty description="暂无任务日志数据"></el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';
import type { CrawlerTaskLog } from '@/types/admin/crawler';
import StatusTag from './StatusTag.vue';

defineProps({
  tasks: {
    type: Array as PropType<CrawlerTaskLog[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});
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
</style> 