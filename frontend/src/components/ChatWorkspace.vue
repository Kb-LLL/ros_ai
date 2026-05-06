<template>
  <div class="flex h-screen overflow-hidden" :class="darkMode ? 'bg-gray-900' : 'bg-gray-50'">
    <aside
      :class="[
        'flex flex-shrink-0 flex-col border-r transition-all duration-300',
        darkMode ? 'border-gray-700 bg-gray-800' : 'border-gray-200 bg-white',
        sidebarOpen ? 'w-72' : 'w-0 overflow-hidden'
      ]"
    >
      <div class="border-b p-3" :class="darkMode ? 'border-gray-700' : 'border-gray-200'">
        <button
          @click="startNewConversation"
          class="flex w-full items-center gap-2 rounded-lg border px-3 py-2.5 text-sm font-medium transition"
          :class="darkMode
            ? 'border-gray-600 text-gray-100 hover:bg-gray-700'
            : 'border-gray-300 text-gray-700 hover:bg-gray-50'"
        >
          <i class="fas fa-plus"></i>
          <span>新建对话</span>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-2">
        <div
          v-if="conversations.length === 0"
          class="py-10 text-center text-sm"
          :class="darkMode ? 'text-gray-500' : 'text-gray-400'"
        >
          <i class="fas fa-comments mb-2 block text-2xl opacity-30"></i>
          暂无历史会话
        </div>

        <div
          v-for="conv in conversations"
          :key="conv.id"
          :class="[
            'group mb-1 flex items-center gap-2 rounded-lg px-3 py-2.5 text-sm transition',
            currentConvId === conv.id
              ? darkMode ? 'bg-gray-700 text-white' : 'bg-blue-50 font-medium text-blue-700'
              : darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'
          ]"
        >
          <button
            class="flex min-w-0 flex-1 items-center gap-2 text-left"
            @click="switchConversation(conv.id)"
          >
            <i class="fas fa-comment-dots flex-shrink-0 text-xs opacity-60"></i>
            <span v-if="editingId !== conv.id" class="truncate">{{ conv.title }}</span>
            <input
              v-else
              ref="renameInputRef"
              v-model="editingTitle"
              @blur="confirmRename(conv)"
              @keyup.enter="confirmRename(conv)"
              @keyup.esc="cancelRename"
              @click.stop
              class="min-w-0 flex-1 border-b bg-transparent text-sm outline-none"
              :class="darkMode ? 'border-gray-400 text-white' : 'border-blue-500 text-gray-800'"
            />
          </button>

          <div
            v-if="editingId !== conv.id"
            class="flex flex-shrink-0 items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100"
          >
            <button
              @click.stop="startRename(conv)"
              class="rounded p-1 hover:bg-black/10"
              title="重命名"
            >
              <i class="fas fa-edit text-xs"></i>
            </button>
            <button
              @click.stop="deleteConversation(conv.id)"
              class="rounded p-1 hover:bg-red-100 hover:text-red-500"
              title="删除"
            >
              <i class="fas fa-trash-alt text-xs"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="border-t p-3" :class="darkMode ? 'border-gray-700' : 'border-gray-200'">
        <button
          @click="router.push('/knowledge')"
          class="mb-3 flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm transition"
          :class="darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-600 hover:bg-gray-100'"
        >
          <i class="fas fa-book text-blue-500"></i>
          <span>我的知识库</span>
        </button>

        <div class="flex items-center gap-2">
          <div class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-blue-500 text-sm font-bold text-white">
            {{ avatarChar }}
          </div>
          <span class="min-w-0 flex-1 truncate text-sm" :class="darkMode ? 'text-gray-300' : 'text-gray-700'">
            {{ userInfo.nickname || userInfo.username }}
          </span>
          <button
            @click="handleLogout"
            class="rounded-lg p-2 transition"
            :class="darkMode
              ? 'text-gray-400 hover:bg-gray-700 hover:text-red-400'
              : 'text-gray-500 hover:bg-red-50 hover:text-red-500'"
            title="退出登录"
          >
            <i class="fas fa-sign-out-alt"></i>
          </button>
        </div>
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col">
      <header
        class="flex flex-shrink-0 items-center border-b px-4 py-3"
        :class="darkMode ? 'border-gray-700 bg-gray-800' : 'border-gray-200 bg-white shadow-sm'"
      >
        <button
          @click="sidebarOpen = !sidebarOpen"
          class="mr-3 rounded-lg p-2 transition"
          :class="darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-600 hover:bg-gray-100'"
        >
          <i class="fas fa-bars"></i>
        </button>

        <span class="text-lg font-bold text-blue-600">AI 顾问</span>

        <span
          v-if="currentConvTitle"
          class="ml-3 max-w-xs truncate text-sm"
          :class="darkMode ? 'text-gray-400' : 'text-gray-500'"
        >
          {{ currentConvTitle }}
        </span>

        <div class="ml-auto flex items-center gap-2">
          <button
            @click="toggleDarkMode"
            class="rounded-full p-2 transition"
            :class="darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-600 hover:bg-gray-100'"
          >
            <i :class="darkMode ? 'fas fa-sun' : 'fas fa-moon'"></i>
          </button>
        </div>
      </header>

      <main ref="chatContainer" class="flex-1 overflow-y-auto p-4" :class="darkMode ? 'bg-gray-900' : 'bg-gray-50'">
        <div
          v-if="!currentConvId"
          class="flex h-full flex-col items-center justify-center gap-4 select-none"
        >
          <div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-500 shadow-lg">
            <i class="fas fa-robot text-2xl text-white"></i>
          </div>
          <h2 class="text-xl font-semibold" :class="darkMode ? 'text-white' : 'text-gray-800'">
            你好，{{ userInfo.nickname || userInfo.username }}
          </h2>
          <p class="text-sm" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
            创建一个新对话，开始文件问答或图片分析
          </p>
          <button
            @click="startNewConversation"
            class="mt-2 rounded-xl bg-blue-500 px-6 py-2.5 font-medium text-white shadow transition hover:bg-blue-600"
          >
            <i class="fas fa-plus mr-2"></i>
            新建对话
          </button>
        </div>

        <div v-else class="mx-auto max-w-3xl space-y-6 pb-4">
          <MessageBubble
            v-for="(msg, idx) in messages"
            :key="msg.id || idx"
            :role="msg.role"
            :content="msg.content"
            :attachments="msg.attachments || []"
            :isLoading="msg.isLoading"
            :isDark="darkMode"
            @regenerate="regenerateMessage(idx)"
          />
        </div>
      </main>

      <footer
        class="flex-shrink-0 border-t p-4"
        :class="darkMode ? 'border-gray-700 bg-gray-800' : 'border-gray-200 bg-white'"
      >
        <div class="relative mx-auto max-w-3xl">
          <PromptTemplates
            :show="showTemplates"
            :isDark="darkMode"
            @select="handleTemplateSelect"
            @close="showTemplates = false"
          />

          <div class="mb-3 flex flex-wrap items-center gap-2">
            <button
              @click="showTemplates = !showTemplates"
              class="flex items-center gap-1 rounded-md border px-2 py-1 text-xs transition"
              :class="darkMode
                ? 'border-gray-600 text-gray-400 hover:bg-gray-700'
                : 'border-gray-300 text-gray-500 hover:bg-gray-50'"
            >
              <i class="fas fa-magic"></i>
              <span>模板</span>
            </button>

            <button
              @click="toggleDocumentSelector"
              class="flex items-center gap-1 rounded-md border px-2 py-1 text-xs transition"
              :class="darkMode
                ? 'border-gray-600 text-gray-400 hover:bg-gray-700'
                : 'border-gray-300 text-gray-500 hover:bg-gray-50'"
            >
              <i class="fas fa-paperclip"></i>
              <span>会话文件</span>
            </button>

            <button
              @click="openImagePicker"
              :disabled="isLoading"
              class="flex items-center gap-1 rounded-md border px-2 py-1 text-xs transition disabled:cursor-not-allowed disabled:opacity-50"
              :class="darkMode
                ? 'border-gray-600 text-gray-400 hover:bg-gray-700'
                : 'border-gray-300 text-gray-500 hover:bg-gray-50'"
            >
              <i class="fas fa-image"></i>
              <span>上传图片</span>
            </button>

            <span
              v-if="hasUploadingImages"
              class="text-xs"
              :class="darkMode ? 'text-amber-300' : 'text-amber-600'"
            >
              图片上传中，上传完成后才能发送
            </span>

            <span
              v-else-if="boundDocuments.length > 0"
              class="text-xs"
              :class="darkMode ? 'text-green-300' : 'text-green-600'"
            >
              当前会话已记忆 {{ boundDocuments.length }} 个文件
            </span>

            <span
              v-else
              class="text-xs"
              :class="darkMode ? 'text-gray-400' : 'text-gray-500'"
            >
              未绑定文件时，默认检索全部知识库
            </span>
          </div>

          <div
            v-if="showDocumentSelector && currentConvId"
            class="mb-3 rounded-2xl border p-4"
            :class="darkMode ? 'border-gray-700 bg-gray-900/70' : 'border-gray-200 bg-gray-50'"
          >
            <div class="mb-3 flex items-center justify-between gap-3">
              <div>
                <p class="text-sm font-medium" :class="darkMode ? 'text-gray-100' : 'text-gray-800'">
                  会话级文件记忆
                </p>
                <p class="text-xs" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
                  绑定后，这个会话优先只检索这些文件；不绑定则默认使用全部知识库。
                </p>
              </div>
              <button
                @click="loadKnowledgeDocuments"
                class="rounded-md px-2 py-1 text-xs transition"
                :class="darkMode
                  ? 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
                  : 'text-gray-500 hover:bg-white hover:text-gray-700'"
              >
                <i class="fas fa-sync-alt mr-1"></i>刷新
              </button>
            </div>

            <div
              v-if="isLoadingDocumentBindings"
              class="py-8 text-center text-sm"
              :class="darkMode ? 'text-gray-400' : 'text-gray-500'"
            >
              <i class="fas fa-spinner fa-spin mr-2"></i>正在加载文件列表...
            </div>

            <div
              v-else-if="knowledgeDocuments.length === 0"
              class="rounded-xl border border-dashed p-6 text-center text-sm"
              :class="darkMode ? 'border-gray-700 text-gray-400' : 'border-gray-300 text-gray-500'"
            >
              <p>当前还没有可绑定的知识文件。</p>
              <button
                @click="router.push('/knowledge')"
                class="mt-3 rounded-lg bg-blue-500 px-3 py-2 text-xs font-medium text-white transition hover:bg-blue-600"
              >
                去上传文件
              </button>
            </div>

            <div v-else class="space-y-2">
              <label
                v-for="doc in knowledgeDocuments"
                :key="doc.id"
                class="flex cursor-pointer items-start gap-3 rounded-xl border px-3 py-3 transition"
                :class="darkMode
                  ? 'border-gray-700 hover:bg-gray-800'
                  : 'border-gray-200 bg-white hover:border-blue-300'"
              >
                <input
                  v-model="selectedDocumentIds"
                  :value="doc.id"
                  type="checkbox"
                  class="mt-0.5 h-4 w-4 rounded border-gray-300 text-blue-500 focus:ring-blue-400"
                />
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-2">
                    <p class="truncate text-sm font-medium" :class="darkMode ? 'text-gray-100' : 'text-gray-800'">
                      {{ doc.fileName }}
                    </p>
                    <span
                      class="rounded-full px-2 py-0.5 text-[11px]"
                      :class="documentTypeClass(doc.fileType)"
                    >
                      {{ documentTypeLabel(doc.fileType) }}
                    </span>
                  </div>
                  <p class="mt-1 text-xs" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
                    {{ formatFileSize(doc.fileSize) }} · {{ formatDate(doc.createTime) }}
                  </p>
                </div>
              </label>

              <div class="mt-3 flex items-center justify-between gap-3">
                <p class="text-xs" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
                  已选择 {{ selectedDocumentIds.length }} / {{ knowledgeDocuments.length }} 个文件
                </p>
                <div class="flex items-center gap-2">
                  <button
                    @click="resetConversationBindings"
                    class="rounded-lg px-3 py-2 text-xs transition"
                    :class="darkMode
                      ? 'text-gray-300 hover:bg-gray-700'
                      : 'text-gray-600 hover:bg-white'"
                  >
                    清空选择
                  </button>
                  <button
                    @click="saveConversationBindings"
                    :disabled="isSavingDocumentBindings"
                    class="rounded-lg bg-blue-500 px-3 py-2 text-xs font-medium text-white transition hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    <i v-if="isSavingDocumentBindings" class="fas fa-spinner fa-spin mr-1"></i>
                    保存会话文件
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="boundDocuments.length" class="mb-3 flex flex-wrap gap-2">
            <div
              v-for="doc in boundDocuments"
              :key="doc.id"
              class="flex items-center gap-2 rounded-full px-3 py-1 text-xs"
              :class="darkMode ? 'bg-blue-500/15 text-blue-200' : 'bg-blue-50 text-blue-700'"
            >
              <i class="fas fa-file-alt"></i>
              <span class="max-w-44 truncate">{{ doc.fileName }}</span>
            </div>
          </div>

          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            class="hidden"
            @change="handleImageSelect"
          />

          <div v-if="selectedImages.length" class="mb-3 grid gap-2 sm:grid-cols-2">
            <div
              v-for="image in selectedImages"
              :key="image.id"
              :class="[
                'flex items-center gap-3 rounded-xl border p-3',
                darkMode ? 'border-gray-700 bg-gray-900/60' : 'border-gray-200 bg-gray-50'
              ]"
            >
              <img :src="image.previewUrl" :alt="image.name" class="h-16 w-16 rounded-lg object-cover" />

              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <p class="truncate text-sm font-medium" :class="darkMode ? 'text-gray-100' : 'text-gray-800'">
                    {{ image.name }}
                  </p>
                  <span
                    class="rounded-full px-2 py-0.5 text-[11px]"
                    :class="getImageStatusClass(image.status)"
                  >
                    {{ getImageStatusLabel(image.status) }}
                  </span>
                </div>

                <p class="mt-1 text-xs" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
                  {{ formatFileSize(image.size) }}
                </p>

                <p v-if="image.errorMessage" class="mt-1 text-xs text-red-500">
                  {{ image.errorMessage }}
                </p>

                <div
                  v-else
                  class="mt-2 h-1.5 overflow-hidden rounded-full"
                  :class="darkMode ? 'bg-gray-700' : 'bg-gray-200'"
                >
                  <div
                    class="h-full rounded-full bg-blue-500 transition-all"
                    :style="{ width: `${Math.max(image.progress, image.status === 'uploaded' ? 100 : 0)}%` }"
                  ></div>
                </div>
              </div>

              <button
                @click="removeSelectedImage(image)"
                :disabled="image.status === 'uploading' || isLoading"
                class="rounded-full p-2 text-sm transition disabled:cursor-not-allowed disabled:opacity-40"
                :class="darkMode
                  ? 'text-gray-400 hover:bg-gray-700 hover:text-red-300'
                  : 'text-gray-400 hover:bg-gray-100 hover:text-red-500'"
                title="移除图片"
              >
                <i class="fas fa-times"></i>
              </button>
            </div>
          </div>

          <div class="flex items-end gap-2">
            <textarea
              ref="textareaRef"
              v-model="userInput"
              rows="1"
              :disabled="!currentConvId || isLoading"
              :placeholder="currentConvId
                ? '输入消息...（Enter 发送，Ctrl+Enter 换行）'
                : '请先新建或选择一个会话'"
              :class="[
                'flex-1 resize-none rounded-xl border px-4 py-3 text-sm transition focus:outline-none focus:ring-2',
                darkMode
                  ? 'border-gray-600 bg-gray-700 text-white placeholder-gray-500 focus:ring-blue-400'
                  : 'border-gray-300 bg-white text-gray-800 focus:border-transparent focus:ring-blue-400',
                (!currentConvId || isLoading) ? 'cursor-not-allowed opacity-60' : ''
              ]"
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.ctrl.enter.exact.prevent="insertLineBreak"
              @input="adjustHeight"
            ></textarea>

            <button
              @click="isLoading ? stopResponse() : sendMessage()"
              :disabled="isLoading ? false : !canSend"
              :class="[
                'flex-shrink-0 rounded-xl p-3 text-white transition disabled:cursor-not-allowed disabled:opacity-50',
                isLoading ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-500 hover:bg-blue-600'
              ]"
            >
              <i :class="isLoading ? 'fas fa-stop' : 'fas fa-paper-plane'"></i>
            </button>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request, { authApi } from '../api/auth.js'
