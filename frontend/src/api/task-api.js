import http from '@/utils/http'
import { trim, isNonEmpty, isMinLength, inMapKeys, normalizeLocalDate, compareDateStrings } from '@/utils/validation'
import { taskStatusByCode, taskPriorityByCode } from '@/utils/enums'

/**
 * 任务接口封装（与后端 TaskController 对齐）
 * - 统一参数校验与清洗：名称、枚举值、日期
 * - 统一路径前缀：`/api/tasks`（由 http 基类代理）
 */

/**
 * 创建任务
 * 接口路径: POST /api/tasks
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskGroupId 任务族ID (必填)
 * @param {string} params.taskName 任务名称 (必填, ≥2位)
 * @param {string} [params.taskDescription] 任务描述 (选填)
 * @param {number} [params.taskStatus] 任务状态 (选填, 枚举: 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消, 默认1)
 * @param {number} [params.taskPriority] 任务优先级 (选填, 枚举: 0-P0紧急, 1-P1高, 2-P2中, 3-P3低, 4-P4最低, 默认3)
 * @param {string} [params.startTime] 开始日期 (选填, YYYY-MM-DD)
 * @param {string} params.dueTime 截止日期 (必填, YYYY-MM-DD, 需 >= 当天 且 > 开始日期)
 * @param {Array<string>} [params.helperUserIdList] 协助者ID列表 (选填)
 * @param {Array<Object>} [params.childTaskList] 子任务列表 (选填)
 * @returns {Promise} 返回 R<TaskDetailResponse>
 */
export const createTask = (params = {}) => {
  const payload = {
    taskGroupId: String(params.taskGroupId || '').trim(),
    taskName: trim(params.taskName || ''),
    taskDescription: trim(params.taskDescription || ''),
    taskStatus: params.taskStatus,
    taskPriority: params.taskPriority,
    startTime: params.startTime,
    dueTime: params.dueTime,
    helperUserIdList: Array.isArray(params.helperUserIdList) ? params.helperUserIdList.map(x => String(x)).filter(Boolean) : [],
    childTaskList: Array.isArray(params.childTaskList) ? params.childTaskList.map(ct => {
      const name = trim(ct && ct.childTaskName || '')
      const status = ct && ct.childTaskStatus
      const due = ct && ct.dueTime
      const assignee = String(ct && ct.assigneeUserId || '').trim()
      
      // 子任务校验
      if (!isNonEmpty(name)) throw new Error('子任务名称不能为空')
      if (!isMinLength(name, 2)) throw new Error('子任务名称至少 2 位')
      if (!normalizeLocalDate(due)) throw new Error('子任务截止日期不能为空')
      if (!isNonEmpty(assignee)) throw new Error('子任务执行者不能为空')

      return {
        childTaskName: name,
        childTaskStatus: status,
        dueTime: normalizeLocalDate(due),
        assigneeUserId: assignee
      }
    }) : []
  }

  if (!isNonEmpty(payload.taskGroupId)) throw new Error('任务族ID不能为空')
  if (!isNonEmpty(payload.taskName)) throw new Error('任务名称不能为空')
  if (!isMinLength(payload.taskName, 2)) throw new Error('任务名称至少 2 位')
  
  if (payload.taskStatus !== undefined && payload.taskStatus !== null) {
    const code = Number(payload.taskStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskStatusByCode, code)) throw new Error('非法任务状态')
    payload.taskStatus = code
  } else {
    delete payload.taskStatus
  }
  
  if (payload.taskPriority !== undefined && payload.taskPriority !== null) {
    const code = Number(payload.taskPriority)
    if (!Number.isFinite(code) || !inMapKeys(taskPriorityByCode, code)) throw new Error('非法任务优先级')
    payload.taskPriority = code
  }
  
  payload.startTime = normalizeLocalDate(payload.startTime) || undefined
  payload.dueTime = normalizeLocalDate(payload.dueTime)
  
  if (!payload.dueTime) throw new Error('截止日期不能为空')
  if (payload.startTime && compareDateStrings(payload.startTime, payload.dueTime) > 0) {
    throw new Error('开始日期必须不晚于截止日期')
  }
  
  return http.post('/tasks', payload)
}

