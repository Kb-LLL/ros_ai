<template>
  <div class="min-h-screen p-6" :class="isDark ? 'bg-gray-900 text-white' : 'bg-gray-50'">
    <div class="max-w-3xl mx-auto">

      <!-- 标题 -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold">📚 我的知识库</h1>
          <p class="text-sm mt-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
            上传文档后，AI 将基于这些内容回答你的问题
          </p>
        </div>
        <button @click="$router.push('/')"
                class="text-sm px-4 py-2 rounded-lg border transition"
                :class="isDark ? 'border-gray-600 hover:bg-gray-700' : 'border-gray-300 hover:bg-gray-100'">
          <i class="fas fa-arrow-left mr-1"></i>返回聊天
        </button>
      </div>

      <!-- 上传区域 -->
      <div
          class="border-2 border-dashed rounded-xl p-8 text-center mb-6 transition cursor-pointer"
          :class="[
          isDragOver ? 'border-blue-500 bg-blue-50' : '',
          isDark ? 'border-gray-600 hover:border-gray-400' : 'border-gray-300 hover:border-blue-400'
        ]"
          @dragover.prevent="isDragOver = true"
          @dragleave="isDragOver = false"
          @drop.prevent="handleDrop"
          @click="$refs.fileInput.click()"
      >
        <input ref="fileInput" type="file"
               accept=".pdf,.txt,.md"
               multiple class="hidden"
               @change="handleFileSelect" />

        <div v-if="uploading" class="flex flex-col items-center gap-3">
          <i class="fas fa-spinner fa-spin text-3xl text-blue-500"></i>
          <p class="text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
            正在上传并解析文件...
          </p>
          <div class="w-48 h-2 bg-gray-200 rounded-full overflow-hidden">
            <div class="h-full bg-blue-500 rounded-full transition-all duration-300"
                 :style="{ width: uploadProgress + '%' }"></div>
          </div>
        </div>
        <div v-else class="flex flex-col items-center gap-3">
          <i class="fas fa-cloud-upload-alt text-4xl"
             :class="isDark ? 'text-gray-400' : 'text-gray-400'"></i>
          <div>
            <p class="font-medium" :class="isDark ? 'text-gray-200' : 'text-gray-700'">
              点击上传或拖拽文件到这里
            </p>
            <p class="text-sm mt-1" :class="isDark ? 'text-gray-400' : 'text-gray-400'">
              支持 PDF、TXT、MD，单文件最大 20MB
            </p>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg"
           class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm flex items-center gap-2">
        <i class="fas fa-exclamation-circle"></i>{{ errorMsg }}
      </div>

      <!-- 成功提示 -->
      <div v-if="successMsg"
           class="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-600 text-sm flex items-center gap-2">
        <i class="fas fa-check-circle"></i>{{ successMsg }}
      </div>

      <!-- 文件列表 -->
      <div :class="['rounded-xl border overflow-hidden',
        isDark ? 'border-gray-700 bg-gray-800' : 'border-gray-200 bg-white']">
        <div class="px-4 py-3 border-b font-medium text-sm flex items-center justify-between"
             :class="isDark ? 'border-gray-700' : 'border-gray-200'">
          <span>已上传文档（{{ documents.length }}）</span>
          <button @click="loadDocuments" class="text-xs text-blue-500 hover:underline">
            <i class="fas fa-sync-alt mr-1"></i>刷新
          </button>
        </div>

        <!-- 空状态 -->
        <div v-if="documents.length === 0"
             class="py-12 text-center text-sm"
             :class="isDark ? 'text-gray-500' : 'text-gray-400'">
          <i class="fas fa-folder-open text-3xl mb-3 block opacity-30"></i>
          暂无文档，点击上方区域上传
        </div>

        <!-- 文件行 -->
        <div v-for="doc in documents" :key="doc.id"
             class="flex items-center gap-3 px-4 py-3 border-b last:border-0 transition"
             :class="isDark
            ? 'border-gray-700 hover:bg-gray-750'
            : 'border-gray-100 hover:bg-gray-50'">

          <!-- 文件图标 -->
          <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0"
               :class="fileIconBg(doc.fileType)">
            <i :class="fileIcon(doc.fileType) + ' text-white'"></i>
          </div>

          <!-- 文件信息 -->
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate"
               :class="isDark ? 'text-gray-100' : 'text-gray-800'">
              {{ doc.fileName }}
            </p>
            <p class="text-xs mt-0.5" :class="isDark ? 'text-gray-400' : 'text-gray-400'">
              {{ formatSize(doc.fileSize) }} · {{ formatDate(doc.createTime) }}
            </p>
          </div>

          <!-- 状态 -->
          <span class="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-600 flex-shrink-0">
            <i class="fas fa-check mr-1"></i>已解析
          </span>

          <!-- 删除 -->
          <button @click="deleteDoc(doc.id)"
                  class="p-1.5 rounded-lg transition flex-shrink-0"
                  :class="isDark
              ? 'text-gray-500 hover:bg-gray-700 hover:text-red-400'
              : 'text-gray-400 hover:bg-red-50 hover:text-red-500'">
            <i class="fas fa-trash-alt text-sm"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import request from '../api/auth.js'

