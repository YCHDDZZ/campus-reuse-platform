<template>
  <MainLayout>
    <div class="product-detail-container" v-if="detail">
      <div class="product-layout">
        <!-- 商品图片 -->
        <div class="image-section">
          <el-card :body-style="{ padding: '4px' }" class="image-card">
            <el-image
              class="cover"
              :src="detail.images?.[0] || 'https://dummyimage.com/400x260/e5e7eb/999999&text=No+Image'"
              :preview-src-list="detail.images || []"
              hide-on-click-modal
              fit="cover"
            />
          </el-card>
        </div>

        <!-- 商品信息 -->
        <div class="info-section">
          <el-card :body-style="{ padding: '12px' }" class="info-card">
            <div class="header-row">
              <h2>{{ detail.title }}</h2>
              <el-tag :type="formatTagType(detail.status)">{{ formatStatus(detail.status) }}</el-tag>
            </div>
            <p class="description">{{ detail.description }}</p>
            <p class="info-row">分类：{{ detail.categoryName }} ｜ 发布者：{{ detail.sellerName }}</p>
            <p class="info-row">审核：<el-tag size="small" :type="formatTagType(detail.auditStatus)">{{ formatStatus(detail.auditStatus) }}</el-tag></p>
            <p class="info-row stats">浏览：{{ detail.viewCount }} ｜ 收藏：{{ detail.favoriteCount }} ｜ 循环指数：{{ detail.recycleScore }}</p>
            <p class="price">{{ formatCurrency(detail.price) }}</p>

            <div class="actions">
              <el-button type="primary" v-if="authStore.isLogin" :disabled="detail.status !== 'AVAILABLE'" @click="createOrder">立即下单</el-button>
              <el-button type="success" v-if="authStore.isLogin && detail.donationEnabled === 1" @click="createDonation">发起捐赠</el-button>
              <el-button type="warning" v-if="authStore.isLogin" @click="toggleFavorite">{{ detail.favorite ? '取消收藏' : '加入收藏' }}</el-button>
            </div>
          </el-card>
        </div>

        <!-- 二维码展示 -->
        <div class="qr-section" v-if="detail.contactQrUrl">
          <el-card :body-style="{ padding: '12px' }" class="qr-card">
            <div class="qr-display">
              <h4 class="qr-title">联系卖家</h4>
              <div class="qr-image-wrapper">
                <el-image
                  :src="detail.contactQrUrl"
                  class="qr-code"
                  alt="联系卖家二维码"
                  :preview-src-list="[detail.contactQrUrl]"
                  :initial-index="0"
                  hide-on-click-modal
                  fit="contain"
                  preview-teleported
                />
              </div>
              <p class="qr-desc">扫码直接联系卖家</p>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <el-row :gutter="8" class="section" v-if="detail">
      <el-col :xs="24" :sm="12">
        <el-card :body-style="{ padding: '10px' }">
          <template #header>
            <div class="card-header">
              <span>留言互动</span>
            </div>
          </template>
          <el-input v-model="messageText" type="textarea" rows="3" placeholder="请输入想咨询的问题" />
          <el-button type="primary" class="mt12" @click="sendMessage" :disabled="!authStore.isLogin">发送留言</el-button>
          <el-empty v-if="!(detail.messages || []).length" description="暂无留言" />
          <el-timeline v-else class="mt12">
            <el-timeline-item v-for="item in detail.messages" :key="item.id" :timestamp="item.createdAt">
              <strong>{{ item.fromUserName }}</strong>：{{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
          <el-card :body-style="{ padding: '10px' }">
            <template #header>
              <div class="card-header">
                <span>交易评价</span>
              </div>
            </template>
            <el-empty v-if="!(detail.reviews || []).length" description="暂无评价" />
            <el-timeline v-else>
              <el-timeline-item v-for="item in detail.reviews" :key="item.id" :timestamp="item.createdAt">
                <strong>{{ item.reviewer }}</strong>：{{ item.score }} 分，{{ item.content }}
              </el-timeline-item>
            </el-timeline>
          </el-card>
          <el-card :body-style="{ padding: '10px' }" class="report-card">
          <template #header>
            <div class="card-header">
              <span>举报商品</span>
            </div>
          </template>
          <el-input v-model="reportReason" placeholder="请输入举报原因，便于管理员处理" />
          <el-button type="danger" class="mt12" :disabled="!authStore.isLogin" @click="report">提交举报</el-button>
        </el-card>
      </el-col>
    </el-row>
  </MainLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'
import { formatCurrency, formatStatus, formatTagType } from '../utils/format'

const route = useRoute()
const authStore = useAuthStore()
const detail = ref(null)
const messageText = ref('')
const reportReason = ref('')

const loadDetail = async () => {
  detail.value = await http.get(`/products/${route.params.id}`)
}

const createOrder = async () => {
  await http.post('/orders', { productId: Number(route.params.id) })
  ElMessage.success('下单成功')
  await loadDetail()
}

const createDonation = async () => {
  const { value } = await ElMessageBox.prompt('请输入受捐组织名称', '发起捐赠')
  await http.post('/donations', { productId: Number(route.params.id), organization: value })
  ElMessage.success('捐赠申请已提交')
  await loadDetail()
}

const toggleFavorite = async () => {
  if (detail.value.favorite) {
    await http.delete(`/products/${route.params.id}/favorite`)
  } else {
    await http.post(`/products/${route.params.id}/favorite`)
  }
  await loadDetail()
}

const sendMessage = async () => {
  if (!messageText.value.trim()) return
  await http.post(`/products/${route.params.id}/messages`, { content: messageText.value })
  messageText.value = ''
  ElMessage.success('留言发送成功')
  await loadDetail()
}

const report = async () => {
  if (!reportReason.value.trim()) return
  await http.post(`/products/${route.params.id}/reports`, { reason: reportReason.value })
  reportReason.value = ''
  ElMessage.success('举报已提交')
}

onMounted(loadDetail)
</script>

<style scoped>
/* 整体布局优化 - 减少空白，更充分利用页面空间 */
:deep(.el-row) {
  margin-bottom: 6px;
}

/* 产品详情布局 - 确保三个卡片高度完全一致 */
.product-detail-container {
  width: 100%;
}

.product-layout {
  display: flex;
  gap: 12px;
  align-items: stretch;
  min-height: 400px;
}

.image-section {
  flex: 0 0 40%;
  display: flex;
  flex-direction: column;
}

.info-section {
  flex: 0 0 40%;
  display: flex;
  flex-direction: column;
}

.qr-section {
  flex: 0 0 20%;
  display: flex;
  flex-direction: column;
}

/* 确保三个卡片高度一致 */
.image-card,
.info-card,
.qr-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.image-card .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 4px;
  height: 100%;
}

.image-card .el-image {
  flex: 1;
  min-height: 0;
}

.info-card .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  height: 100%;
  justify-content: space-between; /* 让内容均匀分布 */
}

