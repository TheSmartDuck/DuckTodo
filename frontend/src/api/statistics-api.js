import http from '@/utils/http'
import { trim, isNonEmpty, normalizeLocalDate } from '@/utils/validation'

/**
 * 个人概览：加入的团队与任务族数量
 * 接口路径: GET /api/stats/me/overview/joined
 * @returns {Promise} 返回 R<Object> { teamCount, taskGroupCount }
 */
export const getMyJoinedOverview = () => http.get('/stats/me/overview/joined')

/**
 * 个人概览：进行中任务与子任务数量
 * 接口路径: GET /api/stats/me/overview/in_progress
 * @returns {Promise} 返回 R<Object> { tasks, childTasks }
 */
export const getMyInProgressOverview = () => http.get('/stats/me/overview/in_progress')

/**
 * 个人概览：本周完成数量
 * 接口路径: GET /api/stats/me/overview/completed/week
 * @returns {Promise} 返回 R<Object> { count }
 */
export const getMyCompletedWeekOverview = () => http.get('/stats/me/overview/completed/week')

/**
 * 个人概览：本月完成数量
 * 接口路径: GET /api/stats/me/overview/completed/month
 * @returns {Promise} 返回 R<Object> { count }
 */
export const getMyCompletedMonthOverview = () => http.get('/stats/me/overview/completed/month')

/**
 * 个人概览：累计完成数量
 * 接口路径: GET /api/stats/me/overview/completed/total
 * @returns {Promise} 返回 R<Object> { count }
 */
export const getMyCompletedTotalOverview = () => http.get('/stats/me/overview/completed/total')

/**
 * 个人概览：逾期统计（总计/中度/严重）
 * 接口路径: GET /api/stats/me/overview/overdue
 * @returns {Promise} 返回 R<Object> { total, moderate, severe }
 */
export const getMyOverdueOverview = () => http.get('/stats/me/overview/overdue')

/**
 * 个人负载趋势（近 N 天每日进行中任务与子任务）
 * 接口路径: GET /api/stats/me/trend/load
 * @param {number} [days] 统计天数 (选填, 默认14, 最大60)
 * @returns {Promise} 返回 R<Object> { items: [{ date, tasks, childTasks }] }
 */
export const getMyLoadTrend = (days) => {
  const n = Number(days)
  if (Number.isFinite(n)) {
    if (n > 60) throw new Error('统计天数最大为60')
  }
  const q = {}
  if (Number.isFinite(n) && n > 0) q.days = n
  return http.get('/stats/me/trend/load', q)
}

/**
 * 个人任务趋势（创建/完成）
 * 接口路径: GET /api/stats/me/trend/tasks
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.range] 范围 'week'|'month' (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ date, created, completed }] }
 */
export const getMyTaskTrend = ({ range, from, to } = {}) => {
  const q = {}
  const r = trim(range)
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f && t) { q.from = f; q.to = t } else if (isNonEmpty(r)) { q.range = r }
  return http.get('/stats/me/trend/tasks', q)
}

/**
 * 完成率趋势（每日 completed/created）
 * 接口路径: GET /api/stats/me/trend/completion
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.range] 范围 'week'|'month' (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ date, rate, created, completed }] }
 */
export const getMyCompletionTrend = ({ range, from, to } = {}) => {
  const q = {}
  const r = trim(range)
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f && t) { q.from = f; q.to = t } else if (isNonEmpty(r)) { q.range = r }
  return http.get('/stats/me/trend/completion', q)
}

/**
 * 活跃度趋势（审计事件数量）
 * 接口路径: GET /api/stats/me/trend/activity
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.range] 范围 'week'|'month' (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ date, count }] }
 */
export const getMyActivityTrend = ({ range, from, to } = {}) => {
  const q = {}
  const r = trim(range)
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f && t) { q.from = f; q.to = t } else if (isNonEmpty(r)) { q.range = r }
  return http.get('/stats/me/trend/activity', q)
}

/**
 * 优先级分布（任务）
 * 接口路径: GET /api/stats/me/distribution/priority
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.scope] 范围 'tasks'|'subs' (选填, 默认 tasks)
 * @returns {Promise} 返回 R<Object> { items: [{ priority, name, count }] }
 */
export const getMyPriorityDistribution = ({ scope } = {}) => {
  const q = {}
  const s = trim(scope)
  if (isNonEmpty(s)) q.scope = s
  return http.get('/stats/me/distribution/priority', q)
}

/**
 * 状态分布（任务/子任务）
 * 接口路径: GET /api/stats/me/distribution/status
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.scope] 范围 'tasks'|'subs' (选填, 默认 tasks)
 * @returns {Promise} 返回 R<Object> { items: [{ status, name, count }] }
 */
export const getMyStatusDistribution = ({ scope } = {}) => {
  const q = {}
  const s = trim(scope)
  if (isNonEmpty(s)) q.scope = s
  return http.get('/stats/me/distribution/status', q)
}

