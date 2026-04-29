<template>
  <MainLayout>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>个人资料</template>

          <div class="profile-top">
            <el-avatar :size="96" :src="form.avatarUrl || authStore.user?.avatarUrl || ''">
              {{ (authStore.user?.username || '用').slice(0, 1).toUpperCase() }}
            </el-avatar>
            <div class="profile-actions">
              <el-upload :http-request="uploadAvatar" :show-file-list="false" accept="image/*">
                <el-button type="primary" plain>上传头像</el-button>
              </el-upload>
              <div class="profile-tip">头像文件存储在MinIO中。</div>
            </div>
          </div>

          <el-form :model="form" label-width="90px" class="profile-form">
            <el-form-item label="用户名">
              <el-input :model-value="authStore.user?.username || ''" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="头像链接">
              <el-input v-model="form.avatarUrl" />
            </el-form-item>
            <el-form-item label="审核状态">
              <el-tag :type="formatTagType(authStore.user?.auditStatus)">{{ formatStatus(authStore.user?.auditStatus) }}</el-tag>
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="formatTagType(authStore.user?.role)">{{ formatStatus(authStore.user?.role) }}</el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-row :gutter="16">
          <el-col :span="6" v-for="card in summaryCards" :key="card.label">
            <el-card shadow="hover">
              <div class="summary-label">{{ card.label }}</div>
              <div class="summary-value">{{ card.value }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-tabs class="mt20">
      <el-tab-pane label="我的商品">
        <el-card>
          <el-table :data="products" stripe>
            <el-table-column prop="title" label="商品" min-width="160" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="scope">{{ formatCurrency(scope.row.price) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope"><el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="auditStatus" label="审核" width="120">
              <template #default="scope"><el-tag :type="formatTagType(scope.row.auditStatus)">{{ formatStatus(scope.row.auditStatus) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="favoriteCount" label="收藏数" width="100" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editProduct(scope.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的订单">
        <el-card>
          <el-table :data="orders" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="180" />
            <el-table-column prop="productTitle" label="商品" min-width="140" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="scope">{{ formatCurrency(scope.row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope"><el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="scope">
                <el-button size="small" type="success" :disabled="scope.row.status !== 'PENDING'" @click="completeOrder(scope.row)">完成</el-button>
                <el-button size="small" :disabled="scope.row.status !== 'PENDING'" @click="cancelOrder(scope.row)">取消</el-button>
                <el-button size="small" type="warning" :disabled="scope.row.status !== 'COMPLETED'" @click="reviewOrder(scope.row)">评价</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="收藏与捐赠">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>我的收藏</template>
              <el-table :data="favorites" stripe>
                <el-table-column prop="title" label="商品" min-width="140" />
                <el-table-column prop="price" label="价格" width="120">
                  <template #default="scope">{{ formatCurrency(scope.row.price) }}</template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="120" />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>我的捐赠</template>
              <el-table :data="donations" stripe>
                <el-table-column prop="productTitle" label="商品" min-width="140" />
                <el-table-column prop="organization" label="组织" min-width="120" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope"><el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag></template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import http from '../api/http'
import { uploadFileToMinio } from '../api/files'
import { formatCurrency, formatStatus, formatTagType } from '../utils/format'
import { useAuthStore } from '../stores/auth'

const router = useRouter()

const authStore = useAuthStore()
const products = ref([])
const orders = ref([])
const favorites = ref([])
const donations = ref([])

const form = reactive({
  realName: '',
  email: '',
  phone: '',
  avatarUrl: '',
})

const syncForm = () => {
  form.realName = authStore.user?.realName || ''
  form.email = authStore.user?.email || ''
  form.phone = authStore.user?.phone || ''
  form.avatarUrl = authStore.user?.avatarUrl || ''
}

watch(() => authStore.user, syncForm, { immediate: true, deep: true })

const summaryCards = computed(() => [
  { label: '已发布', value: products.value.length },
  { label: '订单数', value: orders.value.length },
  { label: '收藏数', value: favorites.value.length },
  { label: '捐赠数', value: donations.value.length },
])

const loadAll = async () => {
  const [p, o, f, d] = await Promise.all([
    http.get('/products/mine'),
    http.get('/orders/my'),
    http.get('/products/favorites/list'),
    http.get('/donations/my'),
  ])
  products.value = p
  orders.value = o
  favorites.value = f
  donations.value = d
}

const uploadAvatar = async (option) => {
  try {
    const result = await uploadFileToMinio(option.file, 'avatars')
    form.avatarUrl = result.url
    await authStore.updateProfile({ avatarUrl: result.url })
    ElMessage.success('头像上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
  }
}

const saveProfile = async () => {
  await authStore.updateProfile({
    realName: form.realName,
    email: form.email,
    phone: form.phone,
    avatarUrl: form.avatarUrl,
  })
  ElMessage.success('资料保存成功')
}

const cancelOrder = async (row) => {
  await http.post(`/orders/${row.id}/cancel`)
  ElMessage.success('订单取消成功')
  await loadAll()
}

const completeOrder = async (row) => {
  await http.post(`/orders/${row.id}/complete`)
  ElMessage.success('订单完成成功')
  await loadAll()
}

const reviewOrder = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入评价内容', '订单评价')
  await http.post(`/orders/${row.id}/review`, { score: 5, content: value })
  ElMessage.success('评价提交成功')
  await loadAll()
}

const editProduct = async (product) => {
  // 跳转到商品表单页面，并传递商品信息用于编辑
  router.push(`/products/${product.id}/edit`)
}

onMounted(async () => {
  if (authStore.isLogin) {
    await authStore.fetchMe()
  }
  await loadAll()
})
</script>

<style scoped>
.mt20 { margin-top: 20px; }
.summary-label { color: #909399; margin-bottom: 8px; }
.summary-value { font-size: 24px; font-weight: 700; }
.profile-top { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.profile-actions { display: flex; flex-direction: column; gap: 8px; }
.profile-tip { color: #909399; font-size: 12px; }
.profile-form { margin-top: 16px; }
</style>
