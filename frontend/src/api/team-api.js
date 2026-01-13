import http from '@/utils/http'
import { trim, isNonEmpty, inMapKeys, isHexColor, isMinLength } from '@/utils/validation'
import { teamStatusByCode } from '@/utils/enums'

/**
 * Team（团队）API 封装
 * 路径前缀：/api/teams
 */

/**
 * 创建团队
 * 接口路径: POST /api/teams
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamName 团队名称 (必填, ≥2位)
 * @param {string} [params.teamDescription] 团队描述 (选填)
 * @param {string} [params.teamAvatar] 团队头像 (选填)
 * @param {number} [params.teamStatus] 团队状态 (选填, 枚举: 0-禁用, 1-进行中, 2-已结束, 默认1)
 * @param {Array} [params.invitedMemberList] 邀请成员列表 (选填)
 * @param {string} params.invitedMemberList[].userId 被邀请用户ID (必填)
 * @param {number} [params.invitedMemberList[].memberRole] 成员角色 (必填, 枚举: 1-管理者, 2-普通成员)
 * @param {number} [params.invitedMemberList[].userRole] 成员角色 (必填, 枚举: 1-管理者, 2-普通成员, 兼容字段名)
 * @param {number} [params.invitedMemberList[].role] 成员角色 (必填, 枚举: 1-管理者, 2-普通成员, 兼容字段名)
 * @returns {Promise} 返回 R<Team>
 */
export const createTeam = (params = {}) => {
  const payload = {
    teamName: trim(params.teamName || ''),
    teamDescription: trim(params.teamDescription || ''),
    teamAvatar: trim(params.teamAvatar || ''),
    teamStatus: params.teamStatus,
    invitedMemberList: Array.isArray(params.invitedMemberList) ? params.invitedMemberList : []
  }
  
  if (!isNonEmpty(payload.teamName)) throw new Error('团队名不能为空')
  if (!isMinLength(payload.teamName, 2)) throw new Error('团队名称至少 2 位')
  
  if (payload.teamStatus !== undefined && payload.teamStatus !== null) {
    const code = Number(payload.teamStatus)
    if (!Number.isFinite(code) || !inMapKeys(teamStatusByCode, code)) throw new Error('非法团队状态')
    payload.teamStatus = code
  }
  
  payload.invitedMemberList = payload.invitedMemberList
    .filter(x => isNonEmpty(x?.userId))
    .map(x => {
      // 支持多种字段名：memberRole（后端期望）、userRole（前端表单使用）、role（兼容）
      // 优先级：memberRole > userRole > role
      const roleValue = x.memberRole !== undefined ? x.memberRole : 
                       (x.userRole !== undefined ? x.userRole : x.role)
      
      // 如果角色值未定义，跳过该成员
      if (roleValue === undefined || roleValue === null) {
        throw new Error(`用户 ${x.userId} 的角色值未定义`)
      }
      
      const code = Number(roleValue)
      // 后端期望：1-管理者(MANAGER), 2-普通成员(MEMBER)
      // UserRoleEnum: OWNER(0), MANAGER(1), MEMBER(2)
      // 创建团队时只能邀请 MANAGER(1) 或 MEMBER(2)，不能邀请 OWNER(0)
      if (!Number.isFinite(code) || ![1, 2].includes(code)) {
        throw new Error(`用户 ${x.userId} 的角色值 ${code} 不合法，仅支持: 1(管理者) 或 2(普通成员)`)
      }
      return { userId: String(x.userId), memberRole: code }
    })
  console.log("params", params)
  return http.post('/teams', payload)
}

/**
 * 邀请成员加入团队
 * 接口路径: POST /api/teams/{teamId}/members
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamId 团队ID (必填)
 * @param {string} params.userId 被邀请用户ID (必填)
 * @param {number} params.userRole 成员角色 (必填, 枚举: 2-管理员, 3-成员)
 * @returns {Promise} 返回 R<TeamUserRelation>
 */
export const inviteTeamMember = ({ teamId, userId, userRole } = {}) => {
  const tid = String(teamId || '').trim()
  const uid = String(userId || '').trim()
  
  if (!isNonEmpty(tid)) throw new Error('缺少 teamId')
  if (!isNonEmpty(uid)) throw new Error('缺少 userId')
  
  const code = Number(userRole)
  if (!Number.isFinite(code) || ![2, 3].includes(code)) {
    throw new Error('角色仅支持数值枚举: 2(管理员) 或 3(成员)')
  }
  
  return http.post(`/teams/${encodeURIComponent(tid)}/members`, { userId: uid, userRole: code })
}

