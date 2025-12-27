<template>
  <div>
    <NavBar :activeMenu="'/requirement-pool'" :user="userData" />
    
    <main class="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
      <!-- 筛选与操作区域 -->
      <RequirementFilter @search="handleSearch" @reset="resetFilters" />
      
      <!-- 需求列表区域 -->
      <RequirementTable 
        :requirements="requirements" 
        :loading="loading"
        @view="viewRequirement"
        @edit="editRequirement"
        @delete="handleDelete"
        @select="handleSelect"
      />
      
      <!-- 分页控件 -->
      <div class="mt-8 flex justify-center">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </main>
    
    <Footer copyright="© 2024 金融资讯智能跟踪平台. All rights reserved." />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import NavBar from '@/components/common/NavBar.vue';
import Footer from '@/components/common/Footer.vue';
import RequirementFilter from '@/components/requirement/RequirementFilter.vue';
import RequirementTable from '@/components/requirement/RequirementTable.vue';
import { listRequirements, deleteRequirement } from '@/services/requirementService';
import { defaultUserData } from '@/utils/defaultData';
import type { RequirementVO, RequirementQueryDTO } from '@/types/requirement';

const router = useRouter();
const userData = ref(defaultUserData);
const requirements = ref<RequirementVO[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const selectedIds = ref<number[]>([]);

// 过滤条件
const filters = ref<RequirementQueryDTO>({
  keyword: '',
  status: '',
  priority: '',
  sourceType: ''
});

// 获取需求列表数据
const fetchRequirements = async () => {
  loading.value = true;
  try {
    const params: RequirementQueryDTO = {
      ...filters.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };
    // 清理空字符串参数，避免传给后端
    Object.keys(params).forEach(key => {
      if (params[key as keyof RequirementQueryDTO] === '') {
        delete params[key as keyof RequirementQueryDTO];
      }
    });
    
    const response = await listRequirements(params);
    if (response.code === 200 && response.data) {
      requirements.value = response.data.list;
      total.value = response.data.total;
    } else {
      ElMessage.error(response.message || '获取需求列表失败');
      requirements.value = [];
      total.value = 0;
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取需求列表失败');
    requirements.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = (searchFilters: RequirementQueryDTO) => {
  filters.value = { ...searchFilters };
  currentPage.value = 1;
  fetchRequirements();
};

// 重置过滤器
const resetFilters = () => {
  filters.value = {
    keyword: '',
    status: '',
    priority: '',
    sourceType: ''
  };
  currentPage.value = 1;
  fetchRequirements();
};

// 处理分页
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchRequirements();
};

// 处理表格选择
const handleSelect = (ids: number[]) => {
  selectedIds.value = ids;
};

// 查看需求详情
const viewRequirement = (id: number) => {
  router.push(`/requirement-detail/${id}`);
};

// 编辑需求
const editRequirement = (id: number) => {
  router.push(`/requirement-edit/${id}`);
};

// 删除需求
const handleDelete = async (id: number) => {
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
    
    loading.value = true; // 开始删除操作，显示加载状态
    const response = await deleteRequirement(id);
    if (response.code === 200) {
      ElMessage.success('删除成功');
      // 如果删除的是当前页最后一条，且不是第一页，则跳转到前一页
      if (requirements.value.length === 1 && currentPage.value > 1) {
         currentPage.value -= 1;
      }
      fetchRequirements(); // 重新加载列表
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') { // 用户取消操作时不提示错误
      ElMessage.error(error.message || '删除操作失败');
    }
  } finally {
      loading.value = false; // 结束删除操作，隐藏加载状态
  }
};

// 监听过滤条件和页码变化
watch([filters, currentPage], fetchRequirements, { deep: true });

// 组件挂载时获取初始数据
onMounted(() => {
  fetchRequirements();
});
</script>

<style scoped>
/* 组件特定样式 */
</style>