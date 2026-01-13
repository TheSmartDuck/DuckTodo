import http from '@/utils/http'
import { trim, isNonEmpty } from '@/utils/validation'

/**
 * 获取当前用户的 LLM 配置列表
 * 接口路径: GET /api/llm-configs
 * @returns {Promise} 返回 R<List<LlmConfig>>
 */
export const getMyLlmConfigList = () => {
  return http.get('/llm-configs')
}

/**
 * 获取单个 LLM 配置详情
 * 接口路径: GET /api/llm-configs/{configId}
 * @param {string} configId 配置ID (必填)
 * @returns {Promise} 返回 R<LlmConfig>
 */
export const getLlmConfigById = (configId) => {
  const id = String(configId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  return http.get(`/llm-configs/${encodeURIComponent(id)}`)
}

/**
 * 添加 LLM 配置
 * 接口路径: POST /api/llm-configs
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.llmProvider 提供商 (必填)
 * @param {string} params.llmApiKey API Key (必填)
 * @param {string} params.llmApiUrl API URL (必填)
 * @param {string} params.llmModelName 模型名称 (必填)
 * @param {number} [params.llmModelTemperature] 温度 (选填)
 * @param {number} [params.llmModelThinking] 是否开启思考模式 (选填, 枚举: 0-否, 1-是)
 * @returns {Promise} 返回 R<LlmConfig>
 */
export const createLlmConfig = (params = {}) => {
  const p = params || {}
  const payload = {
    llmProvider: trim(p.llmProvider),
    llmApiKey: trim(p.llmApiKey),
    llmApiUrl: trim(p.llmApiUrl),
    llmModelName: trim(p.llmModelName)
  }
  
  // 确保数值类型
  if (p.llmModelTemperature !== undefined && p.llmModelTemperature !== '') {
    const t = Number(p.llmModelTemperature)
    if (Number.isFinite(t)) payload.llmModelTemperature = t
  }
  // 枚举值确保为数值类型
  if (p.llmModelThinking !== undefined && p.llmModelThinking !== '') {
    const thinking = Number(p.llmModelThinking)
    if ([0, 1].includes(thinking)) {
      payload.llmModelThinking = thinking
    } else {
      throw new Error('思考模式参数无效')
    }
  }

  // 必填校验
  if (!isNonEmpty(payload.llmProvider)) throw new Error('LLM提供商不能为空')
  if (!isNonEmpty(payload.llmApiKey)) throw new Error('LLM API Key不能为空')
  if (!isNonEmpty(payload.llmApiUrl)) throw new Error('LLM API地址不能为空')
  if (!isNonEmpty(payload.llmModelName)) throw new Error('模型名称不能为空')

  return http.post('/llm-configs', payload)
}

/**
 * 更新 LLM 配置
 * 接口路径: PUT /api/llm-configs/{configId}
 * @param {string} configId 配置ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.llmProvider] 提供商 (选填)
 * @param {string} [params.llmApiKey] API Key (选填)
 * @param {string} [params.llmApiUrl] API URL (选填)
 * @param {string} [params.llmModelName] 模型名称 (选填)
 * @param {number} [params.llmModelTemperature] 温度 (选填)
 * @param {number} [params.llmModelThinking] 是否开启思考模式 (选填, 枚举: 0-否, 1-是)
 * @returns {Promise} 返回 R<LlmConfig>
 */
export const updateLlmConfig = (configId, params = {}) => {
  const id = String(configId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  
  const p = params || {}
  const payload = {}
  if (p.llmProvider !== undefined) payload.llmProvider = trim(p.llmProvider)
  if (p.llmApiKey !== undefined) payload.llmApiKey = trim(p.llmApiKey)
  if (p.llmApiUrl !== undefined) payload.llmApiUrl = trim(p.llmApiUrl)
  if (p.llmModelName !== undefined) payload.llmModelName = trim(p.llmModelName)
  
  // 确保数值类型
  if (p.llmModelTemperature !== undefined && p.llmModelTemperature !== '') {
    const t = Number(p.llmModelTemperature)
    if (Number.isFinite(t)) payload.llmModelTemperature = t
  }
  // 枚举值确保为数值类型
  if (p.llmModelThinking !== undefined && p.llmModelThinking !== '') {
    const thinking = Number(p.llmModelThinking)
    if ([0, 1].includes(thinking)) {
      payload.llmModelThinking = thinking
    } else {
      throw new Error('思考模式参数无效')
    }
  }

  return http.put(`/llm-configs/${encodeURIComponent(id)}`, payload)
}

/**
 * 删除 LLM 配置
 * 接口路径: DELETE /api/llm-configs/{configId}
 * @param {string} configId 配置ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const deleteLlmConfig = (configId) => {
  const id = String(configId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少配置ID')
  return http.del(`/llm-configs/${encodeURIComponent(id)}`)
}

export default {
  getMyLlmConfigList,
  getLlmConfigById,
  createLlmConfig,
  updateLlmConfig,
  deleteLlmConfig
}
