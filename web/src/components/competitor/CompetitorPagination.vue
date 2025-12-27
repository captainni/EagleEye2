<template>
  <div class="mt-8 flex justify-center">
    <nav aria-label="Page navigation">
      <ul class="inline-flex items-center -space-x-px">
        <li>
          <a 
            @click="currentPage > 1 && changePage(currentPage - 1)" 
            :class="[
              'py-2 px-3 ml-0 leading-tight text-gray-500 bg-white rounded-l-button border border-gray-300 cursor-pointer',
              currentPage === 1 ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-100 hover:text-gray-700'
            ]"
          >
            <span class="sr-only">Previous</span>
            <i class="fas fa-chevron-left text-xs"></i>
          </a>
        </li>
        <li v-for="page in displayedPages" :key="page">
          <a 
            v-if="page === '...'" 
            class="py-2 px-3 leading-tight text-gray-500 bg-white border border-gray-300"
          >
            ...
          </a>
          <a 
            v-else 
            @click="changePage(Number(page))" 
            :class="[
              'py-2 px-3 leading-tight border border-gray-300 cursor-pointer',
              currentPage === page 
                ? 'text-primary bg-blue-50 hover:bg-blue-100 hover:text-primary z-10' 
                : 'text-gray-500 bg-white hover:bg-gray-100 hover:text-gray-700'
            ]"
          >
            {{ page }}
          </a>
        </li>
        <li>
          <a 
            @click="currentPage < totalPages && changePage(currentPage + 1)" 
            :class="[
              'py-2 px-3 leading-tight text-gray-500 bg-white rounded-r-button border border-gray-300 cursor-pointer',
              currentPage === totalPages ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-100 hover:text-gray-700'
            ]"
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
import { computed, ref, watch, defineProps, defineEmits } from 'vue';

interface PaginationProps {
  total: number;
  pageSize: number;
  currentPage: number;
}

const props = defineProps<PaginationProps>();
const emit = defineEmits<{
  (e: 'update:currentPage', page: number): void;
}>();

const currentPage = ref(props.currentPage);

// 计算总页数
const totalPages = computed(() => Math.ceil(props.total / props.pageSize));

// 同步外部currentPage变化
watch(() => props.currentPage, (newVal) => {
  currentPage.value = newVal;
});

// 切换页面
const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  emit('update:currentPage', page);
};

// 计算需要显示哪些页码
const displayedPages = computed(() => {
  const pages: (number | string)[] = [];
  const total = totalPages.value;
  const current = currentPage.value;

  if (total <= 7) {
    // 总页数少于等于7，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    // 总页数大于7，需要省略部分页码
    pages.push(1); // 始终显示第一页

    if (current <= 3) {
      // 当前页靠近开始
      pages.push(2, 3, 4, 5, '...', total);
    } else if (current >= total - 2) {
      // 当前页靠近结尾
      pages.push('...', total - 4, total - 3, total - 2, total - 1, total);
    } else {
      // 当前页在中间
      pages.push('...', current - 1, current, current + 1, '...', total);
    }
  }

  return pages;
});
</script> 