<template>
  <div>
    <NavBar :activeMenu="'/requirement-pool'" :user="userData" />
    
    <main class="max-w-[1200px] mx-auto px-6 pt-24 pb-12">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold text-gray-800">添加新需求</h2>
        <router-link 
          to="/requirement-pool" 
          class="!rounded-button px-3 py-1 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm whitespace-nowrap"
        >
          <i class="fas fa-arrow-left mr-1"></i> 返回列表
        </router-link>
      </div>
      
      <RequirementForm @submit="handleSubmit" :loading="loading" />
    </main>
    
    <Footer copyright="© 2026 金融资讯智能跟踪平台. All rights reserved." />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import NavBar from '@/components/common/NavBar.vue';
import Footer from '@/components/common/Footer.vue';
import RequirementForm from '@/components/requirement/RequirementForm.vue';
import { createRequirement } from '@/services/requirementService';
import { defaultUserData } from '@/utils/defaultData';
import type { RequirementCreateDTO } from '@/types/requirement';

const router = useRouter();
const userData = ref(defaultUserData);
const loading = ref(false);

const handleSubmit = async (formData: RequirementCreateDTO) => {
  loading.value = true;
  try {
    // 实际应用中 userId 可能从用户信息中获取
    const dataToSubmit = { ...formData, userId: 1 }; // 暂时硬编码userId=1
    const response = await createRequirement(dataToSubmit);
    if (response.code === 200) {
      ElMessage.success('需求添加成功');
      router.push('/requirement-pool'); // 成功后跳转回列表页
    } else {
      ElMessage.error(response.message || '添加失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '添加操作失败');
  } finally {
    loading.value = false;
  }
};
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