/**
 * 更新任务
 * 接口路径: PUT /api/tasks
 * @param {string} taskId 任务ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.taskName] 任务名称 (选填, ≥2位)
 * @param {string} [params.taskDescription] 任务描述 (选填)
 * @param {number} [params.taskStatus] 任务状态 (选填, 枚举: 0-4)
 * @param {number} [params.taskPriority] 任务优先级 (选填, 枚举: 0-4)
 * @param {string} [params.startTime] 开始日期 (选填, YYYY-MM-DD)
 * @param {string} [params.dueTime] 截止日期 (选填, YYYY-MM-DD)
 * @param {string} [params.finishTime] 完成日期 (选填, YYYY-MM-DD)
 * @returns {Promise} 返回 R<TaskDetailResponse>
 */
export const updateTask = (taskId, params = {}) => {
  const id = String(taskId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务ID')

  const payload = { taskId: id }
  
  if (params.taskName !== undefined) {
    const name = trim(params.taskName || '')
    if (!isNonEmpty(name)) throw new Error('任务名称不能为空')
    if (!isMinLength(name, 2)) throw new Error('任务名称至少 2 位')
    payload.taskName = name
  }
  
  if (params.taskDescription !== undefined) {
    payload.taskDescription = trim(params.taskDescription || '')
  }
  
  if (params.taskStatus !== undefined && params.taskStatus !== null) {
    const code = Number(params.taskStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskStatusByCode, code)) throw new Error('非法任务状态')
    payload.taskStatus = code
  }
  
  if (params.taskPriority !== undefined && params.taskPriority !== null) {
    const code = Number(params.taskPriority)
    if (!Number.isFinite(code) || !inMapKeys(taskPriorityByCode, code)) throw new Error('非法任务优先级')
    payload.taskPriority = code
  }
  
  if (params.startTime !== undefined) payload.startTime = normalizeLocalDate(params.startTime) || undefined
  
  if (params.dueTime !== undefined) {
    const due = normalizeLocalDate(params.dueTime)
    // 允许置空由于API未明确说明，但逻辑上截止时间通常必须存在，此处遵循原逻辑若传则校验
    if (params.dueTime && !due) throw new Error('截止日期格式无效')
    
    // 如果有startTime（本次更新或需结合现有数据，此处仅校验本次更新均存在的情况）
    if (payload.startTime && due && compareDateStrings(payload.startTime, due) > 0) {
      throw new Error('截止日期不能早于开始日期')
    }
    payload.dueTime = due
  }
  
  if (params.finishTime !== undefined) payload.finishTime = normalizeLocalDate(params.finishTime) || undefined
  
  return http.put('/tasks', payload)
}

/**
 * 删除任务
 * 接口路径: DELETE /api/tasks/{taskId}
 * @param {string} taskId 任务ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const deleteTask = (taskId) => {
  const id = String(taskId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务ID')
  return http.del(`/tasks/${encodeURIComponent(id)}`)
}

/**
 * 获取任务详情
 * 接口路径: GET /api/tasks/{taskId}
 * @param {string} taskId 任务ID (必填)
 * @returns {Promise} 返回 R<TaskDetailResponse>
 */
