import http from '@/utils/http'
import { normalizeLocalDate } from '@/utils/validation'

/**
 * 每日日报接口封装（与后端 daily_report_router 对齐）
 * - 统一路径前缀：`/api/ai/daily-report`（AI backend 接口）
 * - 所有接口均需鉴权（通过 http 拦截器自动注入 token）
 * - 返回格式统一为 R<T>，包含 success、code、message、data、timestamp
 */

/**
 * 获取今日已完成的任务与子任务
 * 接口路径: GET /api/ai/daily-report/today-completed-tasks
 * @param {Object} [params] 参数对象 (选填)
 * @param {string} [params.targetDate] 目标日期 (选填, YYYY-MM-DD格式, 默认为今天)
 * @returns {Promise<Array>} 返回任务和子任务列表，每个元素包含：
 *   - task: Task 对象
 *   - child_tasks: ChildTask 对象列表
 */
export const getTodayCompletedTasksWithChildren = async (params = {}) => {
  const queryParams = {}
  
  // 处理日期参数
  if (params.targetDate) {
    const normalizedDate = normalizeLocalDate(params.targetDate)
    if (normalizedDate) {
      queryParams.target_date = normalizedDate
    }
  }
  
  // 构建查询字符串
  const queryString = Object.keys(queryParams)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(queryParams[key])}`)
    .join('&')
  
  const url = `/ai/daily-report/today-completed-tasks${queryString ? `?${queryString}` : ''}`
  
  // 调用接口，http.get 会自动处理 R<T> 格式的响应，提取 data 字段
  const data = await http.get(url)
  
  // 返回数据列表
  // 如果返回的是数组，直接返回；如果是对象且包含 data 字段，返回 data
  if (Array.isArray(data)) {
    return data
  } else if (data && Array.isArray(data.data)) {
    return data.data
  } else {
    return []
  }
}

/**
 * 生成每日日报
 * 接口路径: POST /api/ai/daily-report/generate
 * @param {Object} params 参数对象
 * @param {string} params.llmConfigId LLM配置ID
 * @param {Array<Object>} [params.todayFinishTaskList] 今日完成任务列表 (选填)
 * @returns {Promise<Object>} 返回生成的日报内容，包含：
 *   - today_finish_tasks_report: 今日完成报告
 *   - tomorrow_todo_tasks_report: 明日待办报告
 *   - think_report: 思考报告
 */
export const generateDailyReport = async (params) => {
  if (!params || !params.llmConfigId) {
    throw new Error('llmConfigId 参数必填')
  }
  
  const requestBody = {
    llm_config_id: params.llmConfigId,
    today_finish_task_list: params.todayFinishTaskList || null
  }
  
  const url = '/ai/daily-report/generate'
  
  // 调用接口，http.post 会自动处理 R<T> 格式的响应，提取 data 字段
  // 设置超时时间为 60 秒（60000 毫秒）
  const data = await http.post(url, requestBody, {
    timeout: 60000
  })
  
  // 返回数据对象
  return data || {}
}

/**
 * 创建每日日报工具配置
 * 接口路径: POST /api/ai/daily-report/tool-config
 * @param {Object} params 参数对象
 * @param {string} params.llmConfigId LLM配置ID
 * @returns {Promise<string>} 返回配置ID
 */
export const createDailyReportToolConfig = async (params) => {
  if (!params || !params.llmConfigId) {
    throw new Error('llmConfigId 参数必填')
  }
  
  const requestBody = {
    llm_config_id: params.llmConfigId
  }
  
  const url = '/ai/daily-report/tool-config'
  
  // 调用接口，http.post 会自动处理 R<T> 格式的响应，提取 data 字段
  const data = await http.post(url, requestBody)
  
  // 返回配置ID
  return data || null
}

/**
 * 更新每日日报工具配置
 * 接口路径: PUT /api/ai/daily-report/tool-config
 * @param {Object} params 参数对象
 * @param {string} params.llmConfigId LLM配置ID
 * @returns {Promise<string>} 返回配置ID
 */
export const updateDailyReportToolConfig = async (params) => {
  if (!params || !params.llmConfigId) {
    throw new Error('llmConfigId 参数必填')
  }
  
  const requestBody = {
    llm_config_id: params.llmConfigId
  }
  
  const url = '/ai/daily-report/tool-config'
  
  // 调用接口，http.put 会自动处理 R<T> 格式的响应，提取 data 字段
  const data = await http.put(url, requestBody)
  
  // 返回配置ID
  return data || null
}

/**
 * 根据用户ID获取每日日报工具配置
 * 接口路径: GET /api/ai/daily-report/tool-config
 * @returns {Promise<Object|null>} 返回配置对象，包含 id, user_id, tool_name, config_json 等字段，如果不存在则返回 null
 */
export const getDailyReportToolConfigByUserId = async () => {
  const url = '/ai/daily-report/tool-config'
  
  // 调用接口，http.get 会自动处理 R<T> 格式的响应，提取 data 字段
  const data = await http.get(url)
  
  // 返回配置对象或 null
  return data || null
}

export default {
  getTodayCompletedTasksWithChildren,
  generateDailyReport,
  createDailyReportToolConfig,
  updateDailyReportToolConfig,
  getDailyReportToolConfigByUserId
}
