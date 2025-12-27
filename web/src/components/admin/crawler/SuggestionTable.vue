<template>
  <div class="bg-white rounded-lg shadow-sm overflow-x-auto">
    <el-table
      :data="suggestions"
      style="width: 100%"
      v-loading="loading"
      row-key="id"
    >
      <el-table-column prop="userId" label="建议用户ID" min-width="100" />
      <el-table-column prop="createdAt" label="建议时间" width="180">
          <template #default="{ row }">
              <span>{{ formatDateTime(row.createdAt) }}</span>
          </template>
      </el-table-column>

      <el-table-column prop="suggestionType" label="类型" width="100">
         <template #default="{ row }">
            <TypeTag :type="row.suggestionType || ''" />
         </template>
      </el-table-column>

      <el-table-column prop="sourceName" label="建议名称" min-width="150" />

       <el-table-column prop="suggestionUrl" label="建议URL" min-width="200">
        <template #default="{ row }">
          <el-tooltip :content="row.suggestionUrl" placement="top" :show-after="500">
             <span class="truncate max-w-xs inline-block">{{ row.suggestionUrl }}</span>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column prop="remarks" label="理由" min-width="180">
         <template #default="{ row }">
            <span>{{ row.remarks }}</span>
         </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
           <StatusTag :status="row.status" />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <div class="space-x-1 whitespace-nowrap">
            <template v-if="row.status && row.status.toUpperCase() === 'PENDING'">
               <el-tooltip content="采纳并配置" placement="top">
                  <el-button link type="primary" @click="$emit('approve', row)" class="p-1 text-xs">
                     <i class="fas fa-check"></i>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="拒绝" placement="top">
                   <el-button link type="danger" @click="$emit('reject', row)" class="p-1 text-xs">
                     <i class="fas fa-times"></i>
                   </el-button>
                </el-tooltip>
            </template>
            <template v-else-if="row.status && row.status.toUpperCase() === 'APPROVED'">
               <el-tooltip content="已处理" placement="top">
                  <el-button link disabled class="p-1 text-gray-400 cursor-not-allowed text-xs">
                     <i class="fas fa-check-double"></i>
                  </el-button>
                </el-tooltip>
            </template>
            <template v-else-if="row.status && row.status.toUpperCase() === 'REJECTED'">
               <el-tooltip content="已处理" placement="top">
                  <el-button link disabled class="p-1 text-gray-400 cursor-not-allowed text-xs">
                      <i class="fas fa-ban"></i>
                  </el-button>
                </el-tooltip>
            </template>
          </div>
        </template>
      </el-table-column>
      <template #empty>
         <el-empty description="暂无用户建议数据"></el-empty>
      </template>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';
import type { UserSuggestion } from '@/types/admin/crawler';
import StatusTag from './StatusTag.vue';
import TypeTag from './TypeTag.vue';

defineProps({
  suggestions: {
    type: Array as PropType<UserSuggestion[]>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['approve', 'reject']);

function formatDateTime(dateTime: string | Date | null | undefined): string {
  if (!dateTime) {
    return '-';
  }
  try {
    const date = new Date(dateTime);
    if (isNaN(date.getTime())) {
      return '-'; // Invalid date
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error("Error formatting date-time:", error);
    return '-'; // Return fallback on error
  }
}
</script>

<style>
/* Use styles defined in ConfigTable.vue for consistency */
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