<template>
  <div class="flex h-screen overflow-hidden"
       :class="darkMode ? 'bg-gray-900' : 'bg-gray-50'">

    <!-- ======== 左侧边栏 ======== -->
    <aside
        :class="[
        'flex flex-col flex-shrink-0 transition-all duration-300 border-r',
        darkMode ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200',
        sidebarOpen ? 'w-64' : 'w-0 overflow-hidden'
      ]"
    >
      <!-- 顶部：新建按钮 -->
      <div class="p-3 border-b flex-shrink-0"
           :class="darkMode ? 'border-gray-700' : 'border-gray-200'">
        <button @click="startNewConversation"
                class="w-full flex items-center gap-2 px-3 py-2.5 rounded-lg border text-sm font-medium transition"
                :class="darkMode
            ? 'border-gray-600 text-gray-200 hover:bg-gray-700'
            : 'border-gray-300 text-gray-700 hover:bg-gray-50'">
          <i class="fas fa-plus"></i>
          <span>新建对话</span>
        </button>
      </div>

      <!-- 会话列表 -->
      <div class="flex-1 overflow-y-auto py-2 px-2">
        <!-- 右键菜单遮罩 -->
        <div v-if="ctxMenu.show"
             class="fixed inset-0 z-40"
             @click="ctxMenu.show = false"
             @contextmenu.prevent="ctxMenu.show = false">
        </div>

        <!-- 右键菜单 -->
        <div v-if="ctxMenu.show"
             class="fixed z-50 bg-white border border-gray-200 rounded-lg shadow-lg py-1 w-36"
             :style="{ top: ctxMenu.y + 'px', left: ctxMenu.x + 'px' }">
          <button @click="triggerRename"
                  class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center gap-2">
            <i class="fas fa-edit text-gray-400"></i> 重命名
          </button>
          <button @click="triggerDelete"
                  class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2">
            <i class="fas fa-trash-alt text-red-400"></i> 删除
          </button>
        </div>

        <!-- 空状态 -->
        <div v-if="conversations.length === 0"
             class="text-center py-10 text-sm"
             :class="darkMode ? 'text-gray-500' : 'text-gray-400'">
          <i class="fas fa-comments text-2xl mb-2 block opacity-30"></i>
          暂无历史会话
        </div>

        <!-- 会话条目 -->
        <div
            v-for="conv in conversations"
            :key="conv.id"
            @click="switchConversation(conv.id)"
            @contextmenu.prevent="openCtxMenu($event, conv)"
            :class="[
            'group relative flex items-center gap-2 px-3 py-2.5 rounded-lg cursor-pointer mb-0.5 text-sm transition',
            currentConvId === conv.id
              ? darkMode ? 'bg-gray-600 text-white' : 'bg-blue-50 text-blue-700 font-medium'
              : darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'
          ]"
        >
          <i class="fas fa-comment-dots flex-shrink-0 text-xs opacity-60"></i>

          <!-- 标题 / 重命名输入框 -->
          <span v-if="editingId !== conv.id" class="truncate flex-1">{{ conv.title }}</span>
          <input
              v-else
              ref="renameInputRef"
              v-model="editingTitle"
              @blur="confirmRename(conv)"
              @keyup.enter="confirmRename(conv)"
              @keyup.esc="editingId = null"
              @click.stop
              class="flex-1 bg-transparent outline-none border-b text-sm min-w-0"
              :class="darkMode ? 'border-gray-400 text-white' : 'border-blue-500 text-gray-800'"
          />

          <!-- 悬停操作按钮 -->
          <div v-if="editingId !== conv.id"
               class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0">
            <button @click.stop="startRename(conv)"
                    class="p-1 rounded hover:bg-black/10"
                    title="重命名">
              <i class="fas fa-edit text-xs"></i>
            </button>
            <button @click.stop="deleteConversation(conv.id)"
                    class="p-1 rounded hover:bg-red-100 hover:text-red-500"
                    title="删除">
              <i class="fas fa-trash-alt text-xs"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 底部：用户信息 + 登出 -->
      <div class="p-3 border-t flex items-center gap-2 flex-shrink-0"
           :class="darkMode ? 'border-gray-700' : 'border-gray-200'">
        <div class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center
                    text-white text-sm font-bold flex-shrink-0">
          {{ avatarChar }}
        </div>
        <span class="text-sm truncate flex-1"
              :class="darkMode ? 'text-gray-300' : 'text-gray-700'">
          {{ userInfo.nickname || userInfo.username }}
        </span>
        <button @click="handleLogout" title="退出登录"
                class="p-1.5 rounded-lg transition flex-shrink-0"
                :class="darkMode
            ? 'text-gray-400 hover:bg-gray-700 hover:text-red-400'
            : 'text-gray-500 hover:bg-red-50 hover:text-red-500'">
          <i class="fas fa-sign-out-alt"></i>
        </button>
        <!-- 知识库入口（放在侧边栏底部按钮区） -->
        <div class="px-3 pb-2 flex-shrink-0">
          <button @click="$router.push('/knowledge')"
                  class="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition"
                  :class="darkMode
      ? 'text-gray-300 hover:bg-gray-700'
      : 'text-gray-600 hover:bg-gray-100'">
            <i class="fas fa-book text-purple-500"></i>
            <span>我的知识库</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- ======== 右侧主区域 ======== -->
    <div class="flex flex-col flex-1 min-w-0">

      <!-- 顶部导航 -->
      <header class="flex items-center px-4 py-3 border-b flex-shrink-0"
              :class="darkMode ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200 shadow-sm'">
        <!-- 折叠按钮 -->
        <button @click="sidebarOpen = !sidebarOpen"
                class="p-2 rounded-lg mr-3 transition"
                :class="darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-600 hover:bg-gray-100'">
          <i class="fas fa-bars"></i>
        </button>

        <span class="font-bold text-blue-600 text-lg">AI 顾问</span>

        <!-- 当前会话标题 -->
        <span v-if="currentConvTitle" class="ml-3 text-sm truncate max-w-xs"
              :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
          — {{ currentConvTitle }}
        </span>

        <div class="ml-auto flex items-center gap-2">
          <button @click="toggleDarkMode"
                  class="p-2 rounded-full transition"
                  :class="darkMode ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-600 hover:bg-gray-100'">
            <i :class="darkMode ? 'fas fa-sun' : 'fas fa-moon'"></i>
          </button>
        </div>
      </header>

      <!-- 消息区域 -->
      <main class="flex-1 overflow-y-auto p-4" ref="chatContainer"
            :class="darkMode ? 'bg-gray-900' : 'bg-gray-50'">

        <!-- 欢迎页 -->
        <div v-if="!currentConvId"
             class="h-full flex flex-col items-center justify-center gap-4 select-none">
          <div class="w-16 h-16 bg-blue-500 rounded-2xl flex items-center justify-center shadow-lg">
            <i class="fas fa-robot text-white text-2xl"></i>
          </div>
          <h2 class="text-xl font-semibold"
              :class="darkMode ? 'text-white' : 'text-gray-800'">
            你好，{{ userInfo.nickname || userInfo.username }}！
          </h2>
          <p class="text-sm" :class="darkMode ? 'text-gray-400' : 'text-gray-500'">
            点击「新建对话」开始聊天
          </p>
          <button @click="startNewConversation"
                  class="mt-2 px-6 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-xl font-medium transition shadow">
            <i class="fas fa-plus mr-2"></i>新建对话
          </button>
        </div>

        <!-- 消息列表 -->
        <!-- 消息列表替换 -->
        <div v-else class="max-w-3xl mx-auto space-y-6 pb-4">
          <MessageBubble
              v-for="(msg, idx) in messages"
              :key="idx"
              :role="msg.role"
              :content="msg.content"
              :isLoading="msg.isLoading"
              :isDark="darkMode"
              @regenerate="regenerateMessage(idx)"
          />
        </div>
      </main>

      <!-- 输入框 -->
      <footer class="border-t p-4 flex-shrink-0"
              :class="darkMode ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'">
        <div class="max-w-3xl mx-auto flex items-end gap-2">
          <textarea
              v-model="userInput"
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.ctrl.enter.exact.prevent="userInput += '\n'"
              @input="adjustHeight"
              :disabled="!currentConvId || isLoading"
              :placeholder="currentConvId ? '输入消息... (Enter发送，Ctrl+Enter换行)' : '请先新建或选择一个会话'"
              rows="1"
              ref="textareaRef"
              :class="['flex-1 border rounded-xl px-4 py-3 resize-none focus:outline-none focus:ring-2 transition text-sm',
              darkMode
                ? 'bg-gray-700 border-gray-600 text-white focus:ring-blue-400 placeholder-gray-500'
                : 'border-gray-300 focus:ring-blue-400 focus:border-transparent',
              (!currentConvId || isLoading) ? 'opacity-60 cursor-not-allowed' : '']"
          ></textarea>

          <button
              @click="isLoading ? stopResponse() : sendMessage()"
              :disabled="!currentConvId || (!userInput.trim() && !isLoading)"
              :class="['p-3 rounded-xl transition flex-shrink-0 text-white',
              isLoading
                ? 'bg-red-500 hover:bg-red-600'
                : 'bg-blue-500 hover:bg-blue-600',
              'disabled:opacity-50 disabled:cursor-not-allowed']"
          >
            <i :class="isLoading ? 'fas fa-stop' : 'fas fa-paper-plane'"></i>
          </button>
        </div>
        <!-- 提示词模板 -->
        <PromptTemplates
            :show="showTemplates"
            :isDark="darkMode"
            @select="val => { userInput = val; showTemplates = false }"
            @close="showTemplates = false"
        />

        <div class="max-w-3xl mx-auto">
          <!-- 工具栏 -->
          <div class="flex items-center gap-2 mb-2">
            <button @click="showTemplates = !showTemplates"
                    class="flex items-center gap-1 text-xs px-2 py-1 rounded-md border transition"
                    :class="darkMode
          ? 'border-gray-600 text-gray-400 hover:bg-gray-700'
          : 'border-gray-300 text-gray-500 hover:bg-gray-50'">
              <i class="fas fa-magic"></i>
              <span>模板</span>
            </button>
          </div>

          <div class="flex items-end gap-2">
            <textarea>...</textarea>
            <button>...</button>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth.js'
