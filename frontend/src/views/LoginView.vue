<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="title">校园闲置循环平台</div>
      </template>
      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="88px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名或邮箱" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
              <el-button @click="$router.push('/')">先逛逛</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="88px">
            <el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="registerForm.password" type="password" show-password /></el-form-item>
            <el-form-item label="姓名"><el-input v-model="registerForm.realName" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="registerForm.email" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="registerForm.phone" /></el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister">提交注册</el-button>
              <el-button @click="mockSmsCode">获取演示验证码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <el-alert title="演示账号（用户名/密码）：admin / Admin@123；student / 123456" type="info" :closable="false" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const tab = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: 'student', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', email: '', phone: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    await authStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  await http.post('/auth/register', registerForm)
  ElMessage.success('注册成功，请等待管理员审核')
  tab.value = 'login'
}

const mockSmsCode = async () => {
  const data = await http.post(`/auth/mock-sms-code?phone=${registerForm.phone || '13800138000'}`)
  ElMessage.success(`演示验证码：${data.code}`)
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(120deg, #409eff, #79bbff); }
.login-card { width: 460px; }
.title { font-size: 20px; font-weight: 700; text-align: center; }
</style>
