<template>
  <div :class="['flex', isUser ? 'justify-end' : 'justify-start']">
    <div :class="['flex items-start gap-3', isUser ? 'flex-row-reverse' : '']">

      <!-- 头像 -->
      <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 text-sm font-medium',
        isUser
          ? 'bg-blue-500 text-white'
          : isDark ? 'bg-gray-600 text-green-400' : 'bg-green-100 text-green-600']">
        <i :class="isUser ? 'fas fa-user' : 'fas fa-robot'"></i>
      </div>

      <!-- 气泡 + 操作栏 -->
      <div class="flex flex-col gap-1 max-w-lg">
        <div :class="['px-4 py-3 rounded-2xl text-sm leading-relaxed group relative',
          isUser
            ? 'bg-blue-500 text-white rounded-tr-sm'
            : isDark
              ? 'bg-gray-700 text-gray-100 rounded-tl-sm'
              : 'bg-white text-gray-800 shadow border border-gray-100 rounded-tl-sm']">

          <!-- 加载动画 -->
          <div v-if="isLoading" class="flex gap-1.5 py-1">
            <span v-for="i in 3" :key="i"
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                  :style="{ animationDelay: (i-1)*0.15 + 's' }"></span>
          </div>

          <!-- 内容：AI 用 Markdown，用户用纯文本 -->
          <template v-else>
            <MarkdownRenderer v-if="!isUser" :content="content" :isDark="isDark" />
            <span v-else class="whitespace-pre-wrap">{{ content }}</span>
          </template>
        </div>

        <!-- 操作按钮（AI 消息才显示） -->
        <div v-if="!isUser && !isLoading"
             class="flex items-center gap-2 px-1">

          <!-- 复制 -->
          <button @click="copyContent"
                  class="flex items-center gap-1 text-xs px-2 py-1 rounded-md transition"
                  :class="isDark
              ? 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
              : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'">
            <i :class="copied ? 'fas fa-check text-green-500' : 'far fa-copy'"></i>
            <span>{{ copied ? '已复制' : '复制' }}</span>
          </button>

          <!-- 重新生成 -->
          <button @click="$emit('regenerate')"
                  class="flex items-center gap-1 text-xs px-2 py-1 rounded-md transition"
                  :class="isDark
              ? 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
              : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'">
            <i class="fas fa-redo-alt"></i>
            <span>重新生成</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'

export default {
  name: 'MessageBubble',
  components: { MarkdownRenderer },
  props: {
    role:      { type: String,  required: true },
    content:   { type: String,  default: '' },
    isLoading: { type: Boolean, default: false },
    isDark:    { type: Boolean, default: false },
  },
  emits: ['regenerate'],
  setup(props) {
    const copied = ref(false)
    const isUser = props.role === 'user'

    const copyContent = async () => {
      try {
        await navigator.clipboard.writeText(props.content)
        copied.value = true
        setTimeout(() => { copied.value = false }, 2000)
      } catch {
        // 降级方案
        const el = document.createElement('textarea')
        el.value = props.content
        document.body.appendChild(el)
        el.select()
        document.execCommand('copy')
        document.body.removeChild(el)
        copied.value = true
        setTimeout(() => { copied.value = false }, 2000)
      }
    }

    return { copied, isUser, copyContent }
  }
}
</script>