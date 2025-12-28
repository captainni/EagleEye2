<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-700">爬虫配置列表</h3>
      <el-button type="primary" @click="openAddModal" class="!rounded-button">
        <i class="fas fa-plus mr-2"></i>新增配置
      </el-button>
    </div>

    <ConfigTable
      :configs="configs"
      :loading="loading"
      :runningTaskIds="runningTaskIds"
      :completedTaskIds="completedTaskIds"
      @edit="openEditModal"
      @delete="handleDelete"
      @toggle-status="handleToggleStatus"
      @trigger="handleTrigger"
    />

    <el-pagination
        v-if="pagination.total > 0"
        class="mt-4 justify-end"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        v-model:page-size="pagination.pageSize"
        v-model:current-page="pagination.currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

    <ConfigModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :initial-data="currentConfig"
      @save="handleSave"
      @close="handleModalClose"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, defineExpose, nextTick } from 'vue';
import ConfigTable from '@/components/admin/crawler/ConfigTable.vue';
import ConfigModal from '@/components/admin/crawler/ConfigModal.vue';
import type {
  CrawlerConfigVO,
  CrawlerConfigQueryParam,
  CrawlerConfigCreateData,
  CrawlerConfigUpdateData,
  CrawlerConfigDetailVO
} from '@/types/admin/crawler';
import {
  getCrawlerConfigs,
  createCrawlerConfig,
  updateCrawlerConfig,
  deleteCrawlerConfig,
  updateCrawlerConfigStatus,
  triggerCrawlerConfig,
  getCrawlerConfigDetail,
  safeJSONStringify,
  getTaskStatus,
  type TriggerResult,
  type TaskStatus
} from '@/api/admin/crawler';
import { formatJSON } from '@/utils/jsonUtils';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { PaginatedResult } from '@/types/admin/crawler';

const loading = ref(false);
const configs = ref<CrawlerConfigVO[]>([]);
const runningTaskIds = ref<number[]>([]); // 正在执行的任务配置ID列表
const completedTaskIds = ref<number[]>([]); // 已完成的任务配置ID列表
const queryParams = reactive<CrawlerConfigQueryParam>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  targetType: undefined,
  isActive: undefined,
});
const pagination = reactive({
  total: 0,
  currentPage: 1,
  pageSize: 10
});

const modalVisible = ref(false);
const modalMode = ref<'add' | 'edit'>('add');
const currentConfig = ref<CrawlerConfigDetailVO | null>(null);

const fetchConfigs = async () => {
  loading.value = true;
  queryParams.pageNum = pagination.currentPage;
  queryParams.pageSize = pagination.pageSize;
  try {
    const result: PaginatedResult<CrawlerConfigVO> = await getCrawlerConfigs(queryParams);
    configs.value = result.list;
    pagination.total = result.total;
  } catch (error: any) {
    console.error('Failed to fetch configs:', error);
    ElMessage.error(error?.message || error?.msg || '获取配置列表时发生错误');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchConfigs();
});

const openAddModal = () => {
  modalMode.value = 'add';
  currentConfig.value = null;
  modalVisible.value = true;
};

