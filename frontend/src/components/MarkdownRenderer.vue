<template>
  <div
      class="markdown-body prose prose-sm max-w-none"
      :class="isDark ? 'prose-invert dark-md' : ''"
      v-html="renderedHtml"
  ></div>
</template>

<script>
import { computed } from 'vue'
import { renderMarkdown } from '../utils/markdown.js'
import 'highlight.js/styles/github-dark.css'

export default {
  name: 'MarkdownRenderer',
  props: {
    content: { type: String, default: '' },
    isDark:  { type: Boolean, default: false },
  },
  setup(props) {
    const renderedHtml = computed(() => renderMarkdown(props.content))
    return { renderedHtml }
  }
}
</script>

<style>
/* 代码块样式 */
.markdown-body pre {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 1rem;
  overflow-x: auto;
  position: relative;
  margin: 0.75rem 0;
}
.markdown-body code:not(pre code) {
  background: rgba(99,102,241,0.1);
  color: #6366f1;
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.875em;
}
.markdown-body pre code {
  background: transparent;
  color: #d4d4d4;
  padding: 0;
  font-size: 0.85em;
}
.markdown-body table {
  border-collapse: collapse;
  width: 100%;
  margin: 0.75rem 0;
}
.markdown-body th,
.markdown-body td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem 0.75rem;
  text-align: left;
}
.markdown-body th {
  background: #f9fafb;
  font-weight: 600;
}
.dark-md th { background: #374151; border-color: #4b5563; }
.dark-md td { border-color: #4b5563; }
.markdown-body p { margin: 0.4rem 0; }
.markdown-body ul, .markdown-body ol {
  padding-left: 1.5rem;
  margin: 0.4rem 0;
}
.markdown-body blockquote {
  border-left: 3px solid #6366f1;
  padding-left: 1rem;
  color: #6b7280;
  margin: 0.5rem 0;
}
</style>