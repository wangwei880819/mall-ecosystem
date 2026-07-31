<template>
  <div class="ocr-section">
    <div class="ocr-header">
      <span class="ocr-icon">🤖</span>
      <span>AI 智能识别 - 营业执照信息提取</span>
    </div>
    <el-upload
      :auto-upload="false"
      :show-file-list="true"
      :on-change="handleFileChange"
      :limit="1"
      accept="image/*"
      drag
    >
      <div class="upload-area">
        <el-icon style="font-size:32px;color:#c0c4cc"><Plus /></el-icon>
        <div>点击或拖拽营业执照图片</div>
        <div class="upload-hint">支持 JPG/PNG 格式</div>
      </div>
    </el-upload>
    <el-button type="primary" size="small" style="margin-top:8px" :loading="loading" @click="doOcr">
      开始识别
    </el-button>

    <div v-if="result" class="ocr-result">
      <div class="ocr-score">识别置信度：{{ result.score }}%</div>
      <el-descriptions :column="2" size="small" border style="margin-top:8px">
        <el-descriptions-item label="企业名称">
          {{ result.companyName?.value || '-' }}
          <el-tag size="small" :type="result.companyName?.confidence > 85 ? 'success' : 'warning'">
            {{ result.companyName?.confidence }}%
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信用代码">
          {{ result.creditCode?.value || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="法定代表人">
          {{ result.legalPerson?.value || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册资本">
          {{ result.registeredCapital?.value || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="成立日期">
          {{ result.establishDate?.value || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册地址">
          {{ result.address?.value || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="经营范围" :span="2">
          {{ result.businessScope?.value || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-button type="success" size="small" style="margin-top:8px" @click="$emit('apply', result)">
        自动填充表单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

const emit = defineEmits(['apply'])
const loading = ref(false)
const result = ref(null)
const file = ref(null)

function handleFileChange(f) { file.value = f.raw }

async function doOcr() {
  if (!file.value) { ElMessage.warning('请先选择营业执照图片'); return }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    const res = await request.post('/ai/ocr', formData)
    result.value = res
    ElMessage.success('识别完成')
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ocr-section { padding: 12px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 16px }
.ocr-header { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #333; margin-bottom: 10px }
.ocr-icon { font-size: 18px }
.ocr-result { margin-top: 12px; font-size: 12px }
.ocr-score { color: #409eff; font-weight: 600; font-size: 13px }
.upload-area { padding: 20px; text-align: center; color: #999; font-size: 13px }
.upload-hint { font-size: 11px; color: #bbb; margin-top: 4px }
</style>
