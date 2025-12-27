<template>
  <div class="app-layout">
    <el-container>
      <el-aside width="var(--sidebar-width)">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="EagleEye Logo" class="logo" />
          <h1 class="logo-text">EagleEye</h1>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          :collapse="isCollapse"
          router
        >
          <el-menu-item index="/">
            <el-icon><Monitor /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/policy">
            <el-icon><Document /></el-icon>
            <span>监管政策</span>
          </el-menu-item>
          <el-menu-item index="/competitor">
            <el-icon><DataAnalysis /></el-icon>
            <span>竞品动态</span>
          </el-menu-item>
          <el-menu-item index="/requirement">
            <el-icon><List /></el-icon>
            <span>需求管理</span>
          </el-menu-item>
          <el-menu-item index="/setting">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header height="var(--header-height)">
          <div class="header-container">
            <div class="header-left">
              <el-button
                type="text"
                class="toggle-button"
                @click="toggleSidebar"
              >
                <el-icon v-if="isCollapse"><Expand /></el-icon>
                <el-icon v-else><Fold /></el-icon>
              </el-button>
              <breadcrumb />
            </div>
            <div class="header-right">
              <el-dropdown>
                <span class="user-info">
                  <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
                  <span class="username">管理员</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人中心</el-dropdown-item>
                    <el-dropdown-item>修改密码</el-dropdown-item>
                    <el-dropdown-item divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Monitor, Document, DataAnalysis, List, Setting, Expand, Fold } from '@element-plus/icons-vue'
import Breadcrumb from './Breadcrumb.vue'

export default defineComponent({
  name: 'AppLayout',
  components: {
    Breadcrumb,
    Monitor,
    Document,
    DataAnalysis,
    List,
    Setting,
    Expand,
    Fold
  },
  setup() {
    const route = useRoute()
    const isCollapse = ref(false)
    
    const activeMenu = computed(() => {
      return route.path
    })
    
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value
    }
    
    return {
      isCollapse,
      activeMenu,
      toggleSidebar
    }
  }
})
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
  
  .el-aside {
    background-color: #304156;
    transition: width 0.3s;
    overflow-x: hidden;
    
    .logo-container {
      height: var(--header-height);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 20px;
      
      .logo {
        width: 30px;
        height: 30px;
      }
      
      .logo-text {
        margin-left: 10px;
        color: #fff;
        font-size: 18px;
        font-weight: bold;
      }
    }
    
    .el-menu-vertical {
      border-right: none;
      background-color: transparent;
      
      .el-menu-item {
        color: #bfcbd9;
        
        &.is-active {
          color: #409eff;
          background-color: #263445;
        }
        
        &:hover {
          background-color: #263445;
        }
      }
    }
  }
  
  .el-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    
    .header-container {
      height: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        
        .toggle-button {
          margin-right: 20px;
        }
      }
      
      .header-right {
        .user-info {
          display: flex;
          align-items: center;
          cursor: pointer;
          
          .username {
            margin-left: 10px;
          }
        }
      }
    }
  }
  
  .el-main {
    background-color: #f0f2f5;
    padding: 0;
    overflow: auto;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 