.qr-card .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  height: 100%;
}

/* 头部信息区域优化 */
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}
.cover {
  width: 100%;
  height: 100%;
  max-height: 450px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  min-height: 300px; /* 确保最小高度 */
}

/* 响应式调整：在小屏幕上让图片更高 */
@media (max-width: 768px) {
  .cover {
    height: 100%;
    max-height: 300px;
    min-height: 250px;
  }
}

.description {
  margin: 12px 0;
  line-height: 1.8;
  color: #303133;
  font-size: 18px;
  padding: 8px 0;
  flex: 1; /* 让描述文字填充可用空间 */
  font-weight: 500;
}

.info-row {
  margin: 10px 0;
  font-size: 16px;
  color: #606266;
  padding: 4px 0;
  font-weight: 500;
}

.stats {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.price {
  color: #f56c6c;
  font-size: 32px;
  font-weight: 700;
  margin: 20px 0;
  text-shadow: 0 2px 4px rgba(245, 108, 108, 0.2);
}

/* 二维码区域样式 - 独立的右侧展示区域 */
.qr-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.qr-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex: 1; /* 让二维码区域填充剩余空间 */
  padding: 16px 0;
}
.qr-title {
  margin: 0 0 28px 0;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  flex-shrink: 0; /* 防止标题被压缩 */
  text-align: center;
}
.qr-image-wrapper {
  padding: 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  flex: 1; /* 让二维码图片区域填充剩余空间 */
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 0; /* 防止flex子元素溢出 */
  max-width: 280px;
  margin: 0 auto;
}
.qr-code {
  width: 100%;
  height: 100%;
  max-width: 250px;
  max-height: 250px;
  aspect-ratio: 1; /* 保持正方形比例 */
  object-fit: contain;
  border: 2px solid #409eff;
  padding: 16px;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}
.qr-code:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
  border-color: #66b1ff;
}
.qr-desc {
  margin-top: 28px;
  margin-bottom: auto; /* 让描述推到底部 */
  font-size: 16px;
  color: #606266;
  text-align: center;
  flex-shrink: 0; /* 防止描述被压缩 */
  line-height: 1.6;
  font-weight: 500;
}

/* 按钮区域优化 - 让按钮更清晰明显 */
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 24px;
  margin-bottom: auto; /* 让按钮区域推到底部 */
}
.actions .el-button {
  min-width: 120px;
  font-weight: 600;
  letter-spacing: 0.8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
  padding: 14px 24px;
  font-size: 16px;
  border-radius: 10px;
  text-transform: uppercase;
}
.actions .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.2);
}

/* 内容区域优化 */
.section {
  margin-top: 6px;
}
.mt12 {
  margin-top: 12px;
}
.report-card {
  margin-top: 6px;
}

/* 卡片间距优化 */
.el-card {
  margin-bottom: 6px;
}

.card-header {
  font-weight: 600;
  font-size: 15px;
}

/* 在移动端优化布局 */
@media (max-width: 768px) {
  .product-layout {
    flex-direction: column;
    gap: 8px;
    min-height: auto;
  }

  .image-section,
  .info-section,
  .qr-section {
    flex: 0 0 auto;
    width: 100%;
  }

  .header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .actions {
    flex-direction: column;
  }

  .actions .el-button {
    width: 100%;
  }

  .cover {
    height: 100%;
    max-height: 350px;
    min-height: 250px;
  }

  .price {
    font-size: 22px;
  }

  .description {
    font-size: 13px;
  }

  .info-row {
    font-size: 12px;
  }

  .qr-title {
    font-size: 15px;
  }

  .qr-code {
    width: 100%;
    max-width: 150px;
    height: auto;
    aspect-ratio: 1;
  }

  .qr-desc {
    font-size: 12px;
  }
}
</style>
