<template>
  <div class="rich-editor">
    <div class="editor-toolbar">
      <button @click.prevent="execCmd('bold')" title="加粗"><b>B</b></button>
      <button @click.prevent="execCmd('italic')" title="斜体"><i>I</i></button>
      <button @click.prevent="execCmd('underline')" title="下划线"><u>U</u></button>
      <span class="sep"></span>
      <button @click.prevent="execCmd('formatBlock', 'h2')" title="标题">H2</button>
      <button @click.prevent="execCmd('formatBlock', 'h3')" title="小标题">H3</button>
      <span class="sep"></span>
      <button @click.prevent="execCmd('insertUnorderedList')" title="无序列表">列表</button>
      <button @click.prevent="execCmd('insertOrderedList')" title="有序列表">1.列表</button>
      <span class="sep"></span>
      <button @click.prevent="triggerImageUpload" title="插入本地图片">图片</button>
      <button @click.prevent="insertImageUrl" title="插入图片URL">链接图</button>
      <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
    </div>
    <div
      ref="editor"
      class="editor-content"
      contenteditable="true"
      @input="onInput"
      @paste="onPaste"
      placeholder="请输入商品详情..."
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

const editor = ref(null)
const fileInput = ref(null)
let isInternalUpdate = false

onMounted(() => {
  if (editor.value && props.modelValue) {
    editor.value.innerHTML = props.modelValue
  }
})

// 监听外部值变化，自动同步到编辑器（如AI回填场景）
watch(() => props.modelValue, (newVal) => {
  if (editor.value && !isInternalUpdate && editor.value.innerHTML !== (newVal || '')) {
    editor.value.innerHTML = newVal || ''
  }
})

// 外部值变化时同步到编辑器（仅当非内部编辑触发时）
const syncFromProp = (val) => {
  if (!editor.value) return
  if (isInternalUpdate) return
  if (editor.value.innerHTML !== val) {
    editor.value.innerHTML = val || ''
  }
}

// 暴露方法供父组件调用
defineExpose({
  syncFromProp
})

const execCmd = (command, value) => {
  editor.value?.focus()
  document.execCommand(command, false, value || null)
  onInput()
}

const onInput = () => {
  if (!editor.value) return
  isInternalUpdate = true
  emit('update:modelValue', editor.value.innerHTML)
  nextTick(() => { isInternalUpdate = false })
}

const onPaste = (e) => {
  e.preventDefault()
  const text = e.clipboardData.getData('text/plain')
  document.execCommand('insertText', false, text)
  onInput()
}

const triggerImageUpload = () => {
  fileInput.value?.click()
}

const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    document.execCommand('insertImage', false, ev.target.result)
    onInput()
  }
  reader.readAsDataURL(file)
  // reset file input for re-selection
  e.target.value = ''
}

const insertImageUrl = () => {
  const url = prompt('请输入图片URL:')
  if (url) {
    document.execCommand('insertImage', false, url)
    onInput()
  }
}
</script>

<style scoped>
.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 8px;
  background: #f8f9fb;
  border-bottom: 1px solid #e8e8e8;
  flex-wrap: wrap;
}

.editor-toolbar button {
  padding: 4px 10px;
  border: 1px solid transparent;
  background: none;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  border-radius: 4px;
  transition: all 0.15s;
}

.editor-toolbar button:hover {
  background: #e8e8e8;
  color: #333;
}

.editor-toolbar .sep {
  width: 1px;
  height: 20px;
  background: #ddd;
  margin: 0 4px;
}

.editor-content {
  min-height: 180px;
  max-height: 400px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  outline: none;
  overflow-y: auto;
}

.editor-content:empty::before {
  content: attr(placeholder);
  color: #c0c4cc;
}

.editor-content:focus {
  background: #fff;
}
</style>