/**
 * 接受加入邀请（本人）
 * 接口路径: PUT /api/teams/{teamId}/invites/me/accept
 * @param {string|Object} payload 团队ID或包含teamId的对象 (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const acceptInviteTeam = (payload) => {
  const tid = typeof payload === 'string' ? payload : String(payload?.teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少 teamId')
  return http.put(`/teams/${encodeURIComponent(tid)}/invites/me/accept`)
}

/**
 * 拒绝加入邀请（本人）
 * 接口路径: PUT /api/teams/{teamId}/invites/me/reject
 * @param {string|Object} payload 团队ID或包含teamId的对象 (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const rejectInviteTeam = (payload) => {
  const tid = typeof payload === 'string' ? payload : String(payload?.teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少 teamId')
  return http.put(`/teams/${encodeURIComponent(tid)}/invites/me/reject`)
}

/**
 * 删除团队（级联逻辑删除）
 * 接口路径: DELETE /api/teams/{teamId}
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const deleteTeam = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少 teamId')
  return http.del(`/teams/${encodeURIComponent(tid)}`)
}

/**
 * 退出团队（本人）
 * 接口路径: DELETE /api/teams/{teamId}/members/me
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const leaveTeam = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少 teamId')
  return http.del(`/teams/${encodeURIComponent(tid)}/members/me`)
}

/**
 * 删除团队成员（管理员/所有者）
 * 接口路径: DELETE /api/teams/{teamId}/members/{userId}
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamId 团队ID (必填)
 * @param {string} params.userId 成员ID (必填)
 * @returns {Promise} 返回 R<Boolean>
 */
export const deleteTeamMember = ({ teamId, userId } = {}) => {
  const tid = String(teamId || '').trim()
  const uid = String(userId || '').trim()
  if (!isNonEmpty(tid) || !isNonEmpty(uid)) throw new Error('缺少必要参数: teamId 或 userId')
  return http.del(`/teams/${encodeURIComponent(tid)}/members/${encodeURIComponent(uid)}`)
}

/**
 * 修改团队信息（仅所有者）
 * 接口路径: PUT /api/teams
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamId 团队ID (必填)
 * @param {string} [params.teamName] 团队名称 (选填, ≥2位)
 * @param {string} [params.teamDescription] 团队描述 (选填)
 * @param {number} [params.teamStatus] 团队状态 (选填, 枚举: 0-禁用, 1-进行中, 2-已结束)
 * @returns {Promise} 返回 R<Team>
 */
export const updateTeam = ({ teamId, teamName, teamDescription, teamStatus } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  
  const payload = { teamId: tid }
  
  if (isNonEmpty(teamName)) {
    const name = String(teamName).trim()
    if (!isMinLength(name, 2)) throw new Error('团队名称至少 2 位')
    payload.teamName = name
  }
  
  if (teamDescription !== undefined) payload.teamDescription = trim(teamDescription || '')
  
  if (teamStatus !== undefined && teamStatus !== null) {
    const code = Number(teamStatus)
    if (!Number.isFinite(code) || !inMapKeys(teamStatusByCode, code)) throw new Error('非法团队状态')
    payload.teamStatus = code
  }
  
  return http.put('/teams', payload)
}

/**
 * 修改成员角色（仅所有者或管理员）
 * 接口路径: PUT /api/teams/{teamId}/members/{userId}/role
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamId 团队ID (必填)
 * @param {string} params.userId 成员ID (必填)
 * @param {number} params.userRole 成员角色 (必填, 枚举: 1-管理者, 2-普通成员)
 * @returns {Promise} 返回 R<Boolean>
 */
export const updateMemberRole = ({ teamId, userId, userRole } = {}) => {
  const tid = String(teamId || '').trim()
  const uid = String(userId || '').trim()
  
  if (!isNonEmpty(tid) || !isNonEmpty(uid)) throw new Error('缺少必要参数: teamId 或 userId')
  
  const code = Number(userRole)
  // 根据后端 UserRoleEnum: 0-OWNER, 1-MANAGER, 2-MEMBER
  // 只能修改为 MANAGER(1) 或 MEMBER(2)，不能设为 OWNER(0)
  if (!Number.isFinite(code) || ![1, 2].includes(code)) {
    throw new Error('角色仅支持数值枚举: 1(管理者) 或 2(普通成员)')
  }
  
  return http.put(`/teams/${encodeURIComponent(tid)}/members/${encodeURIComponent(uid)}/role`, { userRole: code })
}

/**
 * 交换本人团队排序
 * 接口路径: PUT /api/teams/order
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamIdA 团队A ID (必填)
 * @param {string} params.teamIdB 团队B ID (必填)
 * @returns {Promise} 返回 R<void>
 */
export const swapMyTeamOrder = ({ teamIdA, teamIdB } = {}) => {
  const a = String(teamIdA || '').trim()
  const b = String(teamIdB || '').trim()
  if (!isNonEmpty(a) || !isNonEmpty(b)) throw new Error('参数错误')
  if (a === b) throw new Error('排序值禁止一致')
  return http.put('/teams/order', { teamIdA: a, teamIdB: b })
}