export default {
  name: 'KnowledgeBase',
  setup() {
    const isDark     = ref(localStorage.getItem('darkMode') === 'true')
    const documents  = ref([])
    const uploading  = ref(false)
    const uploadProgress = ref(0)
    const isDragOver = ref(false)
    const errorMsg   = ref('')
    const successMsg = ref('')
    const fileInput  = ref(null)

    const loadDocuments = async () => {
      try {
        const res = await request.get('/document/list')
        if (res.code === 200) documents.value = res.data || []
      } catch (e) {
        console.error(e)
      }
    }

    const uploadFile = async (file) => {
      errorMsg.value = ''
      successMsg.value = ''
      uploading.value = true
      uploadProgress.value = 0

      // 模拟进度
      const timer = setInterval(() => {
        if (uploadProgress.value < 85) uploadProgress.value += 5
      }, 200)

      try {
        const formData = new FormData()
        formData.append('file', file)
        const res = await request.post('/document/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        clearInterval(timer)
        uploadProgress.value = 100
        if (res.code === 200) {
          successMsg.value = `「${file.name}」上传成功，AI 已可基于此文档回答问题！`
          await loadDocuments()
          setTimeout(() => { successMsg.value = '' }, 4000)
        } else {
          errorMsg.value = res.message || '上传失败'
        }
      } catch (e) {
        clearInterval(timer)
        errorMsg.value = '上传失败，请检查文件格式'
      } finally {
        uploading.value = false
        uploadProgress.value = 0
      }
    }

    const handleFileSelect = (e) => {
      const files = [...e.target.files]
      files.forEach(f => uploadFile(f))
      e.target.value = ''
    }

    const handleDrop = (e) => {
      isDragOver.value = false
      const files = [...e.dataTransfer.files]
      files.forEach(f => uploadFile(f))
    }

    const deleteDoc = async (id) => {
      if (!confirm('确定删除该文档吗？AI 将不再基于此内容回答。')) return
      try {
        await request.delete(`/document/${id}`)
        documents.value = documents.value.filter(d => d.id !== id)
      } catch (e) {
        errorMsg.value = '删除失败'
      }
    }

    const fileIcon = (type) => ({
      pdf:  'fas fa-file-pdf',
      txt:  'fas fa-file-alt',
      md:   'fab fa-markdown',
      docx: 'fas fa-file-word',
      xlsx: 'fas fa-file-excel',
    }[type] || 'fas fa-file')

    const fileIconBg = (type) => ({
      pdf:  'bg-red-500',
      txt:  'bg-gray-500',
      md:   'bg-purple-500',
      docx: 'bg-blue-500',
      xlsx: 'bg-green-500',
    }[type] || 'bg-gray-400')

    const formatSize = (bytes) => {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / 1024 / 1024).toFixed(1) + ' MB'
    }

    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleDateString('zh-CN')
    }

    onMounted(loadDocuments)

    return {
      isDark, documents, uploading, uploadProgress,
      isDragOver, errorMsg, successMsg, fileInput,
      loadDocuments, handleFileSelect, handleDrop,
      deleteDoc, fileIcon, fileIconBg, formatSize, formatDate
    }
  }
}
</script>