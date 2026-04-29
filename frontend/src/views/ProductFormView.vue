<template>
  <MainLayout>
    <el-card>
      <template #header>{{ isEdit ? '编辑商品' : '发布商品' }}</template>
      <el-form :model="form" label-width="110px">
        <el-form-item label="商品标题">
          <el-input v-model="form.title" />
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" rows="4" />
        </el-form-item>

        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" />
        </el-form-item>

        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 220px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品图片">
          <div class="upload-block">
            <el-upload
              :http-request="uploadProductImage"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary" plain>上传商品图片</el-button>
            </el-upload>
            <div class="upload-tip">上传的文件将自动添加到图片列表中。</div>
            <div v-if="form.images.length" class="image-list">
              <div v-for="(url, index) in form.images" :key="url" class="image-item">
                <img :src="url" alt="商品图片" />
                <div class="image-actions">
                  <span class="image-url">{{ url }}</span>
                  <el-button link type="danger" @click="removeImage(index)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="图片链接">
          <el-input
            v-model="imageInput"
            type="textarea"
            :rows="3"
            placeholder="可选：输入多个图片链接，用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="联系人二维码上传">
          <div class="upload-block">
            <el-upload
              :http-request="uploadContactQr"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary" plain>上传联系人二维码</el-button>
            </el-upload>
            <div class="upload-tip">您也可以继续使用手动输入的图片链接。</div>
          </div>
        </el-form-item>

        <el-form-item label="联系人二维码链接">
          <el-input v-model="form.contactQrUrl" placeholder="可选" />
          <img v-if="form.contactQrUrl" :src="form.contactQrUrl" class="qr-preview" alt="联系人二维码" />
        </el-form-item>

        <el-form-item label="支持捐赠">
          <el-switch v-model="donationSwitch" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit">{{ isEdit ? '更新商品' : '提交审核' }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </MainLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layouts/MainLayout.vue'
import http from '../api/http'
import { uploadFileToMinio } from '../api/files'

const router = useRouter()
const route = useRoute()
const categories = ref([])
const donationSwitch = ref(false)
const imageInput = ref('')
const isEdit = !!route.params.id  // 检查是否有商品ID参数，如果有则是编辑模式

const form = reactive({
  title: '',
  description: '',
  price: 10,
  categoryId: '',
  contactQrUrl: '',
  donationEnabled: 0,
  images: [],
})

const syncImageInput = () => {
  imageInput.value = form.images.join(', ')
}

const loadCategories = async () => {
  categories.value = await http.get('/categories')
  if (!form.categoryId && categories.value.length) {
    form.categoryId = categories.value[0].id
  }
}

const loadProductDetail = async () => {
  if (isEdit) {
    try {
      const product = await http.get(`/products/${route.params.id}`)

      // 设置表单数据
      form.title = product.title
      form.description = product.description
      form.price = product.price
      form.categoryId = product.categoryId
      form.contactQrUrl = product.contactQrUrl
      form.donationEnabled = product.donationEnabled
      form.images = product.images.map(img => img.url)

      // 设置开关状态
      donationSwitch.value = product.donationEnabled === 1

      syncImageInput()
    } catch (error) {
      ElMessage.error('加载商品信息失败')
      console.error(error)
    }
  }
}

const uploadProductImage = async (option) => {
  try {
    const result = await uploadFileToMinio(option.file, 'products')
    form.images.push(result.url)
    syncImageInput()
    ElMessage.success('商品图片上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
  }
}

const uploadContactQr = async (option) => {
  try {
    const result = await uploadFileToMinio(option.file, 'contact-qr')
    form.contactQrUrl = result.url
    ElMessage.success('联系人二维码上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
  }
}

const removeImage = (index) => {
  form.images.splice(index, 1)
  syncImageInput()
}

const submit = async () => {
  const manualImages = imageInput.value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

  form.images = Array.from(new Set(manualImages))
  form.donationEnabled = donationSwitch.value ? 1 : 0

  try {
    if (isEdit) {
      // 编辑模式，使用PUT请求
      await http.put(`/products/${route.params.id}`, form)
      ElMessage.success('商品更新成功，等待重新审核')
    } else {
      // 创建模式，使用POST请求
      await http.post('/products', form)
      ElMessage.success('商品提交成功，等待审核')
    }
    router.push('/mine')
  } catch (error) {
    console.error('提交商品失败:', error)
    ElMessage.error('提交商品失败，请重试')
  }
}

onMounted(async () => {
  await loadCategories()
  if (isEdit) {
    await loadProductDetail()
  }
})
</script>

<style scoped>
.upload-block {
  width: 100%;
}

.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.image-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.image-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.image-item img {
  width: 100px;
  height: 72px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #ebeef5;
}

.image-actions {
  display: flex;
  flex: 1;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

.image-url {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}

.qr-preview {
  width: 96px;
  height: 96px;
  margin-left: 12px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
</style>