/**
 * 到期预报（DDL分析）
 * 接口路径: GET /api/stats/me/expiration/forecast
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.buckets] 桶定义 (选填, 默认 '7d,3d,today')
 * @param {string} [params.scope] 范围 'tasks'|'subs' (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ bucket, count }] }
 */
export const getMyExpirationForecast = ({ buckets, scope } = {}) => {
  const q = {}
  const b = trim(buckets)
  const s = trim(scope)
  if (isNonEmpty(b)) q.buckets = b
  if (isNonEmpty(s)) q.scope = s
  return http.get('/stats/me/expiration/forecast', q)
}

/**
 * 逾期统计（区间）
 * 接口路径: GET /api/stats/me/overdue
 * @param {Object} params 参数对象 (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { total, moderate, severe }
 */
export const getMyOverdueInRange = ({ from, to } = {}) => {
  const q = {}
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f) q.from = f
  if (t) q.to = t
  return http.get('/stats/me/overdue', q)
}

/**
 * 平均响应时间（MTTR）
 * 接口路径: GET /api/stats/me/mttr
 * @param {Object} params 参数对象 (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { averageHours, sampleSize }
 */
export const getMyMttr = ({ from, to } = {}) => {
  const q = {}
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f) q.from = f
  if (t) q.to = t
  return http.get('/stats/me/mttr', q)
}

/**
 * 团队概览统计
 * 接口路径: GET /api/stats/teams/{teamId}/overview
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<Object> { total, inProgress, completed, overdue, statusBuckets, priorityBuckets }
 */
export const getTeamOverview = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/overview`)
}

/**
 * 团队任务分布（状态/优先级）
 * 接口路径: GET /api/stats/teams/{teamId}/distribution
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.by] 维度 'status'|'priority' (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ status/priority, name, count }] }
 */
export const getTeamDistribution = (teamId, { by } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const q = {}
  const b = trim(by)
  if (isNonEmpty(b)) q.by = b
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/distribution`, q)
}

/**
 * 团队成员负载统计
 * 接口路径: GET /api/stats/teams/{teamId}/workload
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.scope] 范围 'in_progress'|'all' (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ userId, userName, inProgress, total }] }
 */
export const getTeamWorkload = (teamId, { scope } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const q = {}
  const s = trim(scope)
  if (isNonEmpty(s)) q.scope = s
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/workload`, q)
}

/**
 * 团队燃尽图
 * 接口路径: GET /api/stats/teams/{teamId}/burndown
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ date, remaining, completed }] }
 */
export const getTeamBurndown = (teamId, { from, to } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const q = {}
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f) q.from = f
  if (t) q.to = t
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/burndown`, q)
}

/**
 * 团队逾期统计
 * 接口路径: GET /api/stats/teams/{teamId}/overdue
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string|Date} [params.from] 起始日期 yyyy-MM-dd (选填)
 * @param {string|Date} [params.to] 结束日期 yyyy-MM-dd (选填)
 * @returns {Promise} 返回 R<Object> { total, memberBuckets, groupBuckets }
 */
export const getTeamOverdue = (teamId, { from, to } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const q = {}
  const f = normalizeLocalDate(from)
  const t = normalizeLocalDate(to)
  if (f) q.from = f
  if (t) q.to = t
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/overdue`, q)
}

/**
 * 团队活跃度趋势（审计）
 * 接口路径: GET /api/stats/teams/{teamId}/trend/activity
 * @param {string} teamId 团队ID (必填)
 * @param {Object} params 参数对象 (选填)
 * @param {string} [params.range] 范围 'week'|'month' (选填)
 * @returns {Promise} 返回 R<Object> { items: [{ date, count }] }
 */
export const getTeamActivityTrend = (teamId, { range } = {}) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  const q = {}
  const r = trim(range)
  if (isNonEmpty(r)) q.range = r
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/trend/activity`, q)
}

/**
 * 团队图谱摘要
 * 接口路径: GET /api/stats/teams/{teamId}/graph/summary
 * @param {string} teamId 团队ID (必填)
 * @returns {Promise} 返回 R<Object> { nodeCount, edgeCount, nodeBuckets, edgeBuckets }
 */
export const getTeamGraphSummary = (teamId) => {
  const tid = String(teamId || '').trim()
  if (!isNonEmpty(tid)) throw new Error('缺少团队ID')
  return http.get(`/stats/teams/${encodeURIComponent(tid)}/graph/summary`)
}

export default {
  getMyJoinedOverview,
  getMyInProgressOverview,
  getMyCompletedWeekOverview,
  getMyCompletedMonthOverview,
  getMyCompletedTotalOverview,
  getMyOverdueOverview,
  getMyLoadTrend,
  getMyTaskTrend,
  getMyCompletionTrend,
  getMyActivityTrend,
  getMyPriorityDistribution,
  getMyStatusDistribution,
  getMyExpirationForecast,
  getMyOverdueInRange,
  getMyMttr,
  getTeamOverview,
  getTeamDistribution,
  getTeamWorkload,
  getTeamBurndown,
  getTeamOverdue,
  getTeamActivityTrend,
  getTeamGraphSummary
}
