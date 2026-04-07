<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-blue-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <i class="fas fa-robot text-white text-2xl"></i>
        </div>
        <h1 class="text-2xl font-bold text-gray-800">AI 顾问</h1>
        <p class="text-gray-500 text-sm mt-1">登录后开始智能对话</p>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg"
           class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
        {{ errorMsg }}
      </div>

      <!-- 表单 -->
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              @keyup.enter="handleLogin"
              class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <div class="relative">
            <input
                v-model="form.password"
                :type="showPwd ? 'text' : 'password'"
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
                class="w-full border border-gray-300 rounded-lg px-4 py-3 pr-12 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent transition"
            />
            <button type="button" @click="showPwd = !showPwd"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
              <i :class="showPwd ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <!-- 记住我 -->
        <div class="flex items-center">
          <label class="flex items-center gap-2 cursor-pointer text-sm text-gray-600">
            <input v-model="form.rememberMe" type="checkbox" class="rounded" />
            记住我（30天）
          </label>
        </div>

        <button
            @click="handleLogin"
            :disabled="loading"
            class="w-full bg-blue-500 hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg py-3 font-medium transition"
        >
          <span v-if="loading"><i class="fas fa-spinner fa-spin mr-2"></i>登录中...</span>
          <span v-else>登 录</span>
        </button>
      </div>

      <p class="text-center text-sm text-gray-500 mt-6">
        还没有账号？
        <router-link to="/register" class="text-blue-500 hover:text-blue-700 font-medium">
          立即注册
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api/auth.js'

export default {
  name: 'Login',
  data() {
    return {
      form: { username: '', password: '', rememberMe: false },
      loading: false,
      errorMsg: '',
      showPwd: false
    }
  },
  methods: {
    async handleLogin() {
      this.errorMsg = ''
      if (!this.form.username.trim()) { this.errorMsg = '请输入用户名'; return }
      if (!this.form.password)        { this.errorMsg = '请输入密码'; return }

      this.loading = true
      try {
        const res = await authApi.login(this.form)
        if (res.code === 200) {
          localStorage.setItem('satoken', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify({
            username: res.data.username,
            nickname: res.data.nickname
          }))
          this.$router.push('/')
        } else {
          this.errorMsg = res.message || '登录失败'
        }
      } catch (e) {
        this.errorMsg = '网络错误，请检查后端是否启动'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  position: fixed;       /* fixed 定位，完全覆盖屏幕 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;      /* 禁止滚动 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%);
}

.auth-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
  padding: 2rem;
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;      /* 卡片内容太多时允许卡片内部滚动 */
}
</style>