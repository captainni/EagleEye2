<template>
  <div>
    <NavBar :activeMenu="'/requirement-pool'" :user="userData" />
    
    <main class="max-w-[1200px] mx-auto px-6 pt-24 pb-12">
      <div v-if="loading" class="flex justify-center items-center py-20">
        <i class="fas fa-spinner fa-spin text-2xl text-primary mr-2"></i>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="!requirement" class="bg-white rounded-lg shadow-sm p-8">
        <div class="text-center py-10">
          <p class="text-lg text-gray-600">未找到需求信息</p>
          <router-link to="/requirement-pool" class="text-primary hover:underline mt-4 inline-block">
            返回需求池
          </router-link>
        </div>
      </div>
      
      <div v-else class="bg-white rounded-lg shadow-sm p-8">
        <!-- 需求标题 -->
        <div class="mb-6 pb-4 border-b border-gray-200">
          <h2 class="text-2xl font-semibold text-gray-800 mb-3">{{ requirement.title }}</h2>
        </div>

        <!-- 需求元数据 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4 mb-8 text-sm">
          <dl class="detail-item">
            <dt>状态</dt>
            <dd><StatusTag :status="requirement.statusType" :text="requirement.statusText" /></dd>
          </dl>
          <dl class="detail-item">
            <dt>优先级</dt>
            <dd><PriorityTag :priority="requirement.priorityLevel" :text="requirement.priority" /></dd>
          </dl>
          <dl class="detail-item">
            <dt>来源</dt>
            <dd>
              {{ getSourceTypeText(requirement.sourceType) }}
              <template v-if="requirement.sourceDetail">
                ：
                <router-link 
                  v-if="requirement.sourceType === 'POLICY' && requirement.sourceId"
                  :to="`/policy-detail/${requirement.sourceId}`" 
                  class="text-primary hover:underline"
                >
                  {{ requirement.sourceDetail }}
                </router-link>
                <router-link 
                  v-else-if="requirement.sourceType === 'COMPETITOR' && requirement.sourceId"
                  :to="`/competitor-detail/${requirement.sourceId}`" 
                  class="text-primary hover:underline"
                >
                   {{ requirement.sourceDetail }}
                </router-link>
                <span v-else>{{ requirement.sourceDetail }}</span>
              </template>
            </dd>
          </dl>
          <dl class="detail-item">
            <dt>创建日期</dt>
            <dd>{{ formatDate(requirement.createTime) }}</dd>
          </dl>
          <dl class="detail-item">
            <dt>计划完成</dt>
            <dd>{{ formatDate(requirement.planTime) }}</dd>
          </dl>
          <dl class="detail-item">
            <dt>负责人</dt>
            <dd>{{ requirement.owner || '未分配' }}</dd>
          </dl>
          <dl class="detail-item">
            <dt>完成时间</dt>
            <dd>{{ formatDate(requirement.completeTime) }}</dd>
          </dl>
        </div>

        <!-- 需求背景 -->
        <div v-if="requirement.background" class="mb-8">
          <h4 class="text-base font-semibold text-gray-700 mb-3">需求背景：</h4>
          <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed border border-gray-200 rounded-md p-4 bg-gray-50 whitespace-pre-line">
            {{ requirement.background }}
          </div>
        </div>

        <!-- 详细描述 -->
        <div class="mb-8">
          <h4 class="text-base font-semibold text-gray-700 mb-3">详细描述：</h4>
          <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed border border-gray-200 rounded-md p-4 bg-gray-50 whitespace-pre-line">
            {{ requirement.description }}
          </div>
        </div>

        <!-- 关联信息/附件 -->
        <div v-if="requirement.attachments && requirement.attachments.length > 0" class="mb-6">
          <h4 class="text-base font-semibold text-gray-700 mb-3">关联信息/附件：</h4>
          <div class="border border-gray-200 rounded-md p-4 text-sm">
            <ul class="list-disc list-inside space-y-1 text-gray-700">
              <li v-for="attachment in requirement.attachments" :key="attachment.id">
                <a :href="attachment.url" target="_blank" class="text-primary hover:underline">
                  <i :class="'fas fa-' + attachment.type + ' mr-1'"></i> {{ attachment.name }}
                </a>
              </li>
            </ul>
            <button class="!rounded-button mt-3 px-3 py-1 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-xs whitespace-nowrap">
              <i class="fas fa-paperclip mr-1"></i> 上传附件 (未实现)
            </button>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-end space-x-3 mt-8 pt-4 border-t border-gray-200">
          <router-link 
            to="/requirement-pool" 
            class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
          >
            <i class="fas fa-arrow-left mr-1"></i> 返回列表
          </router-link>
          <router-link 
            :to="`/requirement-edit/${requirement.id}`" 
            class="!rounded-button px-4 py-2 bg-primary text-white text-sm whitespace-nowrap hover:bg-primary/90"
          >
            <i class="fas fa-edit mr-1"></i> 编辑需求
          </router-link>
          <button 
            class="!rounded-button px-4 py-2 border border-danger text-danger hover:bg-danger/10 text-sm whitespace-nowrap"
            @click="handleDelete"
            v-if="requirement.status !== 'REJECTED' && requirement.status !== 'COMPLETED'" 
          >
            <i class="fas fa-trash mr-1"></i> 删除需求
          </button>
          <!-- <button 
            class="!rounded-button px-4 py-2 border border-gray-500 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
            @click="closeRequirement" 
            v-if="requirement.status !== 'REJECTED' && requirement.status !== 'COMPLETED'" 
          >
            <i class="fas fa-times-circle mr-1"></i> 关闭需求 (未实现)
          </button> -->
        </div>
      </div>
    </main>
    
    <Footer copyright="© 2024 金融资讯智能跟踪平台. All rights reserved." />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import NavBar from '@/components/common/NavBar.vue';