const openAddModalWithData = (configData: any) => {
  console.log('openAddModalWithData called with data:', configData);
  
  // 首先设置模态框模式
  modalMode.value = 'add';
  
  // 确保先设置 modalVisible，再设置 currentConfig
  modalVisible.value = true;
  
  // 使用 nextTick 确保 DOM 更新后再设置配置数据
  nextTick(() => {
    // 处理sourceUrls，确保是字符串格式
    const sourceUrlsValue = typeof configData.sourceUrls === 'string' 
      ? configData.sourceUrls 
      : (Array.isArray(configData.sourceUrls) 
          ? configData.sourceUrls.join('\n') 
          : '');
    
    // 使用JSON处理函数确保JSON字段格式正确，并且进行格式化美化
    const jsonFields = {
      llmProviderConfig: configData.llmProviderConfig || {},
      extractionSchema: configData.extractionSchema || {},
      crawl4aiConfigOverride: configData.crawl4aiConfigOverride || {}
    };
    
    // 处理每个JSON字段，保证格式正确和易读性
    Object.keys(jsonFields).forEach(key => {
      jsonFields[key as keyof typeof jsonFields] = 
        formatJSON(jsonFields[key as keyof typeof jsonFields]);
    });
    
    // 为了解决TypeScript类型问题，先创建对象再赋值
    const configObject: any = {
      // 必须包含这些字段以兼容现有表单
      targetName: configData.targetName || '',
      targetType: configData.targetType || 'policy',
      sourceUrls: sourceUrlsValue,
      extractionStrategyType: configData.extractionStrategyType || 'css',
      extractionSchema: jsonFields.extractionSchema,
      isActive: configData.isActive !== undefined ? configData.isActive : true,
      triggerSchedule: configData.triggerSchedule || '0 0 * * * ?',
      crawlDepth: configData.crawlDepth !== undefined ? configData.crawlDepth : 0,
      crawl4aiConfigOverride: jsonFields.crawl4aiConfigOverride,
      llmProviderConfig: jsonFields.llmProviderConfig
    };
    
    // 仅在需要时添加llmInstruction字段
    if (configData.extractionStrategyType === 'llm') {
      configObject.llmInstruction = configData.llmInstruction || '';
    }
    
    // 设置配置对象
    currentConfig.value = configObject;
    
    console.log('Modal should be visible now. modalVisible =', modalVisible.value);
    console.log('Current config set to:', currentConfig.value);
  });
};

const openEditModal = async (config: CrawlerConfigVO) => {
  loading.value = true;
  try {
    const detailData = await getCrawlerConfigDetail(config.configId);
    modalMode.value = 'edit';
    currentConfig.value = detailData;
    modalVisible.value = true;
  } catch (error: any) {
     console.error('Failed to fetch config details:', error);
     ElMessage.error(error?.message || error?.msg || '获取配置详情失败');
  } finally {
     loading.value = false;
  }
};

const handleModalClose = () => {
  modalVisible.value = false;
  currentConfig.value = null;
};

const handleSave = async (saveData: CrawlerConfigCreateData | CrawlerConfigUpdateData) => {
  const isAdding = modalMode.value === 'add';
  
  // 创建数据副本，避免修改原始对象
  const formattedData: any = { ...saveData }; // 使用any类型避免TypeScript错误
  
  // 不管是创建还是编辑，都确保移除不必要的字段，避免后端API错误
  // 这些字段只用于前端显示，不应该提交给后端
  const fieldsToRemove = ['configId', 'createTime', 'updateTime'];
  
  fieldsToRemove.forEach(field => {
    if (field in formattedData) {
      delete formattedData[field];
      console.log(`已移除字段: ${field}`);
    }
  });
  
  // 如果不是 LLM 策略，移除 llmInstruction 字段
  if (formattedData.extractionStrategyType !== 'llm') {
    delete formattedData.llmInstruction;
  }
  
  // 处理sourceUrls字段格式
  if (typeof formattedData.sourceUrls === 'string') {
    // 打印日志
    if (formattedData.sourceUrls) {
      console.log('保存前的sourceUrls:', formattedData.sourceUrls);
    }
  }
  
  // 打印日志前确保对象可以安全序列化
  try {
    console.log('提交的配置数据:', formattedData);
  } catch (err) {
    console.error('提交的配置数据无法打印:', err);
  }
  
  // 根据模式选择调用的API
  const apiCall = isAdding
    ? createCrawlerConfig(formattedData as CrawlerConfigCreateData)
    : updateCrawlerConfig(currentConfig.value!.configId, formattedData as CrawlerConfigUpdateData);
  
  try {
    await apiCall;
    ElMessage.success(isAdding ? '配置已创建' : '配置已更新');
    modalVisible.value = false;
    // 重新加载列表
    await fetchConfigs();
  } catch (error) {
    console.error('Failed to save config:', error);
    ElMessage.error(`保存失败: ${error}`);
  }
};