import { conversationApi } from '../api/conversation.js'
import MessageBubble from './MessageBubble.vue'
import PromptTemplates from './PromptTemplates.vue'

export default {
  name: 'ChatContainer',
  components: { MessageBubble, PromptTemplates },
  setup() {
    const showTemplates = ref(false)
    const router = useRouter()

    // ── 基础状态 ──────────────────────────────────
    const userInfo    = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
    const darkMode    = ref(localStorage.getItem('darkMode') === 'true')
    const sidebarOpen = ref(true)

    // ── 会话 ──────────────────────────────────────
    const conversations   = ref([])
    const currentConvId   = ref(null)
    const messages        = ref([])

    // ── 输入 ──────────────────────────────────────
    const userInput  = ref('')
    const isLoading  = ref(false)
    const chatContainer = ref(null)
    const textareaRef   = ref(null)

    // ── 重命名 ────────────────────────────────────
    const editingId    = ref(null)
    const editingTitle = ref('')
    const renameInputRef = ref(null)

    // ── 右键菜单 ──────────────────────────────────
    const ctxMenu = ref({ show: false, x: 0, y: 0, conv: null })

    let controller = null

    // ── 计算属性 ──────────────────────────────────
    const avatarChar = computed(() => {
      const name = userInfo.value.nickname || userInfo.value.username || '?'
      return name[0].toUpperCase()
    })

    const currentConvTitle = computed(() => {
      const c = conversations.value.find(c => c.id === currentConvId.value)
      return c ? c.title : ''
    })

    // ── 加载会话列表 ──────────────────────────────
    const loadConversations = async () => {
      try {
        const res = await conversationApi.list()
        if (res.code === 200) conversations.value = res.data || []
      } catch (e) {
        console.error('加载会话列表失败', e)
      }
    }

    // ── 新建会话 ──────────────────────────────────
    const startNewConversation = async () => {
      try {
        const res = await conversationApi.create()
        if (res.code === 200) {
          await loadConversations()
          await switchConversation(res.data)
        }
      } catch (e) {
        console.error('创建会话失败', e)
      }
    }

    // ── 切换会话 ──────────────────────────────────
    const switchConversation = async (id) => {
      if (currentConvId.value === id) return
      currentConvId.value = id
      messages.value = []
      try {
        const res = await conversationApi.getMessages(id)
        if (res.code === 200) {
          messages.value = (res.data || []).map(m => ({
            role: m.role,
            content: m.content,
            isLoading: false
          }))
          scrollToBottom()
        }
      } catch (e) {
        console.error('加载消息失败', e)
      }
    }

    // ── 删除会话 ──────────────────────────────────
    const deleteConversation = async (id) => {
      if (!confirm('确定删除这个会话吗？')) return
      try {
        await conversationApi.remove(id)
        conversations.value = conversations.value.filter(c => c.id !== id)
        if (currentConvId.value === id) {
          currentConvId.value = null
          messages.value = []
        }
      } catch (e) {
        console.error('删除失败', e)
      }
    }

    // ── 重命名 ────────────────────────────────────
    const startRename = (conv) => {
      editingId.value = conv.id
      editingTitle.value = conv.title
      nextTick(() => {
        const el = Array.isArray(renameInputRef.value)
            ? renameInputRef.value[0]
            : renameInputRef.value
        el?.focus()
        el?.select()
      })
    }

    const confirmRename = async (conv) => {
      const title = editingTitle.value.trim()
      editingId.value = null
      if (!title || title === conv.title) return
      try {
        await conversationApi.rename(conv.id, title)
        const c = conversations.value.find(c => c.id === conv.id)
        if (c) c.title = title
      } catch (e) {
        console.error('重命名失败', e)
      }
    }

    // ── 右键菜单 ──────────────────────────────────
    const openCtxMenu = (e, conv) => {
      // 边界处理，防止菜单超出屏幕
      const menuW = 144, menuH = 80
      const x = Math.min(e.clientX, window.innerWidth  - menuW - 8)
      const y = Math.min(e.clientY, window.innerHeight - menuH - 8)
      ctxMenu.value = { show: true, x, y, conv }
    }

    const triggerRename = () => {
      const conv = ctxMenu.value.conv
      ctxMenu.value.show = false
      if (conv) startRename(conv)
    }

    const triggerDelete = () => {
      const conv = ctxMenu.value.conv
      ctxMenu.value.show = false
      if (conv) deleteConversation(conv.id)
    }

    // ── 发送消息 ──────────────────────────────────
    const sendMessage = async () => {
      const text = userInput.value.trim()
      if (!text || isLoading.value || !currentConvId.value) return

      if (controller) controller.abort()
      controller = new AbortController()

      userInput.value = ''
      nextTick(adjustHeight)

      messages.value.push({ role: 'user',      content: text, isLoading: false })
      messages.value.push({ role: 'assistant', content: '',   isLoading: true  })
      scrollToBottom()
      isLoading.value = true

      // 持久化用户消息
      try {
        await conversationApi.saveMessage(currentConvId.value, 'user', text)
        // 首条消息后刷新列表（标题会被更新）
        const conv = conversations.value.find(c => c.id === currentConvId.value)
        if (conv?.title === '新对话') setTimeout(loadConversations, 600)
      } catch (e) {
        console.error('保存用户消息失败', e)
      }

      // 流式请求 AI
      let aiContent = ''
      try {
        const token = localStorage.getItem('satoken') || ''
        const resp = await fetch(
            `/api/chat?message=${encodeURIComponent(text)}&memoryId=${currentConvId.value}`,
            { signal: controller.signal, headers: { satoken: token } }
        )
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

        const reader  = resp.body.getReader()
        const decoder = new TextDecoder('utf-8')
        const lastIdx = messages.value.length - 1

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          aiContent += decoder.decode(value, { stream: true })
          messages.value[lastIdx].content   = aiContent
          messages.value[lastIdx].isLoading = false
          scrollToBottom()
        }
      } catch (e) {
        if (e.name !== 'AbortError') {
          const lastIdx = messages.value.length - 1
          messages.value[lastIdx].content   = '抱歉，发生错误，请重试。'
          messages.value[lastIdx].isLoading = false
        }
      } finally {
        isLoading.value = false
        controller = null
      }

      // 持久化 AI 回复
      if (aiContent) {
        try {
          await conversationApi.saveMessage(currentConvId.value, 'assistant', aiContent)
          loadConversations()  // 刷新 update_time 排序
        } catch (e) {
          console.error('保存AI消息失败', e)
        }
      }
    }
// 重新生成：删掉当前 AI 回复，重新请求后端
    const regenerateMessage = async (idx) => {
      if (isLoading.value) return

      let userMsgIndex = -1
      for (let i = idx - 1; i >= 0; i--) {
        if (messages.value[i].role === 'user') {
          userMsgIndex = i
          break
        }
      }
      if (userMsgIndex === -1) return

      const userMsg = messages.value[userMsgIndex].content

      if (controller) controller.abort()
      controller = new AbortController()

      messages.value = messages.value.slice(0, idx)
      messages.value.push({ role: 'assistant', content: '', isLoading: true })
      scrollToBottom()
      isLoading.value = true

      let aiContent = ''
      try {
        const token = localStorage.getItem('satoken') || ''
        const resp = await fetch(
            `/api/chat?message=${encodeURIComponent(userMsg)}&memoryId=${currentConvId.value}`,
            { signal: controller.signal, headers: { satoken: token } }
        )
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

        const reader  = resp.body.getReader()
        const decoder = new TextDecoder('utf-8')
        const lastIdx = messages.value.length - 1

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          aiContent += decoder.decode(value, { stream: true })
          messages.value[lastIdx].content   = aiContent
          messages.value[lastIdx].isLoading = false
          scrollToBottom()
        }
      } catch (e) {
        if (e.name !== 'AbortError') {
          const lastIdx = messages.value.length - 1
          messages.value[lastIdx].content   = '抱歉，发生错误，请重试。'
          messages.value[lastIdx].isLoading = false
        }
      } finally {
        isLoading.value = false
        controller = null
      }

      if (aiContent) {
        try {
          await conversationApi.saveMessage(currentConvId.value, 'assistant', aiContent)
          loadConversations()
        } catch (e) {
          console.error('保存AI消息失败', e)
        }
      }
    }
    // ── 停止响应 ──────────────────────────────────
    const stopResponse = () => {
      controller?.abort()
      const last = messages.value[messages.value.length - 1]
      if (last?.isLoading) last.isLoading = false
      isLoading.value = false
      controller = null
    }

    // ── 工具函数 ──────────────────────────────────
    const scrollToBottom = () => {
      nextTick(() => {
        if (chatContainer.value)
          chatContainer.value.scrollTop = chatContainer.value.scrollHeight
      })
    }

    const adjustHeight = () => {
      const el = textareaRef.value
      if (!el) return
      el.style.height = 'auto'
      el.style.height = Math.min(el.scrollHeight, 160) + 'px'
    }

    const toggleDarkMode = () => {
      darkMode.value = !darkMode.value
      localStorage.setItem('darkMode', darkMode.value)
    }

    const handleLogout = async () => {
      await authApi.logout()
      localStorage.removeItem('satoken')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }

    // ── 初始化 ────────────────────────────────────
    onMounted(async () => {
      await loadConversations()
      // 自动打开最近一次会话
      if (conversations.value.length > 0) {
        await switchConversation(conversations.value[0].id)
      }
    })

    return {
      userInfo, darkMode, sidebarOpen,
      conversations, currentConvId, messages,
      avatarChar, currentConvTitle,
      userInput, isLoading, chatContainer, textareaRef,
      editingId, editingTitle, renameInputRef,
      ctxMenu,
      startNewConversation, switchConversation,
      deleteConversation, startRename, confirmRename,
      openCtxMenu, triggerRename, triggerDelete,
      sendMessage, stopResponse, regenerateMessage,
      adjustHeight, toggleDarkMode, handleLogout, showTemplates,
    }
  }
}
</script>

<style scoped>
textarea {
  min-height: 48px;
  max-height: 160px;
  line-height: 1.5;
}
</style>