import Footer from '@/components/common/Footer.vue';
import StatusTag from '@/components/requirement/StatusTag.vue';
import PriorityTag from '@/components/requirement/PriorityTag.vue';
import { getRequirementDetail, deleteRequirement } from '@/services/requirementService';
import { defaultUserData } from '@/utils/defaultData';
import type { RequirementDetailVO } from '@/types/requirement';

const route = useRoute();
const router = useRouter();
const userData = ref(defaultUserData);
const requirement = ref<RequirementDetailVO | null>(null);
const loading = ref(true);

// 获取需求详情
const fetchRequirementDetail = async () => {
  loading.value = true;
  try {
    const id = Number(route.params.id);
    if (isNaN(id)) {
        throw new Error('无效的需求ID');
    }
    const response = await getRequirementDetail(id);
    if (response.code === 200 && response.data) {
      requirement.value = response.data;
    } else {
      ElMessage.error(response.message || '获取需求详情失败');
      requirement.value = null;
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取需求详情失败');
    requirement.value = null;
  } finally {
    loading.value = false;
  }
};

// 日期格式化函数
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch (e) {
    console.error('Error formatting date:', e);
    return '-';
  }
};

// 获取来源类型文本
const getSourceTypeText = (sourceType?: string): string => {
    if (sourceType === 'POLICY') return '监管政策';
    if (sourceType === 'COMPETITOR') return '竞品动态';
    if (sourceType === 'MANUAL') return '手动创建';
    return '未知来源';
};

// 删除需求
const handleDelete = async () => {
  if (!requirement.value) return;
  try {
    await ElMessageBox.confirm(
      '确定要删除此需求吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    loading.value = true;
    const response = await deleteRequirement(requirement.value.id);
    if (response.code === 200) {
      ElMessage.success('删除成功');
      router.push('/requirement-pool'); // 删除成功后返回列表页
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除操作失败');
    }
  } finally {
      loading.value = false;
  }
};

// 关闭需求 (暂未实现)
// const closeRequirement = () => {
//   // ... 实现关闭需求的逻辑 ...
// };

onMounted(() => {
  fetchRequirementDetail();
});
</script>

<style scoped>
.detail-item {
  display: flex;
  margin-bottom: 0.75rem; /* 12px */
}
.detail-item dt {
  width: 6rem; /* 96px */
  flex-shrink: 0;
  color: #6b7280; /* gray-500 */
  font-weight: 500; /* medium */
}
.detail-item dd {
  color: #374151; /* gray-700 */
}
</style> 