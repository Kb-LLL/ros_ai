import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked 使用 highlight.js
marked.setOptions({
    highlight(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            return hljs.highlight(code, { language: lang }).value
        }
        return hljs.highlightAuto(code).value
    },
    breaks: true,
    gfm: true,
})

export function renderMarkdown(text) {
    return marked(text || '')
}