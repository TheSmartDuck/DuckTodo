export const resultCode = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_ERROR: 500
}

export const resultCodeLabels = {
  200: '成功',
  400: '请求参数错误或业务校验失败',
  401: '未授权或令牌失效',
  403: '拒绝访问，无权限',
  404: '资源不存在',
  500: '服务器内部错误'
}

export const taskPriorityByCode = {
  0: 'P0|紧急优先级',
  1: 'P1|高优先级',
  2: 'P2|中优先级',
  3: 'P3|低优先级',
  4: 'P4|最低优先级'
}

export const userRoleByCode = {
  0: '创建者',
  1: '管理者',
  2: '普通成员'
}

export const ifOwnerByCode = {
  0: '否',
  1: '是'
}

export const taskAuditActionByCode = {
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  COMPLETE: '完成',
  CANCEL: '取消',
  ARCHIVE: '归档',
  RESTORE: '恢复'
}

export const userStatusByCode = {
  0: '禁用',
  1: '正常',
  2: '邀请中',
  3: '已拒绝'
}

const statusBinaryByCode = {
  0: '禁用',
  1: '正常'
}

export const taskGroupStatusByCode = statusBinaryByCode
export const taskNodeStatusByCode = statusBinaryByCode

export const userSexByCode = {
  0: '女',
  1: '男'
}

export const teamStatusByCode = {
  0: '已禁用',
  1: '进行中',
  2: '已结束'
}

export const taskStatusByCode = {
  0: '已禁用',
  1: '未开始',
  2: '进行中',
  3: '已完成',
  4: '已取消'
}

export const deleteStatusByCode = {
  0: '未删除',
  1: '已删除'
}

export const priorityColors = {
  0: '#802520', // P0 Urgent
  1: '#B2653B', // P1 High
  2: '#BA8530', // P2 Medium
  3: '#5C7F71', // P3 Low
  4: '#5C7F71'  // P4 Lowest
}

export default {
  resultCode,
  resultCodeLabels,
  taskPriorityByCode,
  userRoleByCode,
  ifOwnerByCode,
  taskAuditActionByCode,
  userStatusByCode,
  taskGroupStatusByCode,
  taskNodeStatusByCode,
  userSexByCode,
  teamStatusByCode,
  taskStatusByCode,
  deleteStatusByCode,
  priorityColors
}
