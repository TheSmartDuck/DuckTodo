# 统计模块接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. 日期格式：`yyyy-MM-dd`。

## 1. 个人概览统计（加入的团队/任务族）
- **接口路径**: `GET /api/stats/me/overview/joined`
- **功能描述**: 统计当前用户加入且状态正常（Normal）的团队数量和任务族数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "teamCount": 3,
    "taskGroupCount": 5
  },
  "timestamp": 1704074400000
}
```

## 2. 个人概览统计（进行中）
- **接口路径**: `GET /api/stats/me/overview/in_progress`
- **功能描述**: 统计与当前用户有关的主任务（进行中）和子任务（进行中）的数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "tasks": 12,
    "childTasks": 5
  },
  "timestamp": 1704074400000
}
```

## 3. 个人概览统计（本周完成）
- **接口路径**: `GET /api/stats/me/overview/completed/week`
- **功能描述**: 统计当前周内（周一至周日）用户相关任务的完成数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "count": 8
  },
  "timestamp": 1704074400000
}
```

## 4. 个人概览统计（本月完成）
- **接口路径**: `GET /api/stats/me/overview/completed/month`
- **功能描述**: 统计当前月份内用户相关任务的完成数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "count": 25
  },
  "timestamp": 1704074400000
}
```

## 5. 个人概览统计（累计完成）
- **接口路径**: `GET /api/stats/me/overview/completed/total`
- **功能描述**: 统计历史累计完成的用户相关任务数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "count": 150
  },
  "timestamp": 1704074400000
}
```

## 6. 个人概览统计（逾期概览）
- **接口路径**: `GET /api/stats/me/overview/overdue`
- **功能描述**: 统计用户相关任务中（未开始或进行中）且已逾期的数量，并分为中度逾期（≤3天）和严重逾期（>3天）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,
    "moderate": 3,
    "severe": 2
  },
  "timestamp": 1704074400000
}
```

