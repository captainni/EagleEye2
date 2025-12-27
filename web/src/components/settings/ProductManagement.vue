<template>
  <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4 pb-4 border-b border-gray-200">我的产品管理</h3>
    <p class="text-sm text-gray-500 mb-4">添加您负责的产品信息，有助于系统提供更相关的分析建议。</p>

    <div v-if="loading" class="text-center text-gray-500">
      加载中...
    </div>
    <div v-if="!loading && error" class="text-center text-red-500">
      加载失败: {{ error }}
    </div>

    <div v-if="!loading && !error">
      <div class="mb-4 space-y-2" id="my-products-list">
        <div v-if="products.length === 0" class="text-sm text-gray-500 text-center py-4">暂无产品，请添加。</div>
        <!-- Product Item -->
        <div v-for="(product, index) in products" :key="product.id"
             class="product-item flex items-center gap-4 bg-gray-50 p-4 rounded-md hover:bg-gray-100 transition-colors"
             :data-product-id="product.id">
          <div class="flex-1 min-w-0">
            <p class="product-name text-sm font-medium text-gray-900 mb-1">{{ product.name }}</p>
            <p class="product-type text-xs text-gray-600 mb-1">类型：{{ product.type }}</p>
            <p class="product-features text-xs text-gray-500">
              <span class="font-medium">特点：</span>
              <span class="truncate-inline" :title="product.features || '无'">
                {{ product.features || '无' }}
              </span>
            </p>
          </div>
          <div class="flex-shrink-0 flex items-center gap-3 w-24 justify-end">
            <button @click="openEditModal(product)"
                    class="edit-product-btn text-sm text-blue-600 hover:text-blue-800 hover:bg-blue-50 px-3 py-1.5 rounded transition-colors">
              <i class="fas fa-edit mr-1"></i>编辑
            </button>
            <button @click="confirmDeleteProduct(product.id, index)"
                    class="delete-product-btn text-sm text-red-600 hover:text-red-800 hover:bg-red-50 px-3 py-1.5 rounded transition-colors">
              <i class="fas fa-trash-alt mr-1"></i>删除
            </button>
          </div>
        </div>
      </div>
      <button @click="openAddModal" class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:border-primary hover:text-primary text-sm">
          <i class="fas fa-plus mr-1"></i> 添加我的产品
      </button>
    </div>

    <!-- Add Product Modal -->
    <BaseModal :is-open="isAddModalOpen" @close="closeAddModal" title="添加我的产品">
       <form @submit.prevent="submitAddProduct">
         <div class="mb-4">
            <label for="addProductName" class="block text-sm font-medium text-gray-700 mb-1">产品名称 <span class="text-red-500">*</span></label>
            <input type="text" id="addProductName" v-model="newProduct.name" required class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm">
         </div>
         <div class="mb-4">
            <label for="addProductType" class="block text-sm font-medium text-gray-700 mb-1">产品类型 <span class="text-red-500">*</span></label>
            <input type="text" id="addProductType" v-model="newProduct.type" required placeholder="例如：信贷产品 或 信用卡" class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm">
         </div>
         <div class="mb-4">
             <label for="addProductFeatures" class="block text-sm font-medium text-gray-700 mb-1">产品特点/描述 (选填)</label>
             <textarea id="addProductFeatures" v-model="newProduct.features" rows="3" placeholder="例如：线上申请，快速审批 或 年轻客群，分期优惠" class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm resize-none"></textarea>
         </div>
        <div class="modal-footer mt-4">
          <button type="button" class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:bg-gray-50 text-sm" @click="closeAddModal">取消</button>
          <button type="submit" class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm" :disabled="submittingProduct">
             <i v-if="submittingProduct" class="fas fa-spinner fa-spin mr-1"></i>
             {{ submittingProduct ? '添加中...' : '确认添加' }}
          </button>
        </div>
      </form>
    </BaseModal>

    <!-- Edit Product Modal -->
     <BaseModal :is-open="isEditModalOpen" @close="closeEditModal" title="编辑我的产品">
      <form @submit.prevent="submitEditProduct">
         <input type="hidden" v-model="editingProduct.id">
         <div class="mb-4">
            <label for="editProductName" class="block text-sm font-medium text-gray-700 mb-1">产品名称 <span class="text-red-500">*</span></label>
            <input type="text" id="editProductName" v-model="editingProduct.name" required class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm">
         </div>
         <div class="mb-4">
            <label for="editProductType" class="block text-sm font-medium text-gray-700 mb-1">产品类型 <span class="text-red-500">*</span></label>
            <input type="text" id="editProductType" v-model="editingProduct.type" required placeholder="例如：信贷产品 或 信用卡" class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm">
         </div>
         <div class="mb-4">
             <label for="editProductFeatures" class="block text-sm font-medium text-gray-700 mb-1">产品特点/描述 (选填)</label>
             <textarea id="editProductFeatures" v-model="editingProduct.features" rows="3" placeholder="例如：线上申请，快速审批 或 年轻客群，分期优惠" class="block w-full px-3 py-2 border border-gray-300 rounded-button focus:ring-primary focus:border-primary text-sm resize-none"></textarea>
         </div>
        <div class="modal-footer mt-4">
          <button type="button" class="!rounded-button px-4 py-2 border border-gray-300 text-gray-600 hover:bg-gray-50 text-sm" @click="closeEditModal">取消</button>
          <button type="submit" class="!rounded-button px-4 py-2 bg-primary text-white hover:bg-primary/90 text-sm" :disabled="submittingProduct">
             <i v-if="submittingProduct" class="fas fa-spinner fa-spin mr-1"></i>
             {{ submittingProduct ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </BaseModal>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { getSettingsData, addMyProduct, updateMyProduct, deleteMyProduct } from '@/api/settings';
import type { Product } from '@/types/settings';
import BaseModal from '@/components/common/BaseModal.vue'; // 依赖通用模态框

const products = ref<Product[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const submittingProduct = ref(false);

// Add Modal State
const isAddModalOpen = ref(false);
const newProduct = reactive<Omit<Product, 'id'>>({
  name: '',
  type: '',
  features: ''
});

// Edit Modal State
const isEditModalOpen = ref(false);
const editingProduct = reactive<Product>({
  id: '',
  name: '',
  type: '',
  features: ''
});

const loadProducts = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await getSettingsData();
    products.value = data.myProducts;
  } catch (err) {
    console.error("Error loading products:", err);
    error.value = err instanceof Error ? err.message : '获取产品数据失败';
  } finally {
    loading.value = false;
  }
};

// --- Add Product Logic ---
const openAddModal = () => {
  // Reset form
  Object.assign(newProduct, { name: '', type: '', features: '' });
  isAddModalOpen.value = true;
};

const closeAddModal = () => {
  isAddModalOpen.value = false;
};

const submitAddProduct = async () => {
  submittingProduct.value = true;
  try {
    const response = await addMyProduct(newProduct);
    if (response.success && response.newProduct) {
      products.value.push(response.newProduct); // Add to local list
      alert(response.message);
      closeAddModal();
    } else {
      alert(`添加失败: ${response.message}`);
    }
  } catch (err) {
    console.error("Error adding product:", err);
    alert('添加产品时发生错误');
  } finally {
    submittingProduct.value = false;
  }
};

// --- Edit Product Logic ---
const openEditModal = (product: Product) => {
  // Deep copy product data to avoid modifying original object directly
  Object.assign(editingProduct, JSON.parse(JSON.stringify(product)));
  isEditModalOpen.value = true;
};

const closeEditModal = () => {
  isEditModalOpen.value = false;
};

const submitEditProduct = async () => {
  submittingProduct.value = true;
  try {
    const response = await updateMyProduct(editingProduct);
    if (response.success) {
      // Find the product in the list and update it
      const index = products.value.findIndex(p => p.id === editingProduct.id);
      if (index !== -1) {
        products.value[index] = { ...editingProduct }; // Update local list
      }
      alert(response.message);
      closeEditModal();
    } else {
      alert(`更新失败: ${response.message}`);
    }
  } catch (err) {
    console.error("Error updating product:", err);
    alert('更新产品时发生错误');
  } finally {
    submittingProduct.value = false;
  }
};

// --- Delete Product Logic ---
const confirmDeleteProduct = (productId: string, index: number) => {
  // Use a simple confirm for prototype, replace with a nicer confirmation modal if available
  if (window.confirm('确定要删除这个产品吗？')) {
    deleteProductConfirmed(productId, index);
  }
};

const deleteProductConfirmed = async (productId: string, index: number) => {
  // Add visual indication maybe?
  try {
    const response = await deleteMyProduct(productId);
    if (response.success) {
      products.value.splice(index, 1); // Remove from local list
      alert(response.message);
    } else {
      alert(`删除失败: ${response.message}`);
    }
  } catch (err) {
    console.error("Error deleting product:", err);
    alert('删除产品时发生错误');
  }
};

onMounted(() => {
  loadProducts();
});
</script>

<style scoped>
.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  gap: 8px;
}

.product-item {
  transition: all 0.2s ease-in-out;
}

/* 文本截断样式 - 限制最多显示2行，超出显示省略号 */
.truncate-inline {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
  max-height: 3em; /* 2行 × 1.5 line-height */
  word-break: break-word;
}

/* 按钮悬停效果 */
.edit-product-btn:hover,
.delete-product-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style> 