export const getTaskDetail = (taskId) => {
  const id = String(taskId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务ID')
  return http.get(`/tasks/${encodeURIComponent(id)}`)
}

/**
 * 列出任务族的任务（非分页）
 * 接口路径: GET /api/tasks/taskgroups/{taskGroupId}/tasks
 * @param {string} taskGroupId 任务族ID (必填)
 * @param {Object} [params] 筛选参数 (选填)
 * @param {string} [params.taskName] 模糊搜索 (选填)
 * @param {string} [params.taskStatus] 状态列表 (选填)
 * @param {string} [params.taskPriority] 优先级列表 (选填)
 * @param {string} [params.startDueTime] 截止日期起 (选填)
 * @param {string} [params.EndDueTime] 截止日期止 (选填)
 * @param {string|number} [params.sortByMode] 排序 (选填, 0=按截止时间, 1=按优先级)
 * @param {boolean} [params.relatedToMe] 仅与我相关 (选填)
 * @returns {Promise} 返回 R<List<TaskSummaryResponse>>
 */
export const getTaskListByTaskGroup = (taskGroupId, params = {}) => {
  const id = String(taskGroupId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务族ID')
  
  const q = { ...(params || {}) }
  if (q.sortByMode === 'byPriority') {
    q.sortByMode = 1
  } else if (q.sortByMode !== undefined && q.sortByMode !== null) {
    q.sortByMode = Number(q.sortByMode)
  }
  return http.get(`/tasks/taskgroups/${encodeURIComponent(id)}/tasks`, q)
}

/**
 * 列出任务族的任务（分页）
 * 接口路径: GET /api/tasks/taskgroups/{taskGroupId}/tasks/page
 * @param {string} taskGroupId 任务族ID (必填)
 * @param {Object} [params] 筛选参数 (选填)
 * @param {number} [params.page] 页码 (选填, 默认1)
 * @param {number} [params.size] 每页条数 (选填, 默认10)
 * @param {string} [params.taskName] 模糊搜索 (选填)
 * @param {string} [params.taskStatus] 状态列表 (选填)
 * @param {string} [params.taskPriority] 优先级列表 (选填)
 * @param {string} [params.startDueTime] 截止日期起 (选填)
 * @param {string} [params.EndDueTime] 截止日期止 (选填)
 * @param {string|number} [params.sortByMode] 排序 (选填, 0=按截止时间, 1=按优先级)
 * @param {boolean} [params.relatedToMe] 仅与我相关 (选填)
 * @returns {Promise} 返回 R<Page<TaskSummaryResponse>>
 */
export const getTaskPageByTaskGroup = (taskGroupId, params = {}) => {
  const id = String(taskGroupId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务族ID')
  
  const { page = 1, size = 10, ...rest } = params || {}
  const q = { page, size, ...rest }
  if (q.sortByMode === 'byPriority') {
    q.sortByMode = 1
  } else if (q.sortByMode !== undefined && q.sortByMode !== null) {
    q.sortByMode = Number(q.sortByMode)
  }
  return http.get(`/tasks/taskgroups/${encodeURIComponent(id)}/tasks/page`, q)
}

/**
 * 列出与我相关的任务（非分页）
 * 接口路径: GET /api/tasks/me
 * @param {Object} [params] 筛选参数 (选填)
 * @param {Array<string>|string} [params.taskGroupId] 任务族ID列表 (选填)
 * @param {string} [params.taskName] 模糊搜索 (选填)
 * @param {string} [params.taskStatus] 状态列表 (选填)
 * @param {string} [params.taskPriority] 优先级列表 (选填)
 * @param {string} [params.startDueTime] 截止日期起 (选填)
 * @param {string} [params.EndDueTime] 截止日期止 (选填)
 * @param {string|number} [params.sortByMode] 排序 (选填, 0=按截止时间, 1=按优先级)
 * @returns {Promise} 返回 R<List<TaskSummaryResponse>>
 */
export const getTaskListByUserId = (params = {}) => {
  const q = { ...(params || {}) }
  if (q.sortByMode === 'byPriority') {
    q.sortByMode = 1
  } else if (q.sortByMode !== undefined && q.sortByMode !== null) {
    q.sortByMode = Number(q.sortByMode)
  }
  if (Array.isArray(q.taskGroupId)) {
    q.taskGroupId = q.taskGroupId.filter(Boolean).join(',')
  }
  return http.get('/tasks/me', q)
}

/**
 * 列出与我相关的任务（分页）
 * 接口路径: GET /api/tasks/me/page
 * @param {Object} [params] 筛选参数 (选填)
 * @param {number} [params.page] 页码 (选填, 默认1)
 * @param {number} [params.size] 每页条数 (选填, 默认10)
 * @param {Array<string>|string} [params.taskGroupId] 任务族ID列表 (选填)
 * @param {string} [params.taskName] 模糊搜索 (选填)
 * @param {string} [params.taskStatus] 状态列表 (选填)
 * @param {string} [params.taskPriority] 优先级列表 (选填)
 * @param {string} [params.startDueTime] 截止日期起 (选填)
 * @param {string} [params.EndDueTime] 截止日期止 (选填)
 * @param {string|number} [params.sortByMode] 排序 (选填, 0=按截止时间, 1=按优先级)
 * @returns {Promise} 返回 R<Page<TaskSummaryResponse>>
 */
export const getTaskPageByUserId = (params = {}) => {
  const { page = 1, size = 10, ...rest } = params || {}
  const q = { page, size, ...rest }
  if (q.sortByMode === 'byPriority') {
    q.sortByMode = 1
  } else if (q.sortByMode !== undefined && q.sortByMode !== null) {
    q.sortByMode = Number(q.sortByMode)
  }
  if (Array.isArray(q.taskGroupId)) {
    q.taskGroupId = q.taskGroupId.filter(Boolean).join(',')
  }
  return http.get('/tasks/me/page', q)
}

/**
 * 获取我的日程视图
 * 接口路径: GET /api/tasks/me/schedule
 * @param {Object} params 参数对象 (必填)
 * @param {number|string} params.year 年份 (必填, 如 2024)
 * @param {number|string} params.month 月份 (必填, 1-12)
 * @param {Array<string>|string} [params.taskGroupId] 任务族ID列表 (选填)
 * @param {string|number} [params.sortByMode] 排序 (选填)
 * @returns {Promise} 返回 R<List<TaskSummaryResponse>>
 */
export const getScheduleTaskListByUserId = (params = {}) => {
  const q = { ...(params || {}) }
  
  if (!q.year || !q.month) throw new Error('年份和月份为必填项')
  
  if (q.sortByMode === 'byPriority') {
    q.sortByMode = 1
  } else if (q.sortByMode !== undefined && q.sortByMode !== null) {
    q.sortByMode = Number(q.sortByMode)
  }
  if (Array.isArray(q.taskGroupId)) {
    q.taskGroupId = q.taskGroupId.filter(Boolean).join(',')
  }
  return http.get('/tasks/me/schedule', q)
}

/**
 * 查询子任务列表
 * 接口路径: GET /api/tasks/{taskId}/children
 * @param {string} taskId 父任务ID (必填)
 * @returns {Promise} 返回 R<List<ChildTask>>
 */
export const getChildTaskList = (taskId) => {
  const id = String(taskId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务ID')
  return http.get(`/tasks/${encodeURIComponent(id)}/children`)
}

/**
 * 添加子任务
 * 接口路径: POST /api/tasks/children
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskId 父任务ID (必填)
 * @param {string} params.childTaskName 子任务名称 (必填, ≥2位)
 * @param {number} [params.childTaskStatus] 子任务状态 (选填, 枚举: 0-4, 默认1)
 * @param {string} params.dueTime 截止日期 (必填, YYYY-MM-DD)
 * @param {string} params.childTaskAssigneeId 执行者用户ID (必填)
 * @returns {Promise} 返回 R<ChildTask>
 */
export const addChildTask = (params = {}) => {
  const payload = {
    taskId: String(params.taskId || '').trim(),
    childTaskName: trim(params.childTaskName || ''),
    childTaskStatus: params.childTaskStatus,
    dueTime: normalizeLocalDate(params.dueTime),
    childTaskAssigneeId: String(params.childTaskAssigneeId || '').trim()
  }
  
  if (!isNonEmpty(payload.taskId)) throw new Error('缺少父任务ID')
  if (!isNonEmpty(payload.childTaskName)) throw new Error('子任务名称不能为空')
  if (!isMinLength(payload.childTaskName, 2)) throw new Error('子任务名称至少 2 位')
  
  if (payload.childTaskStatus !== undefined && payload.childTaskStatus !== null) {
    const code = Number(payload.childTaskStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskStatusByCode, code)) throw new Error('非法子任务状态')
    payload.childTaskStatus = code
  } else {
    delete payload.childTaskStatus
  }
  
  if (!payload.dueTime) throw new Error('子任务截止日期不能为空')
  if (!isNonEmpty(payload.childTaskAssigneeId)) throw new Error('子任务执行者不能为空')
  
  return http.post('/tasks/children', payload)
}

/**
 * 更新子任务
 * 接口路径: PUT /api/tasks/{taskId}/children
 * @param {string} taskId 父任务ID (必填)
 * @param {string} childTaskId 子任务ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.childTaskName] 子任务名称 (选填, ≥2位)
 * @param {number} [params.childTaskStatus] 子任务状态 (选填, 枚举: 0-4)
 * @param {string} [params.dueTime] 截止日期 (选填, YYYY-MM-DD)
 * @param {string} [params.childTaskAssigneeId] 执行者用户ID (选填)
 * @returns {Promise} 返回 R<ChildTask>
 */
export const updateChildTask = (taskId, childTaskId, params = {}) => {
  const tid = String(taskId || '').trim()
  const cid = String(childTaskId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少父任务ID')
  if (!isNonEmpty(cid)) throw new Error('缺少子任务ID')
  
  const payload = { childTaskId: cid }
  
  if (params.childTaskName !== undefined) {
    const name = trim(params.childTaskName || '')
    if (isNonEmpty(name) && !isMinLength(name, 2)) throw new Error('子任务名称至少 2 位')
    payload.childTaskName = name
  }
  
  if (params.childTaskStatus !== undefined && params.childTaskStatus !== null) {
    const code = Number(params.childTaskStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskStatusByCode, code)) throw new Error('非法子任务状态')
    payload.childTaskStatus = code
  }
  
  if (params.dueTime !== undefined) {
    payload.dueTime = normalizeLocalDate(params.dueTime) || undefined
  }
  
  if (params.childTaskAssigneeId !== undefined) {
    payload.childTaskAssigneeId = String(params.childTaskAssigneeId || '').trim() || null
  }
  
  return http.put(`/tasks/${encodeURIComponent(tid)}/children`, payload)
}

/**
 * 删除子任务
 * 接口路径: DELETE /api/tasks/{taskId}/children/{childTaskId}
 * @param {string} taskId 父任务ID (必填)
 * @param {string} childTaskId 子任务ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const deleteChildTask = (taskId, childTaskId) => {
  const tid = String(taskId || '').trim()
  const cid = String(childTaskId || '').trim()
  if (!isNonEmpty(tid) || !isNonEmpty(cid)) throw new Error('缺少任务ID')
  return http.del(`/tasks/${encodeURIComponent(tid)}/children/${encodeURIComponent(cid)}`)
}

/**
 * 调整子任务排序
 * 接口路径: PUT /api/tasks/{taskId}/children/order
 * @param {string} taskId 父任务ID (必填)
 * @param {Array<string>} orderedChildIds 有序的子任务ID列表 (必填)
 * @returns {Promise} 返回 R<List<ChildTask>>
 */
export const reorderChildTasks = (taskId, orderedChildIds = []) => {
  const tid = String(taskId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少任务ID')
  const arr = Array.isArray(orderedChildIds) ? orderedChildIds.map(id => String(id || '').trim()).filter(Boolean) : []
  if (!arr.length) throw new Error('需提供有序的子任务ID列表')
  return http.put(`/tasks/${encodeURIComponent(tid)}/children/order`, arr)
}

/**
 * 查询任务附件列表
 * 接口路径: GET /api/tasks/{taskId}/files
 * @param {string} taskId 任务ID (必填)
 * @returns {Promise} 返回 R<List<TaskFile>>
 */
export const getFileTaskList = (taskId) => {
  const id = String(taskId || '').trim()
  if (!isNonEmpty(id)) throw new Error('缺少任务ID')
  return http.get(`/tasks/${encodeURIComponent(id)}/files`)
}

/**
 * 上传任务附件
 * 接口路径: POST /api/tasks/{taskId}/files
 * @param {string} taskId 任务ID (必填)
 * @param {File|Blob} file 文件对象 (必填)
 * @param {string} [remark] 附件备注 (选填)
 * @param {Function} [onProgress] 上传进度回调 (选填)
 * @returns {Promise} 返回 R<TaskFile>
 */
export const uploadTaskFile = (taskId, file, remark = '', onProgress) => {
  const tid = String(taskId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少任务ID')
  if (!file) throw new Error('文件不能为空')
  
  const config = {}
  if (onProgress) {
    config.onUploadProgress = onProgress
  }
  return http.upload(`/tasks/${encodeURIComponent(tid)}/files`, file, 'file', { remark }, config)
}

/**
 * 删除任务附件
 * 接口路径: DELETE /api/tasks/{taskId}/files/{taskFileId}
 * @param {string} taskId 任务ID (必填)
 * @param {string} taskFileId 附件ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const deleteTaskFile = (taskId, taskFileId) => {
  const tid = String(taskId || '').trim()
  const fid = String(taskFileId || '').trim()
  if (!isNonEmpty(tid) || !isNonEmpty(fid)) throw new Error('缺少参数')
  return http.del(`/tasks/${encodeURIComponent(tid)}/files/${encodeURIComponent(fid)}`)
}

/**
 * 添加协助者
 * 接口路径: POST /api/tasks/{taskId}/followers
 * @param {string} taskId 任务ID (必填)
 * @param {string} userId 协助者用户ID (必填)
 * @returns {Promise} 返回 R<TaskUserRelation>
 */
export const addTaskHelper = (taskId, userId) => {
  const tid = String(taskId || '').trim()
  const uid = String(userId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少任务ID')
  if (!isNonEmpty(uid)) throw new Error('缺少协助者用户ID')
  return http.post(`/tasks/${encodeURIComponent(tid)}/followers`, { taskId: tid, userId: uid })
}

/**
 * 删除协助者关系
 * 接口路径: DELETE /api/tasks/{taskId}/followers/{taskHelperId}
 * @param {string} taskId 任务ID (必填)
 * @param {string} taskHelperId 协助者用户ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const deleteTaskHelper = (taskId, taskHelperId) => {
  const tid = String(taskId || '').trim()
  const hid = String(taskHelperId || '').trim()
  if (!isNonEmpty(tid) || !isNonEmpty(hid)) throw new Error('缺少参数')
  return http.del(`/tasks/${encodeURIComponent(tid)}/followers/${encodeURIComponent(hid)}`)
}

/**
 * 获取任务审计日志列表
 * 接口路径: GET /api/tasks/{taskId}/audits
 * @param {string} taskId 任务ID (必填)
 * @returns {Promise} 返回 R<List<TaskAudit>>
 */
export const getTaskAuditList = (taskId) => {
  const tid = String(taskId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少任务ID')
  return http.get(`/tasks/${encodeURIComponent(tid)}/audits`)
}

export default {
  updateTask,
  getChildTaskList,
  updateChildTask,
  reorderChildTasks,
  createTask,
  addTaskHelper,
  getFileTaskList,
  uploadTaskFile,
  addChildTask,
  getTaskDetail,
  deleteTask,
  getTaskAuditList,
  getTaskListByUserId,
  getScheduleTaskListByUserId,
  getTaskPageByUserId,
  getTaskListByTaskGroup,
  getTaskPageByTaskGroup,
  deleteTaskHelper,
  deleteTaskFile,
  deleteChildTask
}
