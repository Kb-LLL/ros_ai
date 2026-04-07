<template>
  <!-- 用 RouterView 完全替换，不渲染任何额外 DOM -->
  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
/* 全局禁止页面超出滚动 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;  /* 关键：禁止 body 滚动 */
}

#app {
  height: 100%;
}
</style>