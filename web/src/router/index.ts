import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

// 定义路由
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../pages/Dashboard.vue'),
    meta: {
      title: '仪表盘'
    }
  },
  {
    path: '/policy-monitoring',
    name: 'PolicyMonitoring',
    component: () => import('../pages/policy/PolicyMonitoringPage.vue'),
    meta: {
      title: '监管政策'
    }
  },
  {
    path: '/policy-detail/:id',
    name: 'PolicyDetail',
    component: () => import('../pages/policy/PolicyDetailPage.vue'),
    meta: {
      title: '政策详情'
    }
  },
  {
    path: '/competitor-tracking',
    name: 'CompetitorTracking',
    component: () => import('../pages/competitor/CompetitorTrackingPage.vue'),
    meta: {
      title: '竞品动态'
    }
  },
  {
    path: '/competitor-detail/:id',
    name: 'CompetitorDetail',
    component: () => import('../pages/competitor/CompetitorDetailPage.vue'),
    meta: {
      title: '竞品详情'
    }
  },
  {
    path: '/requirement-pool',
    name: 'RequirementPool',
    component: () => import('../pages/requirement/RequirementPoolPage.vue'),
    meta: {
      title: '需求池'
    }
  },
  {
    path: '/requirement-detail/:id',
    name: 'RequirementDetail',
    component: () => import('../pages/requirement/RequirementDetailPage.vue'),
    meta: {
      title: '需求详情'
    }
  },
  {
    path: '/requirement-add',
    name: 'RequirementAdd',
    component: () => import('../pages/requirement/RequirementAddPage.vue'),
    meta: {
      title: '新增需求'
    }
  },
  {
    path: '/requirement-edit/:id',
    name: 'RequirementEdit',
    component: () => import('../pages/requirement/RequirementEditPage.vue'),
    meta: {
      title: '编辑需求'
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../pages/settings/SettingsPage.vue'),
    meta: {
      title: '设置中心'
    }
  },
  {
    path: '/admin/crawler',
    name: 'AdminCrawlerManagement',
    component: () => import('@/pages/admin/crawler/index.vue'),
    meta: {
      title: '爬虫管理',
      requiresAuth: true,
      roles: ['admin']
    }
  },
  // 默认重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || ''} - EagleEye`
  next()
})

export default router 