## 7. 个人负载趋势
- **接口路径**: `GET /api/stats/me/trend/load`
- **功能描述**: 统计近 N 天每日处于“进行中”的主任务数量与子任务数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `days`: 统计天数 (可选，默认 14，最大 60)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "date": "2024-01-01",
        "tasks": 10,
        "childTasks": 4
      },
      {
        "date": "2024-01-02",
        "tasks": 11,
        "childTasks": 5
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 8. 个人任务趋势（创建/完成）
- **接口路径**: `GET /api/stats/me/trend/tasks`
- **功能描述**: 按日统计用户相关任务的创建数与完成数。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `range`: `week`（近7天）或 `month`（近30天） (可选)
    - `from`: 起始日期 `yyyy-MM-dd` (可选)
    - `to`: 结束日期 `yyyy-MM-dd` (可选)
    - 说明: 若指定 `from`/`to` 则忽略 `range`。
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "date": "2024-01-01",
        "created": 2,
        "completed": 1
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 9. 个人完成率趋势
- **接口路径**: `GET /api/stats/me/trend/completion`
- **功能描述**: 每日计算完成率（完成数/创建数）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `range`: `week`（近7天）或 `month`（近30天） (可选)
    - `from`: 起始日期 `yyyy-MM-dd` (可选)
    - `to`: 结束日期 `yyyy-MM-dd` (可选)
    - 说明: 若指定 `from`/`to` 则忽略 `range`。
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "date": "2024-01-01",
        "rate": 0.5,
        "created": 2,
        "completed": 1
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 10. 个人活跃度趋势（审计）
- **接口路径**: `GET /api/stats/me/trend/activity`
- **功能描述**: 基于操作日志统计每日操作事件数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `range`: `week`（近7天）或 `month`（近30天） (可选)
    - `from`: 起始日期 `yyyy-MM-dd` (可选)
    - `to`: 结束日期 `yyyy-MM-dd` (可选)
    - 说明: 若指定 `from`/`to` 则忽略 `range`。
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "date": "2024-01-01",
        "count": 15
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 11. 个人优先级分布
- **接口路径**: `GET /api/stats/me/distribution/priority`
- **功能描述**: 统计用户相关主任务的优先级占比。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `scope`: `tasks` (主任务) 或 `subs` (子任务) (可选，默认 tasks)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "priority": 1,           // 0=P0紧急, 1=P1高, 2=P2中, 3=P3低, 4=P4最低
        "name": "普通",
        "count": 10
      },
      {
        "priority": 3,
        "name": "紧急",
        "count": 2
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 12. 个人状态分布
- **接口路径**: `GET /api/stats/me/distribution/status`
- **功能描述**: 统计任务或子任务的状态占比。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `scope`: `tasks` (主任务) 或 `subs` (子任务) (可选，默认 tasks)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "status": 1,
        "name": "未开始",
        "count": 5
      },
      {
        "status": 2,
        "name": "进行中",
        "count": 8
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 13. 个人任务到期预报
- **接口路径**: `GET /api/stats/me/expiration/forecast`
- **功能描述**: 按指定时间桶统计未来到期数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `buckets`: 桶定义，逗号分隔 (可选，默认 `7d,3d,today`)
    - `scope`: `tasks` 或 `subs` (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "bucket": "7d",
        "count": 10
      },
      {
        "bucket": "today",
        "count": 2
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 14. 个人逾期统计（指定区间）
- **接口路径**: `GET /api/stats/me/overdue`
- **功能描述**: 统计指定日期区间内且已逾期的任务总量与分桶。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `from`: 起始日期 `yyyy-MM-dd` (可选)
    - `to`: 结束日期 `yyyy-MM-dd` (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 4,
    "moderate": 2,
    "severe": 2
  },
  "timestamp": 1704074400000
}
```

## 15. 个人平均响应时间 (MTTR)
- **接口路径**: `GET /api/stats/me/mttr`
- **功能描述**: 统计区间内任务从开始到完成的平均耗时（小时）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `from`: 起始日期 (可选)
    - `to`: 结束日期 (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "averageHours": 48.5,
    "sampleSize": 20
  },
  "timestamp": 1704074400000
}
```

## 16. 团队概览统计
- **接口路径**: `GET /api/stats/teams/{teamId}/overview`
- **功能描述**: 汇总团队任务总量、进行中、已完成、逾期数及分布。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `teamId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "inProgress": 30,
    "completed": 60,
    "overdue": 5,
    "statusBuckets": [
      { "status": 2, "name": "进行中", "count": 30 } // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
    ],
    "priorityBuckets": [
      { "priority": 1, "name": "普通", "count": 80 } // 0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)
    ]
  },
  "timestamp": 1704074400000
}
```

## 17. 团队任务分布
- **接口路径**: `GET /api/stats/teams/{teamId}/distribution`
- **功能描述**: 统计团队任务的状态或优先级分布。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
    - `by`: `status` (状态) 或 `priority` (优先级) (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      { "status": 2, "name": "进行中", "count": 30 } // 1=未开始, 2=进行中, 3=已完成, 4=已取消
    ]
  },
  "timestamp": 1704074400000
}
```

## 18. 团队成员负载统计
- **接口路径**: `GET /api/stats/teams/{teamId}/workload`
- **功能描述**: 统计团队成员的任务负载情况。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
    - `scope`: `in_progress` (仅进行中) 或 `all` (全部) (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "userId": "u_1",
        "userName": "Alice",
        "inProgress": 5,
        "total": 20
      },
      {
        "userId": "u_2",
        "userName": "Bob",
        "inProgress": 3,
        "total": 15
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 19. 团队燃尽图
- **接口路径**: `GET /api/stats/teams/{teamId}/burndown`
- **功能描述**: 按日统计团队任务的剩余量与完成量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
    - `from`, `to`: 日期区间 (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      {
        "date": "2024-01-01",
        "remaining": 50,
        "completed": 2
      },
      {
        "date": "2024-01-02",
        "remaining": 45,
        "completed": 5
      }
    ]
  },
  "timestamp": 1704074400000
}
```

## 20. 团队逾期统计（详细）
- **接口路径**: `GET /api/stats/teams/{teamId}/overdue`
- **功能描述**: 统计团队区间内逾期总量，并按成员和任务族聚合。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
    - `from`, `to`: 日期区间 (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "memberBuckets": [
      { "userId": "u_1", "userName": "Alice", "count": 4 }
    ],
    "groupBuckets": [
      { "groupId": "g_1", "groupName": "Dev Group", "count": 6 }
    ]
  },
  "timestamp": 1704074400000
}
```

## 21. 团队活跃度趋势
- **接口路径**: `GET /api/stats/teams/{teamId}/trend/activity`
- **功能描述**: 统计团队每日操作事件数量。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
    - `range`: `week` 或 `month` (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "items": [
      { "date": "2024-01-01", "count": 25 }
    ]
  },
  "timestamp": 1704074400000
}
```

## 22. 团队图谱摘要
- **接口路径**: `GET /api/stats/teams/{teamId}/graph/summary`
- **功能描述**: 统计团队任务图谱的节点/边数量与类型分布。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `teamId`: 路径参数 (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "nodeCount": 50,
    "edgeCount": 40,
    "nodeBuckets": [
      { "type": "task", "count": 45 },
      { "type": "milestone", "count": 5 }
    ],
    "edgeBuckets": [
      { "type": "dependency", "count": 30 },
      { "type": "reference", "count": 10 }
    ]
  },
  "timestamp": 1704074400000
}
```
