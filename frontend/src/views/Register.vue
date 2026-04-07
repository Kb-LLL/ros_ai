<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-green-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <i class="fas fa-user-plus text-white text-2xl"></i>
        </div>
        <h1 class="text-2xl font-bold text-gray-800">创建账号</h1>
        <p class="text-gray-500 text-sm mt-1">加入 AI 顾问，开始智能对话</p>
      </div>

      <!-- 成功提示 -->
      <div v-if="successMsg"
           class="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-600 text-sm">
        {{ successMsg }}
      </div>
      <!-- 错误提示 -->
      <div v-if="errorMsg"
           class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
        {{ errorMsg }}
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            用户名 <span class="text-red-400">*</span>
          </label>
          <input
              v-model="form.username"
              type="text"
              placeholder="4-20位字母或数字"
              class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-transparent transition"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">昵称</label>
          <input
              v-model="form.nickname"
              type="text"
              placeholder="不填则使用用户名"
              class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-transparent transition"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            密码 <span class="text-red-400">*</span>
          </label>
          <div class="relative">
            <input
                v-model="form.password"
                :type="showPwd ? 'text' : 'password'"
                placeholder="至少6位"
                class="w-full border border-gray-300 rounded-lg px-4 py-3 pr-12 focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-transparent transition"
            />
            <button type="button" @click="showPwd = !showPwd"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
              <i :class="showPwd ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            确认密码 <span class="text-red-400">*</span>
          </label>
          <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-transparent transition"
          />
        </div>

        <button
            @click="handleRegister"
            :disabled="loading"
            class="w-full bg-green-500 hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg py-3 font-medium transition"
        >
          <span v-if="loading"><i class="fas fa-spinner fa-spin mr-2"></i>注册中...</span>
          <span v-else>注 册</span>
        </button>
      </div>

      <p class="text-center text-sm text-gray-500 mt-6">
        已有账号？
        <router-link to="/login" class="text-blue-500 hover:text-blue-700 font-medium">
          立即登录
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api/auth.js'

export default {
  name: 'Register',
  data() {
    return {
      form: { username: '', password: '', confirmPassword: '', nickname: '' },
      loading: false,
      errorMsg: '',
      successMsg: '',
      showPwd: false
    }
  },
  methods: {
    async handleRegister() {
      this.errorMsg = ''
      this.successMsg = ''

      const { username, password, confirmPassword } = this.form
      // 前端校验
      if (!username.trim()) { this.errorMsg = '请输入用户名'; return }
      if (!/^[a-zA-Z0-9]{4,20}$/.test(username)) {
        this.errorMsg = '用户名为4-20位字母或数字'; return
      }
      if (password.length < 6) { this.errorMsg = '密码不能少于6位'; return }
      if (password !== confirmPassword) { this.errorMsg = '两次密码不一致'; return }

      this.loading = true
      try {
        const res = await authApi.register(this.form)
        if (res.code === 200) {
          this.successMsg = '注册成功！3秒后跳转到登录页...'
          setTimeout(() => this.$router.push('/login'), 3000)
        } else {
          this.errorMsg = res.message || '注册失败'
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
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.auth-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
  padding: 2rem;
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;
}
</style>