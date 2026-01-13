/**
 * 校验与工具函数集合（与后端参数校验规则对齐）
 * 用途：在 API 层发起请求前进行统一的入参校验与清洗
 * 约定：
 * - 所有函数均为纯函数（无副作用）
 * - 非空判定统一使用 isNonEmpty
 * - 枚举合法性使用 inMapKeys 与后端定义的枚举对象对齐
 */
 
/**
 * 去除字符串首尾空白；非字符串原样返回
 * @param {any} v - 输入值
 * @returns {any} - 若为字符串返回裁剪后的字符串，否则原样返回
 */
export const trim = (v) => (typeof v === 'string' ? v.trim() : v)
 
/**
 * 判定值是否为非空字符串（去除首尾空白后长度>0）
 * @param {any} s - 输入值
 * @returns {boolean} - 非空返回 true；null/undefined 或空白字符串返回 false
 */
export const isNonEmpty = (s) => s !== null && s !== undefined && String(s).trim().length > 0
 
/**
 * 邮箱格式校验
 * 规则：^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$
 * @param {string} email - 邮箱字符串
 * @returns {boolean} - 格式合法返回 true，否则返回 false（含空值）
 */
export const isValidEmail = (email) => {
  if (!isNonEmpty(email)) return false
  const re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  return re.test(String(email).trim())
}
 
/**
 * 手机号格式校验
 * 规则：11 位纯数字
 * @param {string|number} phone - 手机号
 * @returns {boolean} - 格式合法返回 true，否则返回 false（含空值）
 */
export const isValidPhone = (phone) => {
  if (!isNonEmpty(phone)) return false
  const re = /^\d{11}$/
  return re.test(String(phone).trim())
}
 
/**
 * 密码强度校验（用于登录/注册/修改密码等）
 * 规则：长度 ≥ 8，且至少包含一个英文字母
 * @param {string} pwd - 密码
 * @returns {boolean} - 满足规则返回 true，否则返回 false（含空值）
 */
export const isValidPassword = (pwd) => {
  if (!isNonEmpty(pwd)) return false
  const s = String(pwd)
  return s.length >= 8 && /[a-zA-Z]/.test(s)
}
 
/**
 * 判断枚举对象是否包含指定键
 * 约定：枚举以普通对象导出（例如 { '0': '女', '1': '男' }）
 * @param {Object} map - 枚举对象（普通对象）
 * @param {any} value - 待判断的值（内部将字符串化）
 * @returns {boolean} - 若枚举对象存在该键返回 true，否则返回 false
 */
export const inMapKeys = (map, value) => {
  if (value === null || value === undefined) return false
  return Object.prototype.hasOwnProperty.call(map || {}, String(value))
}

/**
 * 颜色格式校验（#xxxxxx）
 * 规则：井号开头，后续 6 位十六进制字符
 * @param {string} color - 颜色字符串
 * @returns {boolean} - 格式合法返回 true，否则返回 false（含空值）
 */
export const isHexColor = (color) => /^#[0-9a-fA-F]{6}$/.test(String(color || '').trim())

/**
 * 最小长度校验：去空白后长度 ≥ max
 * @param {string} s - 待校验字符串
 * @param {number} max - 最小长度（含）
 * @returns {boolean}
 */
export const isMinLength = (s, min) => String(s ?? '').trim().length >= Number(min ?? 0)

/**
 * 最大长度校验：去空白后长度 ≤ max
 * @param {string} s - 待校验字符串
 * @param {number} max - 最大长度（含）
 * @returns {boolean}
 */
export const isMaxLength = (s, max) => String(s ?? '').trim().length <= Number(max ?? Infinity)

/**
 * 规范化为本地日期字符串：YYYY-MM-DD
 * @param {any} v
 * @returns {string|null}
 */
export const normalizeLocalDate = (v) => {
  try {
    if (v === null || v === undefined) return null
    const s = String(v).trim()
    if (!s) return null
    if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10)
    const d = new Date(v)
    if (isNaN(d.getTime())) return null
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${dd}`
  } catch (e) { return null }
}

/**
 * 比较两个 YYYY-MM-DD 字符串
 * @param {string} a
 * @param {string} b
 * @returns {number} -1: a<b, 0: 相等或非法, 1: a>b
 */
export const compareDateStrings = (a, b) => {
  const sa = String(a || '').slice(0, 10)
  const sb = String(b || '').slice(0, 10)
  if (!/^\d{4}-\d{2}-\d{2}$/.test(sa) || !/^\d{4}-\d{2}-\d{2}$/.test(sb)) return 0
  if (sa === sb) return 0
  return sa < sb ? -1 : 1
}
