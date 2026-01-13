import http from '@/utils/http'
import { trim, isNonEmpty, isMinLength, isHexColor, isMaxLength, inMapKeys } from '@/utils/validation'
import { taskGroupStatusByCode } from '@/utils/enums'

/**
 * 创建私有任务族
 * 接口路径: POST /api/taskgroups
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.groupName 任务族名称 (必填, ≥2位)
 * @param {string} [params.groupDescription] 任务族描述 (选填)
 * @param {number} [params.groupStatus] 任务族状态 (选填, 枚举: 0-禁用, 1-正常, 默认1)
 * @param {string} [params.groupColor] 任务族颜色 (选填, 6位Hex)
 * @returns {Promise} 返回 R<TaskGroup>
 */
export const createPrivateTaskGroup = (params = {}) => {
  const payload = {
    groupName: trim(params.groupName || ''),
    groupDescription: trim(params.groupDescription || ''),
    groupStatus: params.groupStatus,
    groupColor: params.groupColor
  }
  
  if (params.groupColor) {
    const color = String(params.groupColor).trim()
    if (!isHexColor(color)) throw new Error('颜色格式无效')
    payload.groupColor = color
  }
  
  if (!isNonEmpty(payload.groupName)) throw new Error('任务族名称不能为空')
  if (!isMinLength(payload.groupName, 2)) throw new Error('任务族名称至少 2 位')
  
  if (payload.groupStatus !== undefined && payload.groupStatus !== null) {
    const code = Number(payload.groupStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskGroupStatusByCode, code)) throw new Error('非法任务族状态')
    payload.groupStatus = code
  } else {
    delete payload.groupStatus
  }
  
  return http.post('/taskgroups', payload)
}

/**
 * 更新私有任务族（仅创建者）
 * 接口路径: PUT /api/taskgroups
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskGroupId 任务族ID (必填)
 * @param {string} params.groupName 任务族名称 (必填, ≥2位)
 * @param {string} [params.groupDescription] 任务族描述 (选填)
 * @param {number} [params.groupStatus] 任务族状态 (选填, 枚举: 0-禁用, 1-正常)
 * @returns {Promise} 返回 R<TaskGroup>
 */
export const updatePrivateTaskGroup = ({ taskGroupId, groupName, groupDescription, groupStatus } = {}) => {
  const gid = String(taskGroupId || '').trim()
  if (!isNonEmpty(gid)) throw new Error('缺少任务族ID')
  
  const name = String(groupName || '').trim()
  if (!isNonEmpty(name)) throw new Error('任务族名称不能为空')
  if (!isMinLength(name, 2)) throw new Error('任务族名称至少 2 位')

  const payload = { 
    taskGroupId: gid,
    groupName: name
  }
  
  if (groupDescription !== undefined) payload.groupDescription = trim(groupDescription || '')
  
  if (groupStatus !== undefined && groupStatus !== null) {
    const code = Number(groupStatus)
    if (!Number.isFinite(code) || !inMapKeys(taskGroupStatusByCode, code)) throw new Error('非法任务族状态')
    payload.groupStatus = code
  }
  
  return http.put('/taskgroups', payload)
}

/**
 * 删除私有任务族（仅创建者）
 * 接口路径: DELETE /api/taskgroups/{taskGroupId}
 * @param {string} taskGroupId 任务族ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const deletePrivateTaskGroup = (taskGroupId) => {
  const gid = String(taskGroupId || '').trim()
  if (!isNonEmpty(gid)) throw new Error('缺少任务族ID')
  return http.del(`/taskgroups/${encodeURIComponent(gid)}`)
}

/**
 * 交换个人两个任务族的排序（当前用户维度）
 * 接口路径: PUT /api/taskgroups/order
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskGroupId1 任务族1 ID (必填)
 * @param {string} params.taskGroupId2 任务族2 ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const swapMyTaskGroupOrder = ({ taskGroupId1, taskGroupId2 } = {}) => {
  const a = String(taskGroupId1 || '').trim()
  const b = String(taskGroupId2 || '').trim()
  if (!isNonEmpty(a) || !isNonEmpty(b)) throw new Error('需提供两个任务族ID')
  if (a === b) throw new Error('排序值禁止一致')
  return http.put('/taskgroups/order', { taskGroupIdA: a, taskGroupIdB: b })
}

/**
 * 修改我的任务族颜色（关系维度，仅当前用户）
 * 接口路径: PUT /api/taskgroups/{taskGroupId}/members/me/color
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskGroupId 任务族ID (必填)
 * @param {string} params.groupColor 任务族颜色 (必填, 6位Hex #xxxxxx)
 * @returns {Promise} 返回 R<Boolean>
 */
export const updateMyTaskGroupColor = ({ taskGroupId, groupColor } = {}) => {
  const gid = String(taskGroupId || '').trim()
  if (!isNonEmpty(gid)) throw new Error('缺少任务族ID')
  const color = String(groupColor || '').trim()
  if (!isHexColor(color)) throw new Error('颜色格式无效，需为 #xxxxxx（6位十六进制）')
  return http.put(`/taskgroups/${encodeURIComponent(gid)}/members/me/color`, { groupColor: color })
}

/**
 * 修改我的任务族别名（关系维度，仅当前用户）
 * 接口路径: PUT /api/taskgroups/{taskGroupId}/members/me/alias
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.taskGroupId 任务族ID (必填)
 * @param {string} params.groupAlias 任务族别名 (必填, ≤64字符)
 * @returns {Promise} 返回 R<Boolean>
 */
export const updateMyGroupAlias = ({ taskGroupId, groupAlias } = {}) => {
  const gid = String(taskGroupId || '').trim()
  if (!isNonEmpty(gid)) throw new Error('缺少任务族ID')
  const alias = String(groupAlias || '').trim()
  if (!isNonEmpty(alias)) throw new Error('别名不能为空')
  if (!isMaxLength(alias, 64)) throw new Error('别名长度不能超过64字符')
  return http.put(`/taskgroups/${encodeURIComponent(gid)}/members/me/alias`, { groupAlias: alias })
}

/**
 * 列出当前用户相关的任务族（按关系排序）
 * 接口路径: GET /api/taskgroups/me
 * @returns {Promise} 返回 R<List<TaskGroup>>
 */
export const getMyTaskGroupList = () => {
  return http.get('/taskgroups/me')
}

/**
 * 查询单个任务族下的所有成员（仅正常成员）
 * 接口路径: GET /api/taskgroups/{taskGroupId}/members
 * @param {string} taskGroupId 任务族ID (必填)
 * @returns {Promise} 返回 R<List<TaskGroupMember>>
 */
export const getTaskGroupMembers = (taskGroupId) => {
  const gid = String(taskGroupId || '').trim()
  if (!isNonEmpty(gid)) throw new Error('缺少任务族ID')
  return http.get(`/taskgroups/${encodeURIComponent(gid)}/members`)
}

export default {
  createPrivateTaskGroup,
  updatePrivateTaskGroup,
  updateMyTaskGroupColor,
  updateMyGroupAlias,
  swapMyTaskGroupOrder,
  getTaskGroupMembers,
  getMyTaskGroupList,
  deletePrivateTaskGroup
}