const handleDelete = (configId: number) => {
   const configToDelete = configs.value.find(c => c.configId === configId);
   const confirmMessage = configToDelete
     ? `确定要删除配置 "${configToDelete.targetName}" 吗？`
     : '确定要删除此配置吗？';

   ElMessageBox.confirm(confirmMessage, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: '!rounded-button'
  })
    .then(async () => {
      loading.value = true;
      try {
        await deleteCrawlerConfig(configId);
        ElMessage.success('删除成功');
        fetchConfigs();
      } catch (error: any) {
         console.error('Failed to delete config:', error);
         ElMessage.error(error?.message || error?.msg || '删除失败');
      } finally {
        loading.value = false;
      }
    }).catch(() => { /* User cancelled */ });
};

const handleToggleStatus = async (configId: number, newStatus: boolean) => {
  loading.value = true;
  try {
    await updateCrawlerConfigStatus(configId, newStatus);
    ElMessage.success('状态更新成功');
    fetchConfigs();
  } catch (error: any) {
     console.error('Failed to toggle config status:', error);
     ElMessage.error(error?.message || error?.msg || '状态更新失败');
  } finally {
    loading.value = false;
  }
};

const handleTrigger = async (configId: number) => {
  try {
    const result: TriggerResult = await triggerCrawlerConfig(configId);

    if (result.success) {
      if (result.taskId) {
        // EagleEye 爬虫，返回了 taskId，开始轮询
        ElMessage.info(result.message || '任务已开始，正在后台执行...');

        // 添加到运行中列表
        runningTaskIds.value = [...runningTaskIds.value, configId];
        // 从已完成列表移除
        completedTaskIds.value = completedTaskIds.value.filter(id => id !== configId);

        // 开始轮询任务状态
        let pollCount = 0;
        const maxPolls = 100; // 最多轮询 100 次（5 分钟）
        const pollInterval = setInterval(async () => {
          pollCount++;

          try {
            const status: TaskStatus = await getTaskStatus(result.taskId!);

            if (status.status === 'success') {
              clearInterval(pollInterval);
              // 从运行中移除，添加到已完成
              runningTaskIds.value = runningTaskIds.value.filter(id => id !== configId);
              completedTaskIds.value = [...completedTaskIds.value, configId];

              ElMessage.success(`爬取完成！成功爬取 ${status.articleCount || 0} 篇文章`);
              // 刷新列表以更新 resultPath
              fetchConfigs();

              // 3秒后移除完成状态
              setTimeout(() => {
                completedTaskIds.value = completedTaskIds.value.filter(id => id !== configId);
              }, 3000);
            } else if (status.status === 'failure') {
              clearInterval(pollInterval);
              // 从运行中移除
              runningTaskIds.value = runningTaskIds.value.filter(id => id !== configId);
              ElMessage.error(`爬取失败: ${status.errorMessage || '未知错误'}`);
            } else if (pollCount >= maxPolls) {
              clearInterval(pollInterval);
              // 从运行中移除
              runningTaskIds.value = runningTaskIds.value.filter(id => id !== configId);
              ElMessage.warning('任务执行超时，请稍后查看任务状态');
            }
          } catch (error) {
            clearInterval(pollInterval);
            // 从运行中移除
            runningTaskIds.value = runningTaskIds.value.filter(id => id !== configId);
            console.error('Failed to poll task status:', error);
            ElMessage.error('查询任务状态失败');
          }
        }, 3000); // 每 3 秒轮询一次
      } else {
        // 传统爬虫，无 taskId
        ElMessage.success(result.message || '任务已发送到队列');
      }
    } else {
      ElMessage.error(result.message || '触发任务失败');
    }
  } catch (error: any) {
    console.error('Failed to trigger config task:', error);
    ElMessage.error(error?.message || error?.msg || '触发任务失败');
  }
};

const handleSizeChange = (val: number) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchConfigs();
};

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val;
  fetchConfigs();
};

// 显式暴露方法，使其可以从父组件访问
defineExpose({
  openAddModalWithData
});

</script>

<style scoped>
/* Add specific styles for this tab container if needed */
</style> 