<template>
  <div v-if="show"
       class="absolute bottom-full left-0 right-0 mb-2 z-30">
    <div :class="['rounded-xl border shadow-lg p-4',
      isDark ? 'bg-gray-800 border-gray-600' : 'bg-white border-gray-200']">
      <p class="text-xs font-medium mb-3"
         :class="isDark ? 'text-gray-400' : 'text-gray-500'">
        选择场景模板快速开始
      </p>
      <div class="grid grid-cols-3 gap-2">
        <button
            v-for="tpl in templates"
            :key="tpl.label"
            @click="selectTemplate(tpl)"
            class="flex flex-col items-start gap-1 p-3 rounded-lg border text-sm transition text-left"
            :class="isDark
            ? 'border-gray-600 hover:bg-gray-700 text-gray-300'
            : 'border-gray-200 hover:bg-gray-50 text-gray-700'">
          <span class="text-base">{{ tpl.icon }}</span>
          <span class="font-medium text-xs">{{ tpl.label }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PromptTemplates',
  props: {
    show:   { type: Boolean, default: false },
    isDark: { type: Boolean, default: false },
  },
  emits: ['select', 'close'],
  setup(_, { emit }) {
    const templates = [
      { icon: '📝', label: '合同审查', prompt: '请帮我审查以下合同条款，指出风险点和不合理之处：\n\n' },
      { icon: '🔍', label: '竞品分析', prompt: '请帮我分析以下产品与竞争对手的差异，从功能、定价、用户体验角度：\n\n' },
      { icon: '📊', label: '数据解读', prompt: '请帮我解读以下数据，找出关键趋势和异常点：\n\n' },
      { icon: '💡', label: '头脑风暴', prompt: '请围绕以下主题，帮我进行头脑风暴，给出10个创新想法：\n\n主题：' },
      { icon: '📧', label: '邮件撰写', prompt: '请帮我撰写一封专业的邮件，要求：\n- 收件人：\n- 主题：\n- 核心内容：\n' },
      { icon: '🐛', label: '代码调试', prompt: '请帮我分析以下代码的问题并给出修复方案：\n\n```\n\n```' },
      { icon: '📋', label: '会议总结', prompt: '请将以下会议记录整理成结构化的会议纪要，包含决议和待办事项：\n\n' },
      { icon: '🌐', label: '文章翻译', prompt: '请将以下内容翻译成中文，保持专业术语准确：\n\n' },
      { icon: '✍️', label: '内容优化', prompt: '请帮我优化以下文案，使其更专业、更有说服力：\n\n' },
    ]

    const selectTemplate = (tpl) => {
      emit('select', tpl.prompt)
      emit('close')
    }

    return { templates, selectTemplate }
  }
}
</script>