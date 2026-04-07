import axios from 'axios'

const request = axios.create({
    baseURL: '/api',   // 代理转发到 8080
    timeout: 10000
})

// 请求拦截：自动带 token
request.interceptors.request.use(config => {
    const token = localStorage.getItem('satoken')
    if (token) {
        config.headers['satoken'] = token
    }
    return config
})

// 响应拦截：401 自动跳登录
request.interceptors.response.use(
    res => res.data,
    err => {
        const code = err.response?.data?.code || err.response?.status
        if (code === 401) {
            localStorage.removeItem('satoken')
            localStorage.removeItem('userInfo')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

export const authApi = {
    login:    (data) => request.post('/auth/login', data),
    register: (data) => request.post('/auth/register', data),
    logout:   ()     => request.post('/auth/logout')
}

export default request