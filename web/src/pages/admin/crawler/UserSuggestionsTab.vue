<template>
  <div>
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-700">用户建议审核</h3>
      <!-- Add filtering options here if needed -->
    </div>

    <SuggestionTable
      :suggestions="suggestions"
      :loading="loading"
      @approve="handleApprove"
      @reject="handleReject"
    />

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
import { ref, onMounted, reactive } from 'vue';
import SuggestionTable from '@/components/admin/crawler/SuggestionTable.vue';
import type { UserSuggestion, SuggestionQueryParam } from '@/types/admin/crawler';
import {
  getUserSuggestions,
  approveUserSuggestion,
  rejectUserSuggestion,
  safeJSONStringify
} from '@/api/admin/crawler';
import { formatJSON } from '@/utils/jsonUtils';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();
const emit = defineEmits(['approve', 'reject', 'show-config-modal']);

const loading = ref(false);
const suggestions = ref<UserSuggestion[]>([]);
const total = ref(0);
const queryParams = reactive<SuggestionQueryParam>({
  pageNum: 1,
  pageSize: 10,
});

const fetchSuggestions = async () => {
  loading.value = true;
  try {
    const pageResult = await getUserSuggestions(queryParams);
    console.log('Received suggestions data:', pageResult);
    
    if (pageResult && pageResult.list) {
      suggestions.value = pageResult.list;
      total.value = pageResult.total || 0;
      console.log('Parsed suggestions:', suggestions.value);
    } else {
      console.error('Unexpected API response format:', pageResult);
      suggestions.value = [];
      total.value = 0;
    }
    
  } catch (error: any) {
    console.error('Failed to fetch suggestions:', error);
    ElMessage.error(error?.message || '获取用户建议时发生错误');
    suggestions.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchSuggestions();
});

const handleApprove = (suggestion: UserSuggestion) => {
  ElMessageBox.confirm(`确定要采纳 "${suggestion.sourceName}" 建议吗？采纳后将跳转到配置页面。`, '确认采纳', {
    confirmButtonText: '确定并配置',
    cancelButtonText: '取消',
    type: 'info',
    customClass: '!rounded-button'
  })
    .then(async () => {
        loading.value = true;
        try {
          const handlerId = 1;
          await approveUserSuggestion(suggestion.id.toString(), handlerId);
          ElMessage.success('建议已采纳，正在配置...');
          
          // 处理并清理URL
          let cleanedUrl = '';
          if (suggestion.suggestionUrl) {
            try {
              // 尝试将URL解析为标准格式
              const url = new URL(suggestion.suggestionUrl);
              cleanedUrl = url.toString();
              console.log('采纳的URL已标准化:', cleanedUrl);
            } catch (e) {
              // 如果不是有效URL，保留原始内容但记录警告
              console.warn('采纳的URL格式可能不正确:', suggestion.suggestionUrl);
              cleanedUrl = suggestion.suggestionUrl;
            }
          }
          
          // 准备要传递的配置数据
          const configData = {
            targetName: suggestion.sourceName,
            targetType: suggestion.suggestionType === 'NEW_SOURCE_POLICY' ? 'policy' : 'competitor',
            sourceUrls: cleanedUrl,
            // 添加其他可能需要的默认值
            isActive: true,
            extractionStrategyType: 'css',
            crawlDepth: 0,
            extractionSchema: formatJSON({}), // 使用formatJSON确保有格式化的JSON字符串
            llmProviderConfig: formatJSON({}),  // 使用formatJSON确保有格式化的JSON字符串
            llmInstruction: '',
            crawl4aiConfigOverride: formatJSON({}), // 使用formatJSON确保有格式化的JSON字符串
            triggerSchedule: '0 0 * * * ?'
          };
          
          // 仅记录必要的信息，避免过大的日志输出
          console.log('准备发送show-config-modal事件，targetName:', configData.targetName);
          console.log('sourceUrls (截取):', configData.sourceUrls.substring(0, 30) + (configData.sourceUrls.length > 30 ? '...' : ''));
          
          // 触发父组件事件，让其打开配置模态框并预填充数据
          emit('show-config-modal', configData);

        } catch (error: any) {
          console.error('Failed to approve suggestion:', error);
          ElMessage.error(error?.message || '采纳建议时发生错误');
        } finally {
          loading.value = false;
        }
      })
    .catch(() => {
      ElMessage.info('已取消操作');
    });
};

const handleReject = (suggestion: UserSuggestion) => {
   ElMessageBox.prompt('请输入拒绝理由：', '确认拒绝', {
    confirmButtonText: '确定拒绝',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '拒绝理由不能为空',
    type: 'warning',
    customClass: '!rounded-button'
  })
  .then(async ({ value: remarks }) => {
      if (!remarks) {
          ElMessage.warning('拒绝理由不能为空');
          return;
      }
      loading.value = true;
      try {
        // TODO: 获取真实的 handlerId，暂时使用 1
        const handlerId = 1;
        await rejectUserSuggestion(suggestion.id.toString(), handlerId, remarks);
        ElMessage.info('建议已拒绝');
        fetchSuggestions(); // Refresh list

      } catch (error: any) {
        console.error('Failed to reject suggestion:', error);
        ElMessage.error(error?.message || '拒绝建议时发生错误');
      } finally {
        loading.value = false;
      }
    })
  .catch(() => {
    // User cancelled or input was invalid (already handled by pattern)
    ElMessage.info('已取消拒绝');
  });
};

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  fetchSuggestions();
};

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val;
  fetchSuggestions();
};

</script>

<style scoped>
/* Add specific styles for this tab container if needed */
</style>
