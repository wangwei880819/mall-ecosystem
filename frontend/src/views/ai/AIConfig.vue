<template>
  <div class="page-container">
    <div class="page-header">
      <h2>🤖 模型配置</h2>
    </div>

    <el-card class="config-card">
      <template #header>
        <span>DeepSeek 大模型配置</span>
      </template>

      <el-form label-width="120px">
        <el-form-item label="启用状态">
          <el-switch
            v-model="enabled"
            active-text="开启"
            inactive-text="关闭"
            @change="onSwitchChange"
          />
        </el-form-item>

        <template v-if="enabled">
          <el-form-item label="API密钥">
            <el-input
              v-model="apiKey"
              type="password"
              show-password
              placeholder="请输入DeepSeek API Key"
              style="max-width: 500px"
            />
            <div class="form-tip">获取API Key: <a href="https://platform.deepseek.com/api_keys" target="_blank">https://platform.deepseek.com/api_keys</a></div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const enabled = ref(false)
const apiKey = ref('')
const saving = ref(false)

const fetchConfig = async () => {
  try {
    const res = await request.get('/ai/config')
    if (res.code === 200) {
      enabled.value = res.data.enabled || false
      apiKey.value = res.data.apiKey || ''
    }
  } catch (e) {
    console.error('Failed to fetch AI config:', e)
  }
}

const onSwitchChange = (val) => {
  if (!val) {
    apiKey.value = ''
  }
}

const saveConfig = async () => {
  if (enabled.value && !apiKey.value.trim()) {
    ElMessage.warning('请输入API密钥')
    return
  }
  saving.value = true
  try {
    const res = await request.post('/ai/config', {
      enabled: enabled.value,
      apiKey: apiKey.value
    })
    if (res.code === 200) {
      ElMessage.success('配置已保存')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.config-card {
  max-width: 700px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip a {
  color: #409eff;
}
</style>
