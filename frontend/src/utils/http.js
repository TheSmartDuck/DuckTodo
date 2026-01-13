import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

// 统一 HTTP 工具（axios 二次封装）
// 设计目标与对齐说明：
// - 与后端返回结构 R 保持一致：{ success, code, message, data }
// - 自动注入 JWT（Authorization: Bearer <token>、token、X-Auth-Token）
// - 支持令牌刷新来源：响应头（Authorization / token）与响应体（data.token）
// - 统一未授权处理：清理令牌并跳转 /login
// - 提供上传/下载与常规 CRUD 方法，减少重复代码
// - 基于 /api 作为默认代理前缀（dev 由 vue.config.js 代理，prod 部署同域反向代理）

// Axios 二次封装：统一基础配置
const getBase = () => {
  try {
    const w = typeof window !== 'undefined' ? window : {}
    const v = (w && w.__DUCKTODO_API_BASE__) || (typeof process !== 'undefined' && process.env && process.env.VUE_APP_API_BASE) || '/api'
    return String(v || '/api')
  } catch (e) { return '/api' }
}
const base = getBase()
const instance = axios.create({
  baseURL: base,
  timeout: 15000,
  // 开启跨站点凭据：若后端依赖 Set-Cookie 或需要携带跨域 Cookie
  // 注意：同时需要服务端设置 CORS 的 Access-Control-Allow-Credentials=true
  withCredentials: true
})

// 请求拦截：自动注入token
instance.interceptors.request.use(
  (config) => {
    // 规范化请求路径：
    // - 若 baseURL 以 /api 结尾，且调用方 URL 也以 /api 开头，则去重前缀，避免 /api/api/xxx
    // - 保留绝对 URL（http/https）不处理
    try {
      const b = String(instance.defaults.baseURL || '')
      let u = String(config.url || '')
      const abs = /^https?:\/\//i.test(u)
      if (!abs) {
        const endsWithApi = /\/api$/.test(b.replace(/\/+$/, ''))
        if (endsWithApi && /^\/api\/?/i.test(u)) {
          u = u.replace(/^\/api\/?/i, '/')
        }
        if (!u.startsWith('/')) u = '/' + u
        config.url = u
      }
    } catch (e) {}
    // 注入令牌：
    // - Authorization: Bearer <token>（通用）
    // - token / X-Auth-Token（兼容后端拦截器自定义头）
    const token = localStorage.getItem('token') || localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      config.headers.token = token
      config.headers['X-Auth-Token'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：按后端通用返回 R 结构处理
instance.interceptors.response.use(
  (response) => {
    // 优先从响应头读取刷新后的令牌（后端拦截器会在鉴权成功后下发）
    // 兼容多种头名：authorization/token/x-auth-token/x-token（大小写差异）
    try {
      const headers = response && response.headers ? response.headers : {}
      const auth = headers['authorization'] || headers['Authorization']
      const hdrToken = headers['token'] || headers['x-auth-token'] || headers['X-Auth-Token'] || headers['x-token']
      let newToken = null
      if (hdrToken && typeof hdrToken === 'string' && hdrToken.trim().length > 0) {
        newToken = hdrToken.trim()
      } else if (auth && typeof auth === 'string' && auth.startsWith('Bearer ')) {
        newToken = auth.slice(7)
      }
      if (newToken) {
        try { localStorage.setItem('token', newToken) } catch (e) {}
      }
    } catch (e) {
      // 静默处理头解析异常
    }

    const res = response.data
    // 兼容多种返回结构：
    // 1) R 结构：{ success, code, data, message }
    // 2) code 结构：{ code, data?, message }，约定 code=0 或 200 为成功
    // 3) 其他：直接返回原始数据（例如文件下载的 blob）
    if (res && typeof res === 'object') {
      if ('success' in res && 'code' in res) {
        if (res.success) {
          try {
            const bodyToken = res && res.data && typeof res.data === 'object' ? res.data.token : null
            if (bodyToken && typeof bodyToken === 'string' && bodyToken.trim().length > 0) {
              localStorage.setItem('token', bodyToken.trim())
            }
          } catch (e) {}
          return res.data
        }
        const code = Number(res.code)
        const msg = res.message || '请求失败'
        // 未授权类错误统一处理：清理令牌并跳转登录
        if (code === 403) {
          Message.error(msg || '未授权或令牌已过期')
          try { localStorage.removeItem('token') } catch (e) {}
          if (router.currentRoute.path !== '/login') {
            router.replace('/login')
          }
        } else {
          Message.error(msg)
        }
        const err = new Error(msg)
        err.code = code
        err.raw = res
        return Promise.reject(err)
      }
      if ('code' in res && !('success' in res)) {
        const code = Number(res.code)
        if (code === 0 || code === 200) {
          return res.data !== undefined ? res.data : res
        }
        const msg = res.message || '请求失败'
        if (code === 401 || code === 403) {
          Message.error(msg || '未授权或令牌已过期')
          try { localStorage.removeItem('token') } catch (e) {}
          if (router.currentRoute.path !== '/login') {
            router.replace('/login')
          }
        } else {
          Message.error(msg)
        }
        const err = new Error(msg)
        err.code = code
        err.raw = res
        return Promise.reject(err)
      }
      return res
    }
    // 非 R 结构：直接返回原始数据
    return res
  },
  (error) => {
    // 网络层错误统一处理：
    // - 401/403/419/440 等未授权/会话过期 -> 清理令牌并跳转登录
    // - 5xx 服务器错误 -> 通用提示
    // - 其他 -> 使用服务端返回 message 或 error.message
    const status = error?.response?.status
    const msg = error?.response?.data?.message || error.message || '网络错误'
    if (status === 401 || status === 403 || status === 419 || status === 440) {
      Message.error('未授权或令牌已过期')
      try { localStorage.removeItem('token') } catch (e) {}
      if (router.currentRoute.path !== '/login') {
        router.replace('/login')
      }
    } else if (status >= 500) {
      Message.error('服务器异常，请稍后再试')
    } else {
      Message.error(msg)
    }
    return Promise.reject(error)
  }
)

// 统一方法封装
const http = {
  // 原始 axios request
  request: (config) => instance.request(config),
  // GET：params 拼接到查询字符串
  get: (url, params = {}, config = {}) => instance.get(url, { params, ...config }),
  // POST：JSON 请求体
  post: (url, data = {}, config = {}) => instance.post(url, data, config),
  // PUT：JSON 请求体
  put: (url, data = {}, config = {}) => instance.put(url, data, config),
  // PATCH：JSON 请求体（部分更新）
  patch: (url, data = {}, config = {}) => instance.patch(url, data, config),
  // DELETE：通过 params 传递查询参数
  del: (url, params = {}, config = {}) => instance.delete(url, { params, ...config }),
  // 上传：multipart/form-data；支持附加字段与自定义字段名
  upload: (url, file, fieldName = 'file', extraData = {}, config = {}) => {
    const form = new FormData()
    form.append(fieldName, file)
    Object.keys(extraData || {}).forEach((k) => form.append(k, extraData[k]))
    return instance.post(url, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      ...config
    })
  },
  // 下载：GET + responseType=blob；自动生成 a 链接触发保存
  download: (url, params = {}, filename = 'download', config = {}) => {
    return instance
      .get(url, { params, responseType: 'blob', ...config })
      .then((blob) => {
        const data = blob instanceof Blob ? blob : new Blob([blob])
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(data)
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(link.href)
        return true
      })
  }
}

export { instance }
export default http
// keep newline at EOF
