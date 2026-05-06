<template>
  <div v-if="show" class="absolute bottom-full left-0 right-0 z-30 mb-2">
    <div
      :class="[
        'rounded-xl border p-4 shadow-lg',
        isDark ? 'border-gray-600 bg-gray-800' : 'border-gray-200 bg-white'
      ]"
    >
      <p class="mb-3 text-xs font-medium" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
        选择一个提示模板，快速开始提问
      </p>

      <div class="grid grid-cols-3 gap-2">
        <button
          v-for="tpl in templates"
          :key="tpl.label"
          @click="selectTemplate(tpl)"
          class="flex flex-col items-start gap-1 rounded-lg border p-3 text-left text-sm transition"
          :class="isDark
            ? 'border-gray-600 text-gray-300 hover:bg-gray-700'
            : 'border-gray-200 text-gray-700 hover:bg-gray-50'"
        >
          <span class="text-base">{{ tpl.icon }}</span>
          <span class="text-xs font-medium">{{ tpl.label }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PromptTemplates',
  props: {
    show: { type: Boolean, default: false },
    isDark: { type: Boolean, default: false },
  },
  emits: ['select', 'close'],
  setup(_, { emit }) {
    const templates = [
      { icon: '📄', label: '合同审查', prompt: '请帮我审查以下合同条款，指出风险点和不合理之处：\n\n' },
      { icon: '📊', label: '竞品分析', prompt: '请帮我分析以下产品与竞争对手的差异，从功能、定价、用户体验角度展开：\n\n' },
      { icon: '📈', label: '数据解读', prompt: '请帮我解读以下数据，找出关键趋势和异常点：\n\n' },
      { icon: '💡', label: '头脑风暴', prompt: '请围绕以下主题，帮我做一轮头脑风暴，给出 10 个创新想法：\n\n主题：' },
      { icon: '✉️', label: '邮件撰写', prompt: '请帮我撰写一封专业邮件，要求：\n- 收件人：\n- 主题：\n- 核心内容：\n' },
      { icon: '💻', label: '代码调试', prompt: '请帮我分析以下代码的问题，并给出修复方案：\n\n```\n\n```' },
      { icon: '📝', label: '会议总结', prompt: '请将以下会议记录整理成结构化纪要，包含决议和待办事项：\n\n' },
      { icon: '🌐', label: '文章翻译', prompt: '请将以下内容翻译成中文，保持术语准确、表达自然：\n\n' },
      { icon: '✨', label: '内容优化', prompt: '请帮我优化以下文案，让它更专业、更有说服力：\n\n' },
    ]

    const selectTemplate = (tpl) => {
      emit('select', tpl.prompt)
      emit('close')
    }

    return { templates, selectTemplate }
  }
}
</script>
