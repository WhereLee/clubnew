import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const TOKEN_KEY = 'token'
const REFRESH_KEY = 'refreshToken'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY)
export const setTokens = (token: string, refreshToken?: string) => {
  localStorage.setItem(TOKEN_KEY, token)
  if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
}
export const clearTokens = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_TYPE_KEY)
}
const USER_TYPE_KEY = 'userType'
export const getUserType = () => localStorage.getItem(USER_TYPE_KEY) || ''
export const setUserType = (t: string) => localStorage.setItem(USER_TYPE_KEY, t)

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

/**
 * 静默续期：
 * - 401 时用 refresh token 换新 access + 新 refresh（轮换），成功后重放原请求；
 * - 并发 401 只发起一次 refresh（共享 in-flight Promise），避免轮换冲突触发服务端复用检测；
 * - refresh 本身失败（或本身就是 refresh 请求的 401）→ 清空会话跳登录。
 */
let refreshPromise: Promise<string> | null = null

const doRefresh = (): Promise<string> => {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const refreshToken = getRefreshToken()
      if (!refreshToken) throw new Error('no refresh token')
      const res = await axios.post('/api/auth/refresh', { refreshToken })
      if (res.data.code !== 200) throw new Error(res.data.msg || 'refresh failed')
      setTokens(res.data.data.token, res.data.data.refreshToken)
      return res.data.data.token as string
    })().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

const toLogin = () => {
  clearTokens()
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        return handle401(response.config)
      }
      if (res.code === 403) {
        ElMessage.error('没有权限')
      } else {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg))
    }
    return res
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      return handle401(error.config)
    }
    // 协议级错误（400/403/404/405/500）优先展示后端 msg，而非 axios 原始英文提示
    const msg = error.response?.data?.msg || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

/** 401 统一处理：refresh 接口自身的 401 直接跳登录；其余请求尝试静默续期后重放 */
const handle401 = async (config: any) => {
  if (config?.url?.includes('/auth/refresh') || config?.url?.includes('/auth/login')) {
    toLogin()
    return Promise.reject(new Error('登录已过期'))
  }
  if (config?._retried) {
    // 重放后仍然 401：续期失败，跳登录
    toLogin()
    return Promise.reject(new Error('登录已过期'))
  }
  try {
    const newToken = await doRefresh()
    config._retried = true
    config.headers.Authorization = `Bearer ${newToken}`
    return request(config)
  } catch {
    toLogin()
    return Promise.reject(new Error('登录已过期'))
  }
}

export default request
