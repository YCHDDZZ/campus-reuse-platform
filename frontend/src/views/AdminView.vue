<template>
  <MainLayout>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value">{{ card.value }}</div>
          <div class="metric-desc">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs class="mt20">
      <el-tab-pane label="用户审核">
        <el-card>
          <template #header>
            <div class="panel-header">
              <span>用户管理</span>
              <el-radio-group v-model="userFilter" @change="loadUsers">
                <el-radio-button label="PENDING">待审核</el-radio-button>
                <el-radio-button label="APPROVED">已通过</el-radio-button>
                <el-radio-button label="REJECTED">已拒绝</el-radio-button>
                <el-radio-button label="ALL">全部</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="users" stripe>
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="realName" label="姓名" min-width="100" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="scope">
                <el-tag :type="formatTagType(scope.row.role)">{{ formatStatus(scope.row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditStatus" label="审核状态" width="120">
              <template #default="scope">
                <el-tag :type="formatTagType(scope.row.auditStatus)">{{ formatStatus(scope.row.auditStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="success" @click="auditUser(scope.row, 'APPROVED')">通过</el-button>
                <el-button size="small" type="danger" @click="auditUser(scope.row, 'REJECTED')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="商品审核">
        <el-card>
          <template #header>待审核商品</template>
          <el-table :data="products" stripe>
            <el-table-column prop="title" label="商品" min-width="180" />
            <el-table-column prop="sellerName" label="发布者" min-width="100" />
            <el-table-column prop="categoryName" label="分类" min-width="100" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="scope">{{ formatCurrency(scope.row.price) }}</template>
            </el-table-column>
            <el-table-column prop="donationEnabled" label="支持捐赠" width="100">
              <template #default="scope">{{ formatBoolean(scope.row.donationEnabled === 1) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="success" @click="auditProduct(scope.row, 'APPROVED')">通过</el-button>
                <el-button size="small" type="danger" @click="auditProduct(scope.row, 'REJECTED')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="举报与纠纷">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>举报处理</template>
              <el-table :data="reports" stripe>
                <el-table-column prop="reason" label="举报原因" min-width="180" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button size="small" type="success" @click="resolveReport(scope.row, 'RESOLVED')">已处理</el-button>
                    <el-button size="small" type="warning" @click="resolveReport(scope.row, 'REJECTED')">驳回</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>订单纠纷</template>
              <el-table :data="disputeOrders" stripe>
                <el-table-column prop="orderNo" label="订单号" min-width="180" />
                <el-table-column prop="productTitle" label="商品" min-width="120" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button size="small" type="success" @click="mediate(scope.row, 'complete')">完成</el-button>
                    <el-button size="small" type="danger" @click="mediate(scope.row, 'cancel')">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="捐赠处理">
        <el-card>
          <template #header>捐赠记录</template>
          <el-table :data="donations" stripe>
            <el-table-column prop="productTitle" label="商品" min-width="180" />
            <el-table-column prop="organization" label="组织" min-width="140" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button size="small" type="success" @click="completeDonation(scope.row, 'COMPLETED')">完成</el-button>
                <el-button size="small" type="danger" @click="completeDonation(scope.row, 'REJECTED')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="已上架商品">
        <el-card>
          <template #header>已上架商品管理</template>
          <el-table :data="listedProducts" stripe>
            <el-table-column prop="title" label="商品" min-width="180" />
            <el-table-column prop="sellerName" label="发布者" min-width="100" />
            <el-table-column prop="categoryName" label="分类" min-width="100" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="scope">{{ formatCurrency(scope.row.price) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="当前状态" width="120">
              <template #default="scope">
                <el-tag :type="formatTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="100" />
            <el-table-column prop="favoriteCount" label="收藏数" width="100" />
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-button size="small" type="primary" @click="editProduct(scope.row)">修改</el-button>
                <el-button size="small" type="warning" v-if="scope.row.status === 'AVAILABLE'" @click="freezeProduct(scope.row)">冻结</el-button>
                <el-button size="small" type="success" v-if="scope.row.status === 'FROZEN'" @click="unfreezeProduct(scope.row)">解冻</el-button>
                <el-button size="small" type="info" v-if="scope.row.status === 'AVAILABLE'" @click="offlineProduct(scope.row)">下架</el-button>
                <el-button size="small" type="success" v-if="scope.row.status === 'OFFLINE'" @click="onlineProduct(scope.row)">上架</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="分类管理">
        <el-card>
          <template #header>
            <div class="panel-header">
              <span>商品分类管理</span>
              <el-button type="primary" @click="openCategoryDialog()">新增分类</el-button>
            </div>
          </template>
          <el-table :data="categories" stripe>
            <el-table-column prop="name" label="分类名称" min-width="160" />
            <el-table-column prop="sortNo" label="排序" width="100" />
            <el-table-column prop="enabled" label="启用状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.enabled === 1 ? 'success' : 'info'">{{ scope.row.enabled === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" @click="openCategoryDialog(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeCategory(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortNo" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="categoryEnabledBool" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import http from '../api/http'
import { formatBoolean, formatCurrency, formatStatus, formatTagType } from '../utils/format'

const dashboard = ref({})
const users = ref([])
const products = ref([])
const listedProducts = ref([])
const reports = ref([])
const orders = ref([])
const donations = ref([])
const categories = ref([])
const userFilter = ref('PENDING')
const categoryDialogVisible = ref(false)
const categoryForm = reactive({ id: null, name: '', sortNo: 1, enabled: 1 })

const cards = computed(() => [
  { label: '用户总数', value: dashboard.value.totalUsers || 0, desc: '已注册平台的全部账号' },
  { label: '待审用户', value: dashboard.value.pendingUsers || 0, desc: '等待管理员审核的用户' },
  { label: '商品总数', value: dashboard.value.totalProducts || 0, desc: '平台累计发布的闲置商品' },
  { label: '循环指数', value: dashboard.value.cycleIndex || 0, desc: '根据订单与捐赠综合计算' },
])

const disputeOrders = computed(() => orders.value.filter((item) => item.status === 'DISPUTED'))
const categoryEnabledBool = computed({
  get: () => categoryForm.enabled === 1,
  set: (value) => {
    categoryForm.enabled = value ? 1 : 0
  },
})

const loadUsers = async () => {
  users.value = await http.get('/admin/users', {
    params: { auditStatus: userFilter.value === 'ALL' ? '' : userFilter.value },
  })
}

const loadAll = async () => {
  const [d, p, lp, r, o, dn, c] = await Promise.all([
    http.get('/admin/dashboard'),
    http.get('/admin/products/pending'),
    http.get('/products?auditStatus=APPROVED'), // 获取已审核通过的商品（即已上架商品）
    http.get('/admin/reports'),
    http.get('/admin/orders'),
    http.get('/admin/donations'),
    http.get('/admin/categories'),
  ])
  dashboard.value = d
  products.value = p
  listedProducts.value = lp
  reports.value = r
  orders.value = o
  donations.value = dn
  categories.value = c
  await loadUsers()
}

const auditUser = async (row, status) => {
  await http.post(`/admin/users/${row.id}/audit`, { status })
  ElMessage.success('用户审核已更新')
  await loadAll()
}

const auditProduct = async (row, status) => {
  await http.post(`/admin/products/${row.id}/audit`, { status })
  ElMessage.success('商品审核已更新')
  await loadAll()
}

const resolveReport = async (row, status) => {
  await http.post(`/admin/reports/${row.id}/resolve`, { status, note: '管理员已处理' })
  ElMessage.success('举报处理完成')
  await loadAll()
}

const mediate = async (row, status) => {
  await http.post(`/admin/orders/${row.id}/mediate`, { status, note: '管理员处理纠纷' })
  ElMessage.success('纠纷处理完成')
  await loadAll()
}

const completeDonation = async (row, status) => {
  await http.post(`/admin/donations/${row.id}/complete`, { status, note: '管理员处理捐赠' })
  ElMessage.success('捐赠处理完成')
  await loadAll()
}

const openCategoryDialog = (row) => {
  categoryForm.id = row?.id || null
  categoryForm.name = row?.name || ''
  categoryForm.sortNo = row?.sortNo || 1
  categoryForm.enabled = row?.enabled ?? 1
  categoryDialogVisible.value = true
}

const submitCategory = async () => {
  const payload = { name: categoryForm.name, sortNo: categoryForm.sortNo, enabled: categoryForm.enabled }
  if (categoryForm.id) {
    await http.put(`/admin/categories/${categoryForm.id}`, payload)
    ElMessage.success('分类更新成功')
  } else {
    await http.post('/admin/categories', payload)
    ElMessage.success('分类新增成功')
  }
  categoryDialogVisible.value = false
  await loadAll()
}

const removeCategory = async (row) => {
  await ElMessageBox.confirm(`确认删除分类“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await http.delete(`/admin/categories/${row.id}`)
  ElMessage.success('分类已删除')
  await loadAll()
}

// 编辑商品
const editProduct = async (row) => {
  // 创建一个临时对象来保存商品信息
  const productInfo = {
    ...row,
    images: row.images || [],
    donationEnabled: row.donationEnabled === 1 ? 1 : 0
  };

  // 弹出对话框让用户修改商品信息
  try {
    const { value } = await ElMessageBox.prompt(`
      <div style="text-align: left;">
        <div style="margin-bottom: 10px;"><label>商品标题：</label><input id="edit-title" type="text" value="${productInfo.title}" style="width: 100%; padding: 5px; margin-top: 5px;" /></div>
        <div style="margin-bottom: 10px;"><label>商品描述：</label><textarea id="edit-description" rows="3" style="width: 100%; padding: 5px; margin-top: 5px;">${productInfo.description}</textarea></div>
        <div style="margin-bottom: 10px;"><label>价格：</label><input id="edit-price" type="number" step="0.01" value="${productInfo.price}" style="width: 100%; padding: 5px; margin-top: 5px;" /></div>
        <div style="margin-bottom: 10px;"><label>分类ID：</label><input id="edit-categoryId" type="number" value="${productInfo.categoryId}" style="width: 100%; padding: 5px; margin-top: 5px;" /></div>
        <div style="margin-bottom: 10px;"><label>联系二维码URL：</label><input id="edit-contactQrUrl" type="text" value="${productInfo.contactQrUrl || ''}" style="width: 100%; padding: 5px; margin-top: 5px;" /></div>
        <div style="margin-bottom: 10px;"><label>支持捐赠：</label><select id="edit-donationEnabled" style="width: 100%; padding: 5px; margin-top: 5px;"><option value="0" ${productInfo.donationEnabled === 0 ? 'selected' : ''}>否</option><option value="1" ${productInfo.donationEnabled === 1 ? 'selected' : ''}>是</option></select></div>
        <div style="margin-bottom: 10px;"><label>图片链接（JSON数组格式）：</label><textarea id="edit-images" rows="3" style="width: 100%; padding: 5px; margin-top: 5px;">${JSON.stringify(productInfo.images)}</textarea></div>
      </div>
    `, '编辑商品信息', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dangerouslyUseHTMLString: true,
      distinguishCancelAndClose: true,
      inputValidator: () => true // 不使用默认输入验证
    });

    // 获取编辑后的值
    const title = document.getElementById('edit-title')?.value || productInfo.title;
    const description = document.getElementById('edit-description')?.value || productInfo.description;
    const price = parseFloat(document.getElementById('edit-price')?.value) || productInfo.price;
    const categoryId = parseInt(document.getElementById('edit-categoryId')?.value) || productInfo.categoryId;
    const contactQrUrl = document.getElementById('edit-contactQrUrl')?.value || productInfo.contactQrUrl || '';
    const donationEnabled = parseInt(document.getElementById('edit-donationEnabled')?.value) || 0;
    const imagesStr = document.getElementById('edit-images')?.value || JSON.stringify(productInfo.images);
    let images = [];
    try {
      images = JSON.parse(imagesStr);
    } catch (e) {
      ElMessage.error('图片链接格式错误，请使用JSON数组格式');
      return;
    }

    // 提交更新
    await http.put(`/admin/products/${row.id}`, {
      title,
      description,
      price,
      categoryId,
      contactQrUrl,
      donationEnabled,
      images
    });

    ElMessage.success('商品信息更新成功');
    await loadAll();
  } catch (err) {
    // 用户取消操作
    console.log('用户取消编辑');
  }
};

// 冻结商品
const freezeProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确认冻结商品“${row.title}”吗？冻结后商品将无法被购买。`, '冻结确认', { type: 'warning' });
    await http.post(`/admin/products/${row.id}/freeze`, { status: 'FROZEN', note: '管理员冻结' });
    ElMessage.success('商品已冻结');
    await loadAll();
  } catch (err) {
    // 用户取消操作
    console.log('用户取消冻结操作');
  }
};

// 解冻商品
const unfreezeProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确认解冻商品“${row.title}”吗？解冻后商品将恢复正常状态。`, '解冻确认', { type: 'warning' });
    await http.post(`/admin/products/${row.id}/unfreeze`, { status: 'AVAILABLE', note: '管理员解冻' });
    ElMessage.success('商品已解冻');
    await loadAll();
  } catch (err) {
    // 用户取消操作
    console.log('用户取消解冻操作');
  }
};

// 下架商品
const offlineProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确认下架商品“${row.title}”吗？下架后商品将不再展示给用户。`, '下架确认', { type: 'warning' });
    await http.post(`/admin/products/${row.id}/offline`, { status: 'OFFLINE', note: '管理员下架' });
    ElMessage.success('商品已下架');
    await loadAll();
  } catch (err) {
    // 用户取消操作
    console.log('用户取消下架操作');
  }
};

// 上架商品
const onlineProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确认重新上架商品“${row.title}”吗？上架后商品将重新展示给用户。`, '上架确认', { type: 'warning' });
    await http.post(`/admin/products/${row.id}/online`, { status: 'AVAILABLE', note: '管理员上架' });
    ElMessage.success('商品已上架');
    await loadAll();
  } catch (err) {
    // 用户取消操作
    console.log('用户取消上架操作');
  }
};

onMounted(loadAll)
</script>

<style scoped>
.mt20 { margin-top: 20px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.metric-label { color: #909399; margin-bottom: 8px; }
.metric-value { font-size: 28px; font-weight: 700; color: #303133; }
.metric-desc { margin-top: 8px; color: #909399; font-size: 13px; }
</style>
