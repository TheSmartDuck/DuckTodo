import http from '@/utils/http'
import { trim, isNonEmpty } from '@/utils/validation'

/**
 * AI Backend LLM 相关接口
 * 通过 SpringBoot Gateway 反向代理，使用 /api/ai 前缀访问 ai-backend 服务
 * 
 * 接口说明：
 * - 所有接口均需鉴权
 * - 返回格式统一为 R<T>，包含 success、code、message、data、timestamp
 * - 接口路径前缀：/api/ai/llm
 */

/**
 * 测试 LLM 配置连通性
 * 接口路径: POST /api/ai/llm/test-connectivity
 * @param {Object} config 配置对象 (必填)
 * @param {string} config.llmProvider 提供商 (必填, openai/bailian/siliconflow/openai-compatible)
 * @param {string} config.llmApiKey API Key (必填)
 * @param {string} [config.llmApiUrl] API URL (仅在openai-compatible时必填)
 * @param {string} config.llmModelName 模型名称 (必填)
 * @param {number} config.llmModelType 模型类型 (必填, 1-chat, 2-embedding, 3-rerank)
 * @param {number} [config.llmModelTemperature] 温度 (选填, 0.0-1.0)
 * @param {number} [config.llmModelThinking] 是否开启思考模式 (选填, 枚举: 0-否, 1-是, 仅Chat模型)
 * @returns {Promise<boolean>} 返回 R<Boolean>，成功返回 true，失败抛出错误
 */
export const testLlmConnectivity = (config = {}) => {
  const c = config || {}
  const payload = {}
  
  // 提供商（必填）
  if (c.llmProvider !== undefined) {
    payload.llm_provider = trim(c.llmProvider)
  }
  if (!isNonEmpty(payload.llm_provider)) {
    throw new Error('LLM提供商不能为空')
  }
  
  // API Key（必填）
  if (c.llmApiKey !== undefined) {
    payload.llm_api_key = trim(c.llmApiKey)
  }
  if (!isNonEmpty(payload.llm_api_key)) {
    throw new Error('LLM API Key不能为空')
  }
  
  // API URL（仅在OpenAi兼容时必填）
  const provider = payload.llm_provider
  if (provider === 'openai-compatible') {
    // OpenAi兼容时，API URL必填
    if (c.llmApiUrl !== undefined) {
      payload.llm_api_url = trim(c.llmApiUrl)
    }
    if (!isNonEmpty(payload.llm_api_url)) {
      throw new Error('LLM API地址不能为空')
    }
  } else if (c.llmApiUrl !== undefined && isNonEmpty(trim(c.llmApiUrl))) {
    // 非OpenAi兼容时，如果提供了URL也发送（可选）
    payload.llm_api_url = trim(c.llmApiUrl)
  }
  
  // 模型名称（必填）
  if (c.llmModelName !== undefined) {
    payload.llm_model_name = trim(c.llmModelName)
  }
  if (!isNonEmpty(payload.llm_model_name)) {
    throw new Error('模型名称不能为空')
  }
  
  // 模型类型（必填）
  if (c.llmModelType !== undefined && c.llmModelType !== '') {
    const type = Number(c.llmModelType)
    if ([1, 2, 3].includes(type)) {
      payload.llm_model_type = type
    } else {
      throw new Error('模型类型参数无效，必须是 1-chat, 2-embedding, 3-rerank')
    }
  } else {
    throw new Error('模型类型不能为空')
  }
  
  // 温度（可选）
  if (c.llmModelTemperature !== undefined && c.llmModelTemperature !== '') {
    const t = Number(c.llmModelTemperature)
    if (Number.isFinite(t) && t >= 0 && t <= 1) {
      payload.llm_model_temperature = t
    }
  }
  
  // 思考模式（可选，仅Chat模型）
  if (c.llmModelThinking !== undefined && c.llmModelThinking !== '') {
    const thinking = Number(c.llmModelThinking)
    if ([0, 1].includes(thinking)) {
      payload.llm_model_thinking = thinking
    }
  }

  // 使用 /api/ai 前缀调用 ai-backend 接口
  return http.post('/ai/llm/test-connectivity', payload)
}

export default {
  testLlmConnectivity
}
