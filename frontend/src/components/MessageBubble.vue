<template>
  <div :class="['flex', isUser ? 'justify-end' : 'justify-start']">
    <div :class="['flex items-start gap-3', isUser ? 'flex-row-reverse' : '']">
      <div
        :class="[
          'h-8 w-8 flex-shrink-0 rounded-full flex items-center justify-center text-sm font-medium',
          isUser
            ? 'bg-blue-500 text-white'
            : isDark ? 'bg-gray-600 text-green-400' : 'bg-green-100 text-green-600'
        ]"
      >
        <i :class="isUser ? 'fas fa-user' : 'fas fa-robot'"></i>
      </div>

      <div class="flex max-w-lg flex-col gap-1">
        <div
          :class="[
            'group relative rounded-2xl px-4 py-3 text-sm leading-relaxed',
            isUser
              ? 'rounded-tr-sm bg-blue-500 text-white'
              : isDark
                ? 'rounded-tl-sm bg-gray-700 text-gray-100'
                : 'rounded-tl-sm border border-gray-100 bg-white text-gray-800 shadow'
          ]"
        >
          <div v-if="isLoading" class="flex gap-1.5 py-1">
            <span
              v-for="i in 3"
              :key="i"
              class="h-2 w-2 rounded-full bg-gray-400 animate-bounce"
              :style="{ animationDelay: (i - 1) * 0.15 + 's' }"
            ></span>
          </div>

          <template v-else>
            <MarkdownRenderer v-if="!isUser" :content="content" :isDark="isDark" />
            <div v-else class="space-y-3">
              <div
                v-if="attachments.length"
                :class="[
                  'grid gap-2',
                  attachments.length === 1 ? 'grid-cols-1' : 'grid-cols-2'
                ]"
              >
                <img
                  v-for="attachment in attachments"
                  :key="attachment.id || attachment.previewUrl"
                  :src="attachment.previewUrl"
                  :alt="attachment.name"
                  class="max-h-60 w-full rounded-xl object-cover"
                />
              </div>
              <span v-if="userTextContent" class="whitespace-pre-wrap">{{ userTextContent }}</span>
            </div>
          </template>
        </div>

        <div v-if="!isUser && !isLoading" class="flex items-center gap-2 px-1">
          <button
            @click="copyContent"
            class="flex items-center gap-1 rounded-md px-2 py-1 text-xs transition"
            :class="isDark
              ? 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
              : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'"
          >
            <i :class="copied ? 'fas fa-check text-green-500' : 'far fa-copy'"></i>
            <span>{{ copied ? '已复制' : '复制' }}</span>
          </button>

          <button
            @click="$emit('regenerate')"
            class="flex items-center gap-1 rounded-md px-2 py-1 text-xs transition"
            :class="isDark
              ? 'text-gray-400 hover:bg-gray-700 hover:text-gray-200'
              : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'"
          >
            <i class="fas fa-redo-alt"></i>
            <span>重新生成</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import MarkdownRenderer from './MarkdownRenderer.vue'

export default {
  name: 'MessageBubble',
  components: { MarkdownRenderer },
  props: {
    role: { type: String, required: true },
    content: { type: String, default: '' },
    attachments: { type: Array, default: () => [] },
    isLoading: { type: Boolean, default: false },
    isDark: { type: Boolean, default: false },
  },
  emits: ['regenerate'],
  setup(props) {
    const copied = ref(false)
    const isUser = computed(() => props.role === 'user')
    const userTextContent = computed(() => {
      if (!isUser.value || props.attachments.length === 0) {
        return props.content
      }
      return props.content.replace(/^\[图片\s*\d+\s*张\].*(?:\r?\n)?/, '').trimStart()
    })

    const copyContent = async () => {
      try {
        await navigator.clipboard.writeText(props.content)
        copied.value = true
        setTimeout(() => { copied.value = false }, 2000)
      } catch {
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

    return { copied, isUser, copyContent, userTextContent }
  }
}
</script>
