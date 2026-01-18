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
 * @param {string} params.llmProvider 提供商 (必填, openai/bailian/siliconflow/openai-compatible)
 * @param {string} params.llmApiKey API Key (必填)
 * @param {string} [params.llmApiUrl] API URL (仅在openai-compatible时必填)
 * @param {string} params.llmModelName 模型名称 (必填)
 * @param {number} params.llmModelType 模型类型 (必填, 1-chat, 2-embedding, 3-rerank)
 * @param {number} [params.llmModelTemperature] 温度 (选填, 0.0-1.0, 仅CHAT模型)
 * @param {number} [params.llmModelThinking] 是否开启思考模式 (选填, 枚举: 0-否, 1-是, 仅CHAT模型)
 * @returns {Promise} 返回 R<LlmConfig>
 */
export const createLlmConfig = (params = {}) => {
  const p = params || {}
  const payload = {}
  
  // 提供商（必填）
  payload.llmProvider = trim(p.llmProvider)
  if (!isNonEmpty(payload.llmProvider)) {
    throw new Error('LLM提供商不能为空')
  }
  
  // API Key（必填）
  payload.llmApiKey = trim(p.llmApiKey)
  if (!isNonEmpty(payload.llmApiKey)) {
    throw new Error('LLM API Key不能为空')
  }
  
  // API URL（仅在OpenAi兼容时必填）
  const provider = payload.llmProvider
  if (provider === 'openai-compatible') {
    // OpenAi兼容时，API URL必填
    payload.llmApiUrl = trim(p.llmApiUrl)
    if (!isNonEmpty(payload.llmApiUrl)) {
      throw new Error('LLM API地址不能为空')
    }
  } else if (p.llmApiUrl !== undefined && isNonEmpty(trim(p.llmApiUrl))) {
    // 非OpenAi兼容时，如果提供了URL也发送（可选）
    payload.llmApiUrl = trim(p.llmApiUrl)
  }
  
  // 模型名称（必填）
  payload.llmModelName = trim(p.llmModelName)
  if (!isNonEmpty(payload.llmModelName)) {
    throw new Error('模型名称不能为空')
  }
  
  // 模型类型（必填）
  if (p.llmModelType !== undefined && p.llmModelType !== '') {
    const type = Number(p.llmModelType)
    if ([1, 2, 3].includes(type)) {
      payload.llmModelType = type
    } else {
      throw new Error('模型类型参数无效，必须是 1-chat, 2-embedding, 3-rerank')
    }
  } else {
    throw new Error('模型类型不能为空')
  }
  
  // 温度（可选，仅CHAT模型）
  if (p.llmModelTemperature !== undefined && p.llmModelTemperature !== null && p.llmModelTemperature !== '') {
    const t = Number(p.llmModelTemperature)
    if (Number.isFinite(t) && t >= 0 && t <= 1) {
      payload.llmModelTemperature = t
    }
  }
  
  // 思考模式（可选，仅CHAT模型）
  if (p.llmModelThinking !== undefined && p.llmModelThinking !== '') {
    const thinking = Number(p.llmModelThinking)
    if ([0, 1].includes(thinking)) {
      payload.llmModelThinking = thinking
    } else {
      throw new Error('思考模式参数无效，必须是 0-否 或 1-是')
    }
  }

  return http.post('/llm-configs', payload)
}

/**
 * 更新 LLM 配置
 * 接口路径: PUT /api/llm-configs/{configId}
 * @param {string} configId 配置ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.llmProvider] 提供商 (选填, openai/bailian/siliconflow/openai-compatible)
 * @param {string} [params.llmApiKey] API Key (选填)
 * @param {string} [params.llmApiUrl] API URL (选填, 仅在openai-compatible时必填)
 * @param {string} [params.llmModelName] 模型名称 (选填)
 * @param {number} [params.llmModelType] 模型类型 (选填, 1-chat, 2-embedding, 3-rerank)
 * @param {number} [params.llmModelTemperature] 温度 (选填, 0.0-1.0, 仅CHAT模型)
 * @param {number} [params.llmModelThinking] 是否开启思考模式 (选填, 枚举: 0-否, 1-是, 仅CHAT模型)
 * @returns {Promise} 返回 R<LlmConfig>
 */
export const updateLlmConfig = (configId, params = {}) => {
  const id = String(configId || '').trim()
  if (!isNonEmpty(id)) {
    throw new Error('缺少配置ID')
  }
  
  const p = params || {}
  const payload = {}
  
  // 提供商（可选）
  if (p.llmProvider !== undefined) {
    payload.llmProvider = trim(p.llmProvider)
  }
  
  // API Key（可选）
  if (p.llmApiKey !== undefined) {
    payload.llmApiKey = trim(p.llmApiKey)
  }
  
  // API URL（可选，但如果提供商是openai-compatible则必填）
  const provider = payload.llmProvider || p.llmProvider
  if (provider === 'openai-compatible') {
    // OpenAi兼容时，如果提供了提供商，则API URL必填
    if (p.llmApiUrl !== undefined) {
      payload.llmApiUrl = trim(p.llmApiUrl)
      if (!isNonEmpty(payload.llmApiUrl)) {
        throw new Error('LLM API地址不能为空')
      }
    } else if (p.llmProvider !== undefined) {
      // 如果更新了提供商为openai-compatible，但没有提供URL，报错
      throw new Error('LLM API地址不能为空')
    }
  } else if (p.llmApiUrl !== undefined && isNonEmpty(trim(p.llmApiUrl))) {
    // 非OpenAi兼容时，如果提供了URL也发送（可选）
    payload.llmApiUrl = trim(p.llmApiUrl)
  }
  
  // 模型名称（可选）
  if (p.llmModelName !== undefined) {
    payload.llmModelName = trim(p.llmModelName)
  }
  
  // 模型类型（可选）
  if (p.llmModelType !== undefined && p.llmModelType !== '') {
    const type = Number(p.llmModelType)
    if ([1, 2, 3].includes(type)) {
      payload.llmModelType = type
    } else {
      throw new Error('模型类型参数无效，必须是 1-chat, 2-embedding, 3-rerank')
    }
  }
  
  // 温度（可选，仅CHAT模型）
  if (p.llmModelTemperature !== undefined && p.llmModelTemperature !== null && p.llmModelTemperature !== '') {
    const t = Number(p.llmModelTemperature)
    if (Number.isFinite(t) && t >= 0 && t <= 1) {
      payload.llmModelTemperature = t
    }
  }
  
  // 思考模式（可选，仅CHAT模型）
  if (p.llmModelThinking !== undefined && p.llmModelThinking !== '') {
    const thinking = Number(p.llmModelThinking)
    if ([0, 1].includes(thinking)) {
      payload.llmModelThinking = thinking
    } else {
      throw new Error('思考模式参数无效，必须是 0-否 或 1-是')
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
