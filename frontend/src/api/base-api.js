import http from '@/utils/http'
import { userSexByCode } from '@/utils/enums'
import { trim, isNonEmpty, isValidEmail, isValidPhone, isValidPassword, inMapKeys } from '@/utils/validation'

// Base（基础）接口：与后端 BaseController 对齐
// - GET   /base/health
// - POST  /base/login
// - POST  /base/register
// - POST  /base/logout
//
// 入参与返回值均做了最小限度的控制与拆解：
// - 入参：仅允许白名单字段，进行字符串化与裁剪；
// - 返回：统一提取 data 并在必要处结构化（例如 login/register 提取 token 与 user）。

/**
 * 健康检查：返回后端应用信息与服务状态
 * 接口路径: GET /api/base/health
 * @returns {Promise} 返回 R<Object>
 */
export const health = async () => {
  const data = await http.get('/base/health')
  // 初步拆解：确保返回为对象并规范常用字段名称
  if (data && typeof data === 'object') {
    return data
  }else {
    throw new Error('无效的返回值')
  }
}

/**
 * 登录：允许 userName 或 userEmail，与 userPassword
 * 接口路径: POST /api/base/login
 * @param {Object} params 参数对象 (必填)
 * @param {string} [params.userName] 用户名 (选填, userName 与 userEmail 二选一必填)
 * @param {string} [params.userEmail] 邮箱 (选填, userName 与 userEmail 二选一必填)
 * @param {string} params.userPassword 密码 (必填)
 * @returns {Promise<Object>} 返回 { token, user }
 */
export const login = async (params = {}) => {
  const userName = trim(params.userName || '')
  const userEmail = trim(params.userEmail || '')
  const userPassword = trim(params.userPassword || '')

  if (!isNonEmpty(userName) && !isNonEmpty(userEmail)) {
    throw new Error('缺少用户名或邮箱')
  }
  if (!isNonEmpty(userPassword)) {
    throw new Error('缺少密码')
  }
  // 仅当提供了 email 且无 username 时，或者提供了 email 但格式不对时校验
  // 现有逻辑：只要提供了 email 就校验。这很合理。
  if (isNonEmpty(userEmail) && !isValidEmail(userEmail)) {
    throw new Error('邮箱格式不正确')
  }

  const payload = {}
  // 优先使用 userName，若无则使用 userEmail
  if (isNonEmpty(userName)) {
    payload.userName = userName
  } else {
    payload.userEmail = userEmail
  }
  payload.userPassword = userPassword

  const data = await http.post('/base/login', payload)
  // 初步拆解：返回 { token, user }
  const token = data && data.token ? String(data.token) : ''
  const user = data && data.user ? data.user : null
  return { token, user }
}

/**
 * 注册：注册新用户，并自动登录
 * 接口路径: POST /api/base/register
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.userName 用户名 (必填)
 * @param {string} params.userEmail 邮箱 (必填)
 * @param {string} params.userPassword 密码 (必填, 8位以上且包含英文)
 * @param {string} params.userPhone 手机号 (必填)
 * @param {number} params.userSex 性别 (必填, 枚举: 0-女, 1-男, 2-保密)
 * @returns {Promise<Object>} 返回 { token, user }
 */
export const register = async (params = {}) => {
  const payload = {
    userName: trim(params.userName || ''),
    userEmail: trim(params.userEmail || ''),
    userPassword: trim(params.userPassword || ''),
    userPhone: trim(params.userPhone || ''),
    userSex: params.userSex
  }
  // 必填校验
  if (!isNonEmpty(payload.userName)) {
    throw new Error('用户名不能为空')
  }
  if (!isNonEmpty(payload.userEmail)) {
    throw new Error('邮箱不能为空')
  }
  if (!isNonEmpty(payload.userPassword)) {
    throw new Error('密码不能为空')
  }
  if (!isNonEmpty(payload.userPhone)) {
    throw new Error('手机号不能为空')
  }
  if (payload.userSex === undefined || payload.userSex === null) {
    throw new Error('性别不能为空')
  }
  if (typeof payload.userSex !== 'number') {
    throw new Error('性别必须为数值类型')
  }
  if (!inMapKeys(userSexByCode, payload.userSex)) {
    throw new Error('性别参数无效')
  }
  // 格式校验
  if (!isValidEmail(payload.userEmail)) {
    throw new Error('邮箱格式不正确')
  }
  if (!isValidPassword(payload.userPassword)) {
    throw new Error('密码需8位以上且包含英文')
  }
  if (!isValidPhone(payload.userPhone)) {
    throw new Error('手机号格式不正确')
  }

  const data = await http.post('/base/register', payload)
  const token = data && data.token ? String(data.token) : ''
  const user = data && data.user ? data.user : null
  return { token, user }
}

/**
 * 登出：无入参，后端始终成功；前端丢弃令牌
 * 接口路径: POST /api/base/logout
 * @returns {Promise} 返回 R<void>
 */
export const logout = async () => {
  const data = await http.post('/base/logout', {})
  return data
}

export default {
  register,
  login,
  logout,
  health
}
