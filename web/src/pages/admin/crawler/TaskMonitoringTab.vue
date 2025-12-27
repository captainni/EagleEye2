<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-700">任务执行监控</h3>
       <div class="flex space-x-2">
         <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期时间"
            end-placeholder="结束日期时间"
            class="!rounded-button text-sm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
         <el-select v-model="queryParams.status" placeholder="所有状态" clearable class="!rounded-button text-sm w-32">
             <el-option label="成功" value="success"></el-option>
             <el-option label="失败" value="failure"></el-option>
         </el-select>
         <el-button type="primary" @click="handleQuery" :loading="loading" class="!rounded-button">查询</el-button>
         <el-button type="warning" @click="testApiCall" class="!rounded-button ml-2">测试API</el-button>
       </div>
    </div>

    <TaskTable :tasks="tasks" :loading="loading" />

     <!-- Pagination -->
     <el-pagination
        v-if="total > 0"
        class="mt-4 justify-end"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParams.pageSize"
        :current-page="queryParams.pageNum"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import TaskTable from '@/components/admin/crawler/TaskTable.vue';
import type { CrawlerTaskLog, TaskLogQueryParam } from '@/types/admin/crawler';
import { getCrawlerTaskLogs } from '@/api/admin/crawler';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const tasks = ref<CrawlerTaskLog[]>([]);
const total = ref(0);
const dateRange = ref<[string, string] | null>(null);

const queryParams = reactive<TaskLogQueryParam>({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
  startTime: undefined,
  endTime: undefined,
});

// Update queryParams based on dateRange
const queryParamsWithDate = computed(() => {
  const params: TaskLogQueryParam = { ...queryParams };
  if (dateRange.value) {
    params.startTime = dateRange.value[0];
    params.endTime = dateRange.value[1];
  } else {
    params.startTime = undefined;
    params.endTime = undefined;
  }
  return params;
});

const fetchTasks = async () => {
  loading.value = true;
  try {
    // response 现在直接是后端返回的 data 部分 (PaginatedResult<CrawlerTaskLog>)
    const responseData = await getCrawlerTaskLogs(queryParamsWithDate.value);
    
    // --- DEBUGGING --- 
    console.log('Response data from getCrawlerTaskLogs:', responseData);
    // --- END DEBUGGING ---

    // 直接使用 responseData，它应该包含 list 和 total 字段
    // 根据 Linter 提示，字段确认是 list 和 total
    if (responseData && responseData.list && typeof responseData.total === 'number') {
      tasks.value = responseData.list;
      total.value = responseData.total;
    } else {
        // 如果 responseData 结构不符合预期，也报个错
        console.error('Unexpected response data structure:', responseData);
        ElMessage.error('获取任务日志失败：响应数据格式不正确');
        tasks.value = []; // 清空数据
        total.value = 0;
    }

  } catch (error) {
    // 网络错误或 code !== 200 的情况由拦截器处理了 ElMessage
    console.error('Failed to fetch task logs:', error);
    // 这里可以不再重复显示 ElMessage，或者显示一个更通用的错误
    tasks.value = []; // 清空数据
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTasks();
});

const handleQuery = () => {
  queryParams.pageNum = 1; // Reset to first page on query
  fetchTasks();
};

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  fetchTasks();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  fetchTasks();
};

// 测试API调用函数
const testApiCall = async () => {
  console.log('==== 测试API调用开始 ====');
  console.log('查询参数:', queryParamsWithDate.value);
  
  try {
    // 直接使用fetch调用，绕过apiClient
    // 注意：由于vite.config.ts配置了代理/api -> http://localhost:9090，
    // 所以这里使用/api/v1/...路径会被重写为http://localhost:9090/v1/...
    const response = await fetch('/api/v1/admin/crawler/tasks?pageNum=1&pageSize=10', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    
    console.log('API响应状态:', response.status);
    const data = await response.json();
    console.log('API响应数据:', data);
    
    if (data && data.code === 200 && data.data) {
      console.log('API响应数据中的data部分:', data.data);
      console.log('data.list存在吗?', Boolean(data.data.list));
      console.log('data.total存在吗?', typeof data.data.total === 'number');
    }
  } catch (error) {
    console.error('测试API调用失败:', error);
  }
  
  console.log('==== 测试API调用结束 ====');
};

</script>

<style scoped>
/* Ensure date picker and select match the button style */
.el-date-editor,
.el-select {
  height: 32px; /* Match button height py-1.5 + border */
}
.el-select .el-input {
    height: 30px; /* Inner input height adjustment */
}
.el-input__wrapper {
    border-radius: 4px !important; /* !rounded-button */
}
</style>
