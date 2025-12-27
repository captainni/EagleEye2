<template>
  <div>
    <NavBar :activeMenu="'/requirement-pool'" :user="userData" />
    
    <main class="max-w-[1200px] mx-auto px-6 pt-24 pb-12">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold text-gray-800">编辑需求</h2>
        <router-link 
          to="/requirement-pool" 
          class="!rounded-button px-3 py-1 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
        >
          <i class="fas fa-arrow-left mr-1"></i> 返回列表
        </router-link>
      </div>
      
      <div v-if="initialLoading" class="flex justify-center items-center py-20">
        <i class="fas fa-spinner fa-spin text-2xl text-primary mr-2"></i>
        <span>加载需求数据...</span>
      </div>
      <div v-else-if="!requirement" class="bg-white rounded-lg shadow-sm p-8 text-center">
        <p class="text-lg text-gray-600">未找到需要编辑的需求信息</p>
      </div>
      <RequirementForm 
        v-else
        :initial-data="requirement" 
        :is-edit="true" 
        @submit="handleSubmit" 
        :loading="submitLoading"
      />
    </main>
    
    <Footer copyright="© 2024 金融资讯智能跟踪平台. All rights reserved." />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import NavBar from '@/components/common/NavBar.vue';
import Footer from '@/components/common/Footer.vue';
import RequirementForm from '@/components/requirement/RequirementForm.vue';
import { getRequirementDetail, updateRequirement } from '@/services/requirementService';
import { defaultUserData } from '@/utils/defaultData';
import type { RequirementDetailVO, RequirementUpdateDTO } from '@/types/requirement';

const route = useRoute();
const router = useRouter();
const userData = ref(defaultUserData);
const requirement = ref<RequirementDetailVO | null>(null);
const initialLoading = ref(true);
const submitLoading = ref(false);

// 获取需求详情用于表单初始化
const fetchRequirementDetail = async () => {
  initialLoading.value = true;
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
    initialLoading.value = false;
  }
};

const handleSubmit = async (formData: RequirementUpdateDTO) => {
  submitLoading.value = true;
  try {
    const id = Number(route.params.id);
    if (isNaN(id)) {
        throw new Error('无效的需求ID');
    }
    const response = await updateRequirement(id, formData);
    if (response.code === 200) {
      ElMessage.success('需求更新成功');
      router.push(`/requirement-detail/${id}`); // 成功后跳转回详情页
    } else {
      ElMessage.error(response.message || '更新失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '更新操作失败');
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  fetchRequirementDetail();
});
</script>

<style scoped>
.form-label {
  display: block;
  margin-bottom: 0.25rem; /* 4px */
  font-size: 0.875rem; /* 14px */
  font-weight: 500; /* medium */
  color: #374151; /* gray-700 */
}
.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.5rem 0.75rem; /* 8px 12px */
  border: 1px solid #d1d5db; /* gray-300 */
  border-radius: 4px; /* rounded-button */
  font-size: 0.875rem; /* 14px */
  line-height: 1.25rem; /* 20px */
  background-color: #fff;
}
.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
  border-color: #3b82f6; /* primary */
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5); /* ring-primary */
}

/* 自定义下拉箭头 */
select {
  appearance: none;
  background-image: url('data:image/svg+xml;charset=UTF-8,%3csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="none" stroke="%236b7280" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"%3e%3cpath d="M6 8l4 4 4-4"/%3e%3c/svg%3e');
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 2.5rem;
}
</style> 