<template>
  <MainLayout>
    <el-row :gutter="20">
      <el-col :span="17">
        <el-card class="hero-card" shadow="never">
          <div class="hero-title">让闲置流转，让价值再生</div>
          <div class="hero-desc">聚合校园二手交易、公益捐赠与循环激励，帮助同学快速发布、安心挑选、绿色共享校园资源。</div>
          <div class="hero-tags">
            <el-tag>校园认证</el-tag>
            <el-tag type="success">公益捐赠</el-tag>
            <el-tag type="warning">绿色循环</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-row :gutter="12">
          <el-col :span="12" v-for="card in summaryCards" :key="card.label">
            <el-card shadow="hover" class="summary-card">
              <div class="summary-label">{{ card.label }}</div>
              <div class="summary-value">{{ card.value }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="输入商品名称搜索" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" clearable style="width: 180px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="query.sortBy" style="width: 150px">
            <el-option label="最新发布" value="latest" />
            <el-option label="人气优先" value="hot" />
            <el-option label="价格升序" value="priceAsc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">筛选</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-empty v-if="!loading && !products.length" description="暂无符合条件的商品" class="empty-block" />

    <el-row v-else :gutter="16" class="list" v-loading="loading">
      <el-col :span="8" v-for="item in products" :key="item.id" class="card-col">
        <el-card shadow="hover">
          <img class="cover" :src="item.images?.[0] || 'https://dummyimage.com/400x260/e5e7eb/999999&text=No+Image'" />
          <div class="title-row">
            <div class="title">{{ item.title }}</div>
            <el-tag>{{ item.categoryName }}</el-tag>
          </div>
          <div class="desc">{{ item.description }}</div>
          <div class="meta">
            <span>卖家：{{ item.sellerName }}</span>
            <el-tag size="small" :type="formatTagType(item.status)">{{ formatStatus(item.status) }}</el-tag>
          </div>
          <div class="meta second-line">
            <span>循环值：{{ item.recycleScore }}</span>
            <span>收藏：{{ item.favoriteCount }}</span>
          </div>
          <div class="footer">
            <span class="price">{{ formatCurrency(item.price) }}</span>
            <div>
              <el-button size="small" @click="$router.push(`/products/${item.id}`)">详情</el-button>
              <el-button size="small" type="warning" v-if="authStore.isLogin" @click="toggleFavorite(item)">{{ item.favorite ? '取消收藏' : '加入收藏' }}</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import MainLayout from '../layouts/MainLayout.vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'
import { formatCurrency, formatStatus, formatTagType } from '../utils/format'

const authStore = useAuthStore()
const loading = ref(false)
const categories = ref([])
const products = ref([])
const query = reactive({ keyword: '', categoryId: '', sortBy: 'latest' })

const summaryCards = computed(() => [
  { label: '在售商品', value: products.value.length },
  { label: '分类数量', value: categories.value.length },
  { label: '支持捐赠', value: products.value.filter((item) => item.donationEnabled === 1).length },
  { label: '热门商品', value: products.value.filter((item) => item.favoriteCount > 0).length },
])

const loadCategories = async () => {
  categories.value = await http.get('/categories')
}

const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await http.get('/products', { params: query })
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async (item) => {
  if (item.favorite) {
    await http.delete(`/products/${item.id}/favorite`)
  } else {
    await http.post(`/products/${item.id}/favorite`)
  }
  await loadProducts()
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
})
</script>

<style scoped>
.hero-card { margin-bottom: 16px; border-radius: 16px; background: linear-gradient(135deg, #409eff 0%, #79bbff 100%); color: #fff; }
.hero-title { font-size: 28px; font-weight: 700; margin-bottom: 12px; }
.hero-desc { line-height: 1.8; opacity: 0.95; }
.hero-tags { margin-top: 16px; display: flex; gap: 8px; }
.summary-card { margin-bottom: 12px; text-align: center; }
.summary-label { color: #909399; margin-bottom: 8px; }
.summary-value { font-size: 24px; font-weight: 700; }
.filter-card { margin-bottom: 16px; }
.empty-block { margin-top: 40px; }
.list { margin-top: 16px; }
.card-col { margin-bottom: 16px; }
.cover { width: 100%; height: 220px; object-fit: cover; border-radius: 8px; }
.title-row { display: flex; justify-content: space-between; align-items: center; margin: 12px 0 8px; gap: 8px; }
.title { font-size: 18px; font-weight: 700; }
.desc { min-height: 48px; color: #606266; margin-bottom: 10px; }
.meta { display: flex; justify-content: space-between; color: #909399; font-size: 13px; margin-bottom: 10px; gap: 12px; }
.second-line { margin-bottom: 12px; }
.footer { display: flex; justify-content: space-between; align-items: center; }
.price { font-size: 22px; color: #f56c6c; font-weight: 700; }
</style>