import { conversationApi } from '../api/conversation.js'
import MessageBubble from './MessageBubble.vue'
import PromptTemplates from './PromptTemplates.vue'

const MAX_IMAGE_SIZE = 10 * 1024 * 1024

const router = useRouter()

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const darkMode = ref(localStorage.getItem('darkMode') === 'true')
const sidebarOpen = ref(true)
const conversations = ref([])
const currentConvId = ref(null)
const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const showTemplates = ref(false)
const showDocumentSelector = ref(false)
const selectedImages = ref([])
const knowledgeDocuments = ref([])
const boundDocuments = ref([])
const selectedDocumentIds = ref([])
const isLoadingDocumentBindings = ref(false)
const isSavingDocumentBindings = ref(false)
const editingId = ref(null)
const editingTitle = ref('')
const chatContainer = ref(null)
const textareaRef = ref(null)
const fileInputRef = ref(null)
const renameInputRef = ref(null)

let controller = null
let imageSeed = 0

const avatarChar = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || '?'
  return name.charAt(0).toUpperCase()
})

const currentConversation = computed(() =>
  conversations.value.find(conv => conv.id === currentConvId.value) || null
)

const currentConvTitle = computed(() => currentConversation.value?.title || '')

const uploadedImages = computed(() =>
  selectedImages.value.filter(image => image.status === 'uploaded' && image.objectKey)
)

