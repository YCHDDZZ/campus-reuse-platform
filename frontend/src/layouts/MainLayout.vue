<template>
  <div class="layout">
    <header class="header">
      <div class="brand" @click="$router.push('/')">余晨华的校园闲置循环平台</div>
      <div class="nav">
        <el-button link @click="$router.push('/')">首页</el-button>
        <el-button link @click="$router.push('/products/create')" v-if="authStore.isLogin">发布商品</el-button>
        <el-button link @click="$router.push('/mine')" v-if="authStore.isLogin">个人中心</el-button>
        <el-button link @click="$router.push('/admin')" v-if="authStore.isAdmin">后台管理</el-button>
        <el-button link @click="$router.push('/login')" v-if="!authStore.isLogin">登录</el-button>
        <template v-else>
          <div class="user-box">
            <el-avatar :size="32" :src="authStore.user?.avatarUrl || ''">
              {{ (authStore.user?.username || '用').slice(0, 1).toUpperCase() }}
            </el-avatar>
            <span class="user">{{ authStore.user?.username }}</span>
          </div>
          <el-button link @click="logout">退出登录</el-button>
        </template>
      </div>
    </header>
    <main class="body">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 整体布局优化 - 更充分使用浏览器空间，减少空白 */
.layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: #409eff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.brand {
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}
.nav {
  display: flex;
  align-items: center;
  gap: 16px;
}
.nav :deep(.el-button) {
  color: #fff;
  font-weight: 500;
}
.nav :deep(.el-button):not(.el-button--primary) {
  /* 链接样式的按钮 */
  color: rgba(255, 255, 255, 0.8);
  padding: 8px 12px;
}
.nav :deep(.el-button):not(.el-button--primary):hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}
.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 8px;
}
.user {
  margin: 0 4px;
  color: #fff;
}
.body {
  flex: 1;
  max-width: 100%;
  margin: 0 auto;
  padding: 16px;
  width: 100%;
  box-sizing: border-box;
}



/* 响应式设计优化 */
@media (max-width: 768px) {
  .header {
    padding: 0 12px;
    height: 56px;
  }
  .brand {
    font-size: 16px;
  }
  .nav {
    gap: 12px;
  }
  .body {
    padding: 12px;
  }
}
</style>
