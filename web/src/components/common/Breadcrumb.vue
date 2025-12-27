<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="{ path: item.path }">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue'
import { useRoute, RouteLocationNormalizedLoaded } from 'vue-router'

interface BreadcrumbItem {
  title: string
  path: string
}

export default defineComponent({
  name: 'Breadcrumb',
  setup() {
    const breadcrumbs = ref<BreadcrumbItem[]>([])
    const route = useRoute()
    
    const getBreadcrumbs = (route: RouteLocationNormalizedLoaded) => {
      let matched = route.matched.filter(item => item.meta && item.meta.title)
      const first = matched[0]
      
      if (first && first.path !== '/') {
        matched = [{ path: '/', meta: { title: '仪表盘' } }, ...matched]
      }
      
      return matched.map(item => {
        return {
          title: item.meta.title as string,
          path: item.path
        }
      })
    }
    
    watch(
      () => route.path,
      () => {
        breadcrumbs.value = getBreadcrumbs(route)
      },
      { immediate: true }
    )
    
    return {
      breadcrumbs
    }
  }
})
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  line-height: var(--header-height);
}
</style> 