const hasUploadingImages = computed(() =>
  selectedImages.value.some(image => image.status === 'uploading')
)

const canSend = computed(() => {
  if (!currentConvId.value || hasUploadingImages.value || isLoading.value) {
    return false
  }
  return Boolean(userInput.value.trim() || uploadedImages.value.length > 0)
})

const extractErrorMessage = (error, fallback = '操作失败，请稍后重试') => {
  const responseMessage = error?.response?.data?.message
  if (responseMessage) {
    return responseMessage
  }
  if (typeof error?.message === 'string' && error.message.trim()) {
    return error.message
  }
  return fallback
}

const formatFileSize = (size) => {
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const formatDate = (dateStr) => {
  if (!dateStr) {
    return ''
  }
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const documentTypeLabel = (type) => ({
  pdf: 'PDF',
  doc: 'Word',
  docx: 'Word',
  xls: 'Excel',
  xlsx: 'Excel',
  txt: 'TXT',
  md: 'Markdown'
}[type] || (type || '文件').toUpperCase())

const documentTypeClass = (type) => ({
  pdf: darkMode.value ? 'bg-red-500/15 text-red-200' : 'bg-red-50 text-red-600',
  doc: darkMode.value ? 'bg-blue-500/15 text-blue-200' : 'bg-blue-50 text-blue-600',
  docx: darkMode.value ? 'bg-blue-500/15 text-blue-200' : 'bg-blue-50 text-blue-600',
  xls: darkMode.value ? 'bg-green-500/15 text-green-200' : 'bg-green-50 text-green-600',
  xlsx: darkMode.value ? 'bg-green-500/15 text-green-200' : 'bg-green-50 text-green-600',
  md: darkMode.value ? 'bg-purple-500/15 text-purple-200' : 'bg-purple-50 text-purple-600',
  txt: darkMode.value ? 'bg-gray-500/15 text-gray-200' : 'bg-gray-100 text-gray-600'
}[type] || (darkMode.value ? 'bg-gray-500/15 text-gray-200' : 'bg-gray-100 text-gray-600'))

const getImageStatusLabel = (status) => {
  if (status === 'uploading') {
    return '上传中'
  }
  if (status === 'uploaded') {
    return '已上传'
  }
  return '上传失败'
}

const getImageStatusClass = (status) => {
  if (status === 'uploading') {
    return darkMode.value
      ? 'bg-amber-500/15 text-amber-300'
      : 'bg-amber-100 text-amber-700'
  }
  if (status === 'uploaded') {
    return darkMode.value
      ? 'bg-green-500/15 text-green-300'
      : 'bg-green-100 text-green-700'
  }
  return darkMode.value
    ? 'bg-red-500/15 text-red-300'
    : 'bg-red-100 text-red-700'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const adjustHeight = () => {
  const el = textareaRef.value
  if (!el) {
    return
  }
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 180)}px`
}

const insertLineBreak = () => {
  userInput.value += '\n'
  nextTick(adjustHeight)
}

const toggleDarkMode = () => {
  darkMode.value = !darkMode.value
  localStorage.setItem('darkMode', String(darkMode.value))
}

const loadConversations = async () => {
  try {
    const res = await conversationApi.list()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const loadKnowledgeDocuments = async () => {
  try {
    const res = await request.get('/document/list')
    if (res.code === 200) {
      knowledgeDocuments.value = res.data || []
    }
  } catch (error) {
    console.error('加载知识文件失败', error)
  }
}

const loadConversationBindings = async (conversationId) => {
  if (!conversationId) {
    boundDocuments.value = []
    selectedDocumentIds.value = []
    return
  }

  isLoadingDocumentBindings.value = true
  try {
    const [documentsRes, bindingRes] = await Promise.all([
      request.get('/document/list'),
      conversationApi.getDocuments(conversationId)
    ])

    knowledgeDocuments.value = documentsRes.code === 200 ? (documentsRes.data || []) : []
    boundDocuments.value = bindingRes.code === 200 ? (bindingRes.data || []) : []
    selectedDocumentIds.value = boundDocuments.value.map(doc => doc.id)
  } catch (error) {
    console.error('加载会话文件绑定失败', error)
  } finally {
    isLoadingDocumentBindings.value = false
  }
}

const resetConversationBindings = () => {
  selectedDocumentIds.value = []
}

const saveConversationBindings = async () => {
  if (!currentConvId.value) {
    return
  }

  isSavingDocumentBindings.value = true
  try {
    const res = await conversationApi.bindDocuments(currentConvId.value, selectedDocumentIds.value)
    if (res.code !== 200) {
      throw new Error(res.message || '保存会话文件失败')
    }
    boundDocuments.value = knowledgeDocuments.value.filter(doc => selectedDocumentIds.value.includes(doc.id))
    showDocumentSelector.value = false
  } catch (error) {
    window.alert(extractErrorMessage(error, '保存会话文件失败'))
  } finally {
    isSavingDocumentBindings.value = false
  }
}

const toggleDocumentSelector = async () => {
  const ready = await ensureConversationReady()
  if (!ready) {
    return
  }

  if (!showDocumentSelector.value) {
    await loadConversationBindings(currentConvId.value)
  }
  showDocumentSelector.value = !showDocumentSelector.value
}

const isBlobUrl = (url) => typeof url === 'string' && url.startsWith('blob:')

const releaseImagePreview = (image) => {
  if (isBlobUrl(image?.previewUrl)) {
    URL.revokeObjectURL(image.previewUrl)
  }
}

const releaseMessageAttachmentPreviews = (messageList = []) => {
  messageList.forEach((message) => {
    const attachments = message.attachments || []
    attachments.forEach((attachment) => {
      if (isBlobUrl(attachment?.previewUrl)) {
        URL.revokeObjectURL(attachment.previewUrl)
      }
    })
  })
}

const clearSelectedImages = async (deleteRemote = false) => {
  const images = [...selectedImages.value]
  selectedImages.value = []

  for (const image of images) {
    releaseImagePreview(image)
    if (deleteRemote && image.objectKey) {
      try {
        await request.delete('/asset/image', { params: { objectKey: image.objectKey } })
      } catch (error) {
        console.warn('删除临时图片失败', error)
      }
    }
  }
}

const ensureConversationReady = async () => {
  if (currentConvId.value) {
    return true
  }
  await startNewConversation()
  return Boolean(currentConvId.value)
}

const switchConversation = async (id) => {
  if (currentConvId.value === id) {
    return
  }
  if (isLoading.value) {
    window.alert('当前正在生成回复，请先停止本次回答。')
    return
  }
  if (hasUploadingImages.value) {
    window.alert('图片还在上传，请稍候再切换会话。')
    return
  }

  await clearSelectedImages(true)
  releaseMessageAttachmentPreviews(messages.value)
  showTemplates.value = false
  showDocumentSelector.value = false
  editingId.value = null
  userInput.value = ''
  currentConvId.value = id
  messages.value = []
  nextTick(adjustHeight)

  try {
    const res = await conversationApi.getMessages(id)
    if (res.code === 200) {
      messages.value = (res.data || []).map(message => ({
        ...message,
        isLoading: false,
        rawMessage: message.role === 'user' ? message.content : '',
        imageKeys: [],
        attachments: []
      }))
      scrollToBottom()
    }
    await loadConversationBindings(id)
  } catch (error) {
    console.error('加载消息失败', error)
  }
}

const startNewConversation = async () => {
  if (isLoading.value) {
    window.alert('当前正在生成回复，请先停止本次回答。')
    return
  }
  if (hasUploadingImages.value) {
    window.alert('图片还在上传，请稍候再创建新会话。')
    return
  }

  await clearSelectedImages(true)
  showTemplates.value = false
  showDocumentSelector.value = false
  userInput.value = ''
  nextTick(adjustHeight)

  try {
    const res = await conversationApi.create()
    if (res.code === 200 && res.data) {
      await loadConversations()
      await switchConversation(res.data)
    }
  } catch (error) {
    console.error('创建会话失败', error)
  }
}

const deleteConversation = async (id) => {
  if (isLoading.value) {
    window.alert('当前正在生成回复，请先停止本次回答。')
    return
  }
  if (!window.confirm('确定删除这个会话吗？')) {
    return
  }

  try {
    await conversationApi.remove(id)
    conversations.value = conversations.value.filter(conv => conv.id !== id)
    if (currentConvId.value === id) {
      currentConvId.value = null
      releaseMessageAttachmentPreviews(messages.value)
      messages.value = []
      userInput.value = ''
      boundDocuments.value = []
      selectedDocumentIds.value = []
      showDocumentSelector.value = false
      await clearSelectedImages(true)
      nextTick(adjustHeight)
    }
  } catch (error) {
    console.error('删除会话失败', error)
  }
}

const startRename = (conv) => {
  editingId.value = conv.id
  editingTitle.value = conv.title
  nextTick(() => {
    renameInputRef.value?.focus()
    renameInputRef.value?.select()
  })
}

const cancelRename = () => {
  editingId.value = null
  editingTitle.value = ''
}

const confirmRename = async (conv) => {
  const title = editingTitle.value.trim()
  editingId.value = null
  if (!title || title === conv.title) {
    return
  }

  try {
    const res = await conversationApi.rename(conv.id, title)
    if (res.code === 200) {
      conv.title = title
    }
  } catch (error) {
    console.error('重命名会话失败', error)
  }
}

const handleTemplateSelect = (prompt) => {
  userInput.value = prompt
  showTemplates.value = false
  nextTick(adjustHeight)
}

const openImagePicker = async () => {
  if (isLoading.value) {
    return
  }
  const ready = await ensureConversationReady()
  if (ready) {
    fileInputRef.value?.click()
  }
}

const uploadImageFile = async (file) => {
  const image = reactive({
    id: ++imageSeed,
    file,
    name: file.name,
    size: file.size,
    previewUrl: URL.createObjectURL(file),
    progress: 0,
    status: 'uploading',
    objectKey: '',
    errorMessage: ''
  })
  selectedImages.value.push(image)

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request.post('/asset/image', formData, {
      onUploadProgress: (event) => {
        if (!event.total) {
          return
        }
        image.progress = Math.round((event.loaded / event.total) * 100)
      }
    })

    if (res.code !== 200 || !res.data?.objectKey) {
      throw new Error(res.message || '图片上传失败')
    }

    image.progress = 100
    image.status = 'uploaded'
    image.objectKey = res.data.objectKey
  } catch (error) {
    image.status = 'error'
    image.errorMessage = extractErrorMessage(error, '图片上传失败')
  }
}

const handleImageSelect = async (event) => {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length) {
    return
  }

  const ready = await ensureConversationReady()
  if (!ready) {
    return
  }

  const validFiles = []
  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      window.alert(`仅支持图片文件：${file.name}`)
      continue
    }
    if (file.size > MAX_IMAGE_SIZE) {
      window.alert(`图片不能超过 10MB：${file.name}`)
      continue
    }
    validFiles.push(file)
  }

  await Promise.all(validFiles.map(uploadImageFile))
}

const removeSelectedImage = async (image) => {
  if (image.status === 'uploading' || isLoading.value) {
    return
  }

  selectedImages.value = selectedImages.value.filter(item => item.id !== image.id)
  releaseImagePreview(image)

  if (image.objectKey) {
    try {
      await request.delete('/asset/image', { params: { objectKey: image.objectKey } })
    } catch (error) {
      console.warn('删除图片失败', error)
    }
  }
}

const buildUserDisplayContent = (text, images) => {
  const trimmedText = text.trim()
  if (!images.length) {
    return trimmedText
  }

  const imageLine = `[图片 ${images.length} 张] ${images.map(image => image.name).join('、')}`
  return trimmedText ? `${imageLine}\n${trimmedText}` : imageLine
}

const buildUserRawMessage = (text, images) => {
  const trimmedText = text.trim()
  if (trimmedText) {
    return trimmedText
  }
  if (images.length > 1) {
    return '请分析这些图片'
  }
  return images.length ? '请分析这张图片' : ''
}

const buildUserMessageAttachments = (images) =>
  images.map(image => ({
    id: image.id,
    name: image.name,
    objectKey: image.objectKey,
    previewUrl: image.file ? URL.createObjectURL(image.file) : image.previewUrl
  }))

const streamReply = async ({ memoryId, message, imageKeys, assistantIndex }) => {
  const token = localStorage.getItem('satoken') || ''
  controller = new AbortController()

  let aiContent = ''
  let aborted = false

  try {
    const response = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        satoken: token
      },
      body: JSON.stringify({
        memoryId,
        message,
        imageKeys
      }),
      signal: controller.signal
    })

    if (response.status === 401) {
      localStorage.removeItem('satoken')
      localStorage.removeItem('userInfo')
      router.push('/login')
      throw new Error('登录已过期，请重新登录')
    }

    if (!response.ok) {
      throw new Error(`请求失败，状态码 ${response.status}`)
    }
    if (!response.body) {
      throw new Error('当前环境不支持流式响应')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      aiContent += decoder.decode(value, { stream: true })
      if (messages.value[assistantIndex]) {
        messages.value[assistantIndex].content = aiContent
        messages.value[assistantIndex].isLoading = false
      }
      scrollToBottom()
    }

    aiContent += decoder.decode()
    if (messages.value[assistantIndex]) {
      messages.value[assistantIndex].content = aiContent
      messages.value[assistantIndex].isLoading = false
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      aborted = true
    } else {
      throw error
    }
  } finally {
    controller = null
  }

  return { aiContent: aiContent.trimEnd(), aborted }
}

const persistTurn = async ({ saveUser, userContent, assistantContent }) => {
  if (!currentConvId.value) {
    return
  }

  if (saveUser && userContent) {
    await conversationApi.saveMessage(currentConvId.value, 'user', userContent)
  }
  if (assistantContent) {
    await conversationApi.saveMessage(currentConvId.value, 'assistant', assistantContent)
  }
  await loadConversations()
}

const sendMessage = async () => {
  if (!canSend.value) {
    return
  }

  const text = userInput.value
  const images = uploadedImages.value.map(image => ({
    id: image.id,
    name: image.name,
    objectKey: image.objectKey,
    file: image.file
  }))
  const rawMessage = buildUserRawMessage(text, images)
  const displayContent = buildUserDisplayContent(text, images)
  const attachments = buildUserMessageAttachments(images)

  const userMessage = {
    role: 'user',
    content: displayContent,
    rawMessage,
    imageKeys: images.map(image => image.objectKey),
    attachments,
    isLoading: false
  }
  const assistantMessage = {
    role: 'assistant',
    content: '',
    isLoading: true
  }

  messages.value.push(userMessage)
  messages.value.push(assistantMessage)
  const assistantIndex = messages.value.length - 1

  isLoading.value = true
  showTemplates.value = false
  userInput.value = ''
  nextTick(adjustHeight)
  scrollToBottom()

  let aiContent = ''
  let aborted = false

  try {
    const result = await streamReply({
      memoryId: currentConvId.value,
      message: rawMessage,
      imageKeys: userMessage.imageKeys,
      assistantIndex
    })
    aiContent = result.aiContent
    aborted = result.aborted
  } catch (error) {
    if (messages.value[assistantIndex]) {
      messages.value[assistantIndex].content = extractErrorMessage(error, '抱歉，发生错误，请稍后重试。')
      messages.value[assistantIndex].isLoading = false
    }
    userInput.value = text
    nextTick(adjustHeight)
    isLoading.value = false
    return
  }

  if (aborted && !aiContent) {
    releaseMessageAttachmentPreviews([messages.value[assistantIndex - 1]])
    messages.value.splice(assistantIndex - 1, 2)
    userInput.value = text
    nextTick(adjustHeight)
    isLoading.value = false
    return
  }

  if (messages.value[assistantIndex]) {
    messages.value[assistantIndex].content = aiContent || '未收到模型返回内容，请重试。'
    messages.value[assistantIndex].isLoading = false
  }

  try {
    await persistTurn({
      saveUser: true,
      userContent: displayContent,
      assistantContent: aiContent
    })
    await clearSelectedImages(false)
  } catch (error) {
    console.error('保存会话消息失败', error)
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const restoreAssistantAfterFailedRegenerate = async (assistantIndex, previousContent) => {
  if (messages.value[assistantIndex]) {
    messages.value[assistantIndex].content = previousContent
    messages.value[assistantIndex].isLoading = false
  }
  try {
    await conversationApi.saveMessage(currentConvId.value, 'assistant', previousContent)
  } catch (error) {
    console.error('恢复上一条 AI 回复失败', error)
  }
}

const regenerateMessage = async (index) => {
  if (isLoading.value) {
    return
  }
  if (index !== messages.value.length - 1) {
    window.alert('当前只支持重新生成最后一条 AI 回复。')
    return
  }

  const assistantMessage = messages.value[index]
  const userMessage = messages.value[index - 1]
  if (!assistantMessage || assistantMessage.role !== 'assistant' || !userMessage || userMessage.role !== 'user') {
    return
  }

  if ((!userMessage.imageKeys || userMessage.imageKeys.length === 0) && userMessage.content.startsWith('[图片')) {
    window.alert('包含图片的历史消息暂不支持刷新后重新生成，请重新上传图片再提问。')
    return
  }

  try {
    await conversationApi.deleteLastAiMessage(currentConvId.value)
  } catch (error) {
    window.alert('删除旧回答失败，请稍后再试。')
    return
  }

  const previousAssistantContent = assistantMessage.content
  assistantMessage.content = ''
  assistantMessage.isLoading = true
  isLoading.value = true
  scrollToBottom()

  let aiContent = ''
  let aborted = false

  try {
    const result = await streamReply({
      memoryId: currentConvId.value,
      message: userMessage.rawMessage || userMessage.content,
      imageKeys: userMessage.imageKeys || [],
      assistantIndex: index
    })
    aiContent = result.aiContent
    aborted = result.aborted
  } catch (error) {
    await restoreAssistantAfterFailedRegenerate(index, previousAssistantContent)
    isLoading.value = false
    return
  }

  if (aborted && !aiContent) {
    await restoreAssistantAfterFailedRegenerate(index, previousAssistantContent)
    isLoading.value = false
    return
  }

  assistantMessage.content = aiContent || previousAssistantContent
  assistantMessage.isLoading = false

  try {
    await persistTurn({
      saveUser: false,
      assistantContent: assistantMessage.content
    })
  } catch (error) {
    console.error('保存重新生成结果失败', error)
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const stopResponse = () => {
  controller?.abort()
}

const handleLogout = async () => {
  try {
    await authApi.logout()
  } catch (error) {
    console.warn('退出登录请求失败', error)
  } finally {
    localStorage.removeItem('satoken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}

onMounted(async () => {
  await loadKnowledgeDocuments()
  await loadConversations()
  if (conversations.value.length > 0) {
    await switchConversation(conversations.value[0].id)
  } else {
    nextTick(adjustHeight)
  }
})

onBeforeUnmount(async () => {
  controller?.abort()
  releaseMessageAttachmentPreviews(messages.value)
  await clearSelectedImages(true)
})
</script>

<style scoped>
textarea {
  min-height: 48px;
  max-height: 180px;
  line-height: 1.5;
}
</style>
