import http from '@/utils/http'
import { userSexByCode } from '@/utils/enums'
import { trim, isNonEmpty, isValidEmail, isValidPhone, isValidPassword, inMapKeys } from '@/utils/validation'

/**
 * 获取当前登录用户信息
 * 接口路径: GET /api/user/me
 * @returns {Promise} 返回 R<User>
 */
export const getMe = () => {
  return http.get('/user/me')
}

/**
 * 根据 userId 获取指定用户信息
 * 接口路径: GET /api/user/{userId}
 * @param {string|number} userId 用户ID (必填)
 * @returns {Promise} 返回 R<User>
 */
export const getUserById = (userId) => {
  const id = String(userId || '').trim()
  if (!isNonEmpty(id)) throw new Error('用户ID不能为空')
  return http.get(`/user/${encodeURIComponent(id)}`)
}

/**
 * 修改当前用户个人信息
 * 接口路径: PUT /api/user/me
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.userEmail] 邮箱 (选填)
 * @param {string} [params.userPhone] 手机号 (选填)
 * @param {number} [params.userSex] 性别 (选填, 枚举: 0-女, 1-男, 2-保密)
 * @param {string} [params.userRemark] 备注 (选填)
 * @returns {Promise} 返回 R<User>
 */
export const updateMe = (params = {}) => {
  const p = params || {}
  const payload = {}
  
  if (p.userEmail !== undefined) payload.userEmail = trim(p.userEmail)
  if (p.userPhone !== undefined) payload.userPhone = trim(p.userPhone)
  if (p.userSex !== undefined) payload.userSex = p.userSex
  if (p.userRemark !== undefined) payload.userRemark = trim(p.userRemark)

  // 校验逻辑
  if (payload.userEmail !== undefined && isNonEmpty(payload.userEmail)) {
    if (!isValidEmail(payload.userEmail)) throw new Error('邮箱格式不正确')
  }
  
  if (payload.userPhone !== undefined && isNonEmpty(payload.userPhone)) {
    if (!isValidPhone(payload.userPhone)) throw new Error('手机号格式不正确')
  }

  if (payload.userSex !== undefined && payload.userSex !== null && payload.userSex !== '') {
    // 确保为数值类型
    const sexNum = Number(payload.userSex)
    if (!Number.isFinite(sexNum)) throw new Error('性别必须为数值类型')
    payload.userSex = sexNum
    
    if (!inMapKeys(userSexByCode, payload.userSex)) throw new Error('性别参数无效')
  }

  return http.put('/user/me', payload)
}

/**
 * 修改当前用户密码
 * 接口路径: PUT /api/user/me/password
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.originalPassword 原密码 (必填)
 * @param {string} params.newPassword 新密码 (必填, ≥8且包含英文)
 * @returns {Promise} 返回 R<void>
 */
export const updatePassword = ({ originalPassword, newPassword } = {}) => {
  const op = trim(originalPassword)
  const np = trim(newPassword)
  
  if (!isNonEmpty(op)) throw new Error('原密码不能为空')
  if (!isNonEmpty(np)) throw new Error('新密码不能为空')
  if (!isValidPassword(np)) throw new Error('新密码需8位以上且包含英文')
  
  return http.put('/user/me/password', { originalPassword: op, newPassword: np })
}

/**
 * 获取当前用户的 AK/SK
 * 接口路径: GET /api/user/me/access-keys
 * @returns {Promise} 返回 R<Object> { userAccesskey, userSecretkey }
 */
export const getAccessKeys = () => {
  return http.get('/user/me/access-keys')
}

/**
 * 更新当前用户的 AK/SK
 * 接口路径: PUT /api/user/me/access-keys
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.userAccesskey Access Key (必填)
 * @param {string} params.userSecretkey Secret Key (必填)
 * @returns {Promise} 返回 R<Object> { userAccesskey, userSecretkey }
 */
export const updateAccessKeys = ({ userAccesskey, userSecretkey } = {}) => {
  const ak = trim(userAccesskey)
  const sk = trim(userSecretkey)
  
  if (!isNonEmpty(ak)) throw new Error('Access Key不能为空')
  if (!isNonEmpty(sk)) throw new Error('Secret Key不能为空')
  
  return http.put('/user/me/access-keys', { userAccesskey: ak, userSecretkey: sk })
}

/**
 * 删除当前用户的 AK/SK
 * 接口路径: DELETE /api/user/me/access-keys
 * @returns {Promise} 返回 R<Object>
 */
export const deleteAccessKeys = () => {
  return http.del('/user/me/access-keys')
}

/**
 * 上传并更新当前用户头像
 * 接口路径: POST /api/user/me/avatar
 * @param {File} file 图片文件 (必填)
 * @returns {Promise} 返回 R<User>
 */
export const updateAvatar = (file) => {
  if (!file) throw new Error('文件为空')
  const type = file.type || ''
  if (!type || String(type).indexOf('image/') !== 0) throw new Error('仅支持图片类型')
  return http.upload('/user/me/avatar', file, 'file')
}

/**
 * 分页查询用户列表
 * 接口路径: GET /api/user
 * @param {Object} params 参数对象 (选填)
 * @param {number} [params.page] 页码 (选填, 默认1)
 * @param {number} [params.size] 每页条数 (选填, 默认10, 最大100)
 * @param {string} [params.userName] 用户名 (选填, 模糊)
 * @param {string} [params.userEmail] 邮箱 (选填, 模糊)
 * @param {string} [params.userPhone] 手机号 (选填, 模糊)
 * @returns {Promise} 返回 R<Page<User>>
 */
export const listUsers = ({ page = 1, size = 10, userName, userEmail, userPhone } = {}) => {
  const p = Number(page)
  const s = Number(size)
  
  const query = {
    page: Number.isFinite(p) && p > 0 ? p : 1,
    size: Number.isFinite(s) && s > 0 ? (s > 100 ? 100 : s) : 10
  }
  
  if (isNonEmpty(userName)) query.userName = trim(userName)
  if (isNonEmpty(userEmail)) query.userEmail = trim(userEmail)
  if (isNonEmpty(userPhone)) query.userPhone = trim(userPhone)
  
  return http.get('/user', query)
}

export default {
  getMe,
  getUserById,
  updateMe,
  updatePassword,
  getAccessKeys,
  updateAccessKeys,
  deleteAccessKeys,
  updateAvatar,
  listUsers
}
