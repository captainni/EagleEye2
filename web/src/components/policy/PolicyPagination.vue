<template>
  <div class="mt-8 flex justify-center">
    <nav aria-label="Page navigation">
      <ul class="inline-flex items-center -space-x-px">
        <li>
          <a 
            href="#" 
            class="py-2 px-3 ml-0 leading-tight text-gray-500 bg-white rounded-l-button border border-gray-300 hover:bg-gray-100 hover:text-gray-700 cursor-pointer"
            @click.prevent="onPageChange(currentPage - 1)"
            :class="{ 'opacity-50 cursor-not-allowed': currentPage === 1 }"
          >
            <span class="sr-only">Previous</span>
            <i class="fas fa-chevron-left text-xs"></i>
          </a>
        </li>
        
        <!-- 页码按钮 -->
        <li v-for="page in displayedPages" :key="page">
          <a 
            v-if="page !== '...'" 
            href="#" 
            :class="[
              'py-2 px-3 leading-tight border border-gray-300 cursor-pointer',
              currentPage === Number(page)
                ? 'text-primary bg-blue-50 hover:bg-blue-100 hover:text-primary z-10'
                : 'text-gray-500 bg-white hover:bg-gray-100 hover:text-gray-700'
            ]"
            @click.prevent="onPageChange(Number(page))"
          >
            {{ page }}
          </a>
          <span 
            v-else
            class="py-2 px-3 leading-tight text-gray-500 bg-white border border-gray-300"
          >
            ...
          </span>
        </li>
        
        <li>
          <a 
            href="#" 
            class="py-2 px-3 leading-tight text-gray-500 bg-white rounded-r-button border border-gray-300 hover:bg-gray-100 hover:text-gray-700 cursor-pointer"
            @click.prevent="onPageChange(currentPage + 1)"
            :class="{ 'opacity-50 cursor-not-allowed': currentPage === totalPages }"
          >
            <span class="sr-only">Next</span>
            <i class="fas fa-chevron-right text-xs"></i>
          </a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps<{
  currentPage: number;
  totalItems: number;
  itemsPerPage: number;
}>();

const emit = defineEmits<{
  (e: 'page-change', page: number): void;
}>();

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(props.totalItems / props.itemsPerPage);
});

// 计算显示哪些页码
const displayedPages = computed(() => {
  const pages: (number | string)[] = [];
  const maxVisiblePages = 5; // 最大可见页码数
  
  if (totalPages.value <= maxVisiblePages) {
    // 总页数较少，显示所有页码
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i);
    }
  } else {
    // 总页数较多，需要省略部分页码
    pages.push(1); // 始终显示第一页
    
    // 当前页周围的页码
    let startPage = Math.max(2, props.currentPage - 1);
    let endPage = Math.min(totalPages.value - 1, props.currentPage + 1);
    
    // 处理边界情况
    if (props.currentPage <= 3) {
      endPage = 4;
    }
    
    if (props.currentPage >= totalPages.value - 2) {
      startPage = totalPages.value - 3;
    }
    
    // 添加省略号
    if (startPage > 2) {
      pages.push('...');
    }
    
    // 添加中间页码
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    // 添加末尾省略号
    if (endPage < totalPages.value - 1) {
      pages.push('...');
    }
    
    // 始终显示最后一页
    pages.push(totalPages.value);
  }
  
  return pages;
});

// 页码变化处理函数
const onPageChange = (page: number) => {
  if (page < 1 || page > totalPages.value) return;
  emit('page-change', page);
};
</script> 