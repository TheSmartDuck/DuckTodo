import http from '@/utils/http'
import { trim, isNonEmpty } from '@/utils/validation'

/**
 * 获取当前用户的钉钉机器人配置列表
 * 接口路径: GET /api/dingtalk-robot-configs
 * @returns {Promise} 返回 R<List<DingTalkRobotConfig>>
 */
export const getMyDingTalkRobotList = () => {
  return http.get('/dingtalk-robot-configs')
}

/**
 * 获取单个钉钉机器人配置
 * 接口路径: GET /api/dingtalk-robot-configs/{robotId}
 * @param {string} robotId 配置ID (必填)
 * @returns {Promise} 返回 R<DingTalkRobotConfig>
 */
export const getDingTalkRobotConfigById = (robotId) => {
  const id = String(robotId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  return http.get(`/dingtalk-robot-configs/${encodeURIComponent(id)}`)
}

/**
 * 添加钉钉机器人配置
 * 接口路径: POST /api/dingtalk-robot-configs
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.robotName 机器人名称 (必填)
 * @param {string} params.dingtalkRobotToken 机器人Token (必填)
 * @param {string} params.dingtalkRobotSecret 机器人Secret (必填)
 * @param {string} params.dingtalkRobotKeyword 机器人关键字 (必填)
 * @returns {Promise} 返回 R<DingTalkRobotConfig>
 */
export const createDingTalkRobotConfig = (params = {}) => {
  const p = params || {}
  const payload = {
    robotName: trim(p.robotName),
    dingtalkRobotToken: trim(p.dingtalkRobotToken),
    dingtalkRobotSecret: trim(p.dingtalkRobotSecret),
    dingtalkRobotKeyword: trim(p.dingtalkRobotKeyword)
  }

  if (!isNonEmpty(payload.robotName)) throw new Error('机器人名称不能为空')
  if (!isNonEmpty(payload.dingtalkRobotToken)) throw new Error('机器人Token不能为空')
  if (!isNonEmpty(payload.dingtalkRobotSecret)) throw new Error('机器人Secret不能为空')
  if (!isNonEmpty(payload.dingtalkRobotKeyword)) throw new Error('机器人关键字不能为空')

  return http.post('/dingtalk-robot-configs', payload)
}

/**
 * 更改钉钉机器人配置
 * 接口路径: PUT /api/dingtalk-robot-configs/{robotId}
 * @param {string} robotId 配置ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.robotName] 机器人名称 (选填)
 * @param {string} [params.dingtalkRobotToken] 机器人Token (选填)
 * @param {string} [params.dingtalkRobotSecret] 机器人Secret (选填)
 * @param {string} [params.dingtalkRobotKeyword] 机器人关键字 (选填)
 * @returns {Promise} 返回 R<DingTalkRobotConfig>
 */
export const updateDingTalkRobotConfig = (robotId, params = {}) => {
  const id = String(robotId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  
  const p = params || {}
  const payload = {}
  if (p.robotName !== undefined) payload.robotName = trim(p.robotName)
  if (p.dingtalkRobotToken !== undefined) payload.dingtalkRobotToken = trim(p.dingtalkRobotToken)
  if (p.dingtalkRobotSecret !== undefined) payload.dingtalkRobotSecret = trim(p.dingtalkRobotSecret)
  if (p.dingtalkRobotKeyword !== undefined) payload.dingtalkRobotKeyword = trim(p.dingtalkRobotKeyword)
  
  return http.put(`/dingtalk-robot-configs/${encodeURIComponent(id)}`, payload)
}

/**
 * 删除钉钉机器人配置
 * 接口路径: DELETE /api/dingtalk-robot-configs/{robotId}
 * @param {string} robotId 配置ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const deleteDingTalkRobotConfig = (robotId) => {
  const id = String(robotId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  return http.del(`/dingtalk-robot-configs/${encodeURIComponent(id)}`)
}

export default {
  getMyDingTalkRobotList,
  getDingTalkRobotConfigById,
  createDingTalkRobotConfig,
  updateDingTalkRobotConfig,
  deleteDingTalkRobotConfig
}