/**
 * 修改本人在团队的颜色
 * 接口路径: PUT /api/teams/{teamId}/members/me/color
 * @param {Object} params 参数对象 (必填)
 * @param {string} params.teamId 团队ID (必填)
 * @param {string} params.teamColor 颜色 (必填, 6位十六进制 #xxxxxx)
 * @returns {Promise} 返回 R<void>
 */
export const updateMyTeamColor = ({ teamId, teamColor } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const color = String(teamColor || '').trim()
  if (!isHexColor(color)) throw new Error('颜色格式无效，需为 #xxxxxx（6位十六进制）')
  return http.put(`/teams/${encodeURIComponent(tid)}/members/me/color`, { teamColor: color })
}

/**
 * 我的邀请团队分页
 * 接口路径: GET /api/teams/me/invites
 * @param {Object} params 参数对象 (选填)
 * @param {number} [params.page] 页码 (选填, 默认1)
 * @param {number} [params.size] 每页条数 (选填, 默认10, 最大100)
 * @param {string} [params.memberStatus] 成员状态 (选填, 默认 '2'-邀请中)
 * @param {string} [params.teamName] 团队名称 (选填, 模糊)
 * @returns {Promise} 返回 R<Page<TeamUserRelation>>
 */
export const getMyInviteTeamPage = ({ page = 1, size = 10, memberStatus = '2', teamName = '' } = {}) => {
  const p = Number(page); const s = Number(size)
  const params = {
    page: Number.isFinite(p) && p > 0 ? p : 1,
    size: Number.isFinite(s) && s > 0 ? (s > 100 ? 100 : s) : 10
  }
  const ms = String(memberStatus || '2').trim()
  if (ms) params.memberStatus = ms
  if (isNonEmpty(teamName)) params.teamName = trim(teamName)
  return http.get('/teams/me/invites', params)
}

/**
 * 列出本人相关团队（进行中，按个人排序）
 * 接口路径: GET /api/teams/me
 * @returns {Promise} 返回 R<List<Team>>
 */
export const getMyRelatedTeamList = () => {
  return http.get('/teams/me')
}

/**
 * 查询团队基础信息
 * 接口路径: GET /api/teams/{teamId}
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<Team>
 */
export const getTeamBaseInfo = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  return http.get(`/teams/${encodeURIComponent(tid)}`)
}

/**
 * 团队数据大屏
 * 接口路径: GET /api/teams/{teamId}/dashboard
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<Object>
 */
export const getTeamDashboard = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  return http.get(`/teams/${encodeURIComponent(tid)}/dashboard`)
}

/**
 * 团队成员分页
 * 接口路径: GET /api/teams/{teamId}/members
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {number} [params.page] 页码 (选填, 默认1)
 * @param {number} [params.size] 每页条数 (选填, 默认10, 最大100)
 * @param {string} [params.userName] 用户名 (选填, 模糊)
 * @param {string|number} [params.memberRole] 角色筛选 (选填)
 * @param {string|number} [params.userStatus] 状态筛选 (选填)
 * @param {boolean} [params.invited] 是否仅显示邀请中 (选填)
 * @returns {Promise} 返回 R<Page<TeamUserRelation>>
 */
export const getTeamMemberPage = (teamId, { page = 1, size = 10, userName = '', memberRole, userStatus, invited } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const p = Number(page); const s = Number(size)
  const params = {
    page: Number.isFinite(p) && p > 0 ? p : 1,
    size: Number.isFinite(s) && s > 0 ? (s > 100 ? 100 : s) : 10
  }
  if (isNonEmpty(userName)) params.userName = trim(userName)
  
  if (memberRole !== undefined && memberRole !== null) {
    const m = String(memberRole).trim().toLowerCase()
    if (['owner', 'manager', 'member', '0', '1', '2', '3'].includes(m)) params.memberRole = m
  }
  
  if (userStatus !== undefined && userStatus !== null) {
    params.userStatus = userStatus
  }
  
  if (typeof invited === 'boolean') params.invited = invited
  return http.get(`/teams/${encodeURIComponent(tid)}/members`, params)
}

export default {
  createTeam,
  updateTeam,
  updateMemberRole,
  updateMyTeamColor,
  rejectInviteTeam,
  acceptInviteTeam,
  swapMyTeamOrder,
  getTeamMemberPage,
  inviteTeamMember,
  getTeamBaseInfo,
  deleteTeam,
  getTeamDashboard,
  getMyRelatedTeamList,
  getMyInviteTeamPage,
  deleteTeamMember,
  leaveTeam,
}
