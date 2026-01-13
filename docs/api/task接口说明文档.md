# task接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `taskStatus` 枚举：0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消。
> 4. `taskPriority` 枚举：0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)。
> 5. `childTaskStatus` 枚举：同 `taskStatus`。

## 1. 创建任务
- **接口路径**: `POST /api/tasks`
- **功能描述**: 创建一个新的任务，可同时设置协助者、子任务。会自动将创建者设为拥有者。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无（参数均在 Body 中）
- **请求体 (JSON)**:
```json
{
  "taskGroupId": "tg_123456",            // 必填，任务族ID
  "taskName": "完成项目报告",             // 必填，至少2字符
  "taskDescription": "这是任务的详细描述信息",       // 可选
  "taskStatus": 1,                       // 可选，默认1 (1=未开始, 2=进行中, 3=已完成, 4=已取消)
  "taskPriority": 3,                     // 可选，默认3 (0=P0紧急, 1=P1高, 2=P2中, 3=P3低, 4=P4最低)
  "startTime": "2024-01-01",             // 可选，默认当天
  "dueTime": "2024-01-05",               // 必填，截止日期 (需 >= 当天 且 > 开始日期)
  "helperUserIdList": ["u_001", "u_002"],// 可选，协助者ID列表 (私人任务族不可添加)
  "childTaskList": [                     // 可选，子任务列表
    {
      "childTaskName": "收集数据",        // 必填
      "childTaskStatus": 1,              // 可选，默认1 (1-未开始, 2-进行中, 3-已完成, 4-已取消)
      "dueTime": "2024-01-03",           // 必填，不能晚于主任务
      "assigneeUserId": "u_001"          // 必填，需在协助者列表中或为创建者
    }
  ]
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "taskId": "t_987654",
    "taskGroupId": "tg_123456",
    "taskGroupName": "开发组任务",
    "teamId": "team_001",
    "teamName": "后端开发组",
    "taskName": "完成项目报告",
    "taskDescription": "这是任务的详细描述信息",
    "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
    "taskPriority": 3, // 0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)
    "startTime": "2024-01-01",
    "dueTime": "2024-01-05",
    "finishTime": null,
    "isOwner": 1,                        // 1=是拥有者, 0=否
    "taskHelperList": [                  // 包含拥有者和协助者
      {
        "taskUserRelationshipId": "r_111",
        "userId": "u_me",
        "ifOwner": true,
        "userName": "MyName",
        "userAvatar": "http://example.com/avatar.jpg",
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "childTaskList": [
      {
        "childTaskId": "ct_111",
        "childTaskName": "收集数据",
        "childTaskStatus": 1,              // 1=未开始, 2=进行中, 3=已完成, 4=已取消
        "childTaskIndex": 1,
        "childTaskAssigneeId": "u_001",
        "dueTime": "2024-01-03",
        "finishTime": null,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "attachments": []
  },
  "timestamp": 1704074400000
}
```

## 2. 更新任务
- **接口路径**: `PUT /api/tasks`
- **功能描述**: 更新任务的基础信息（名称、描述、状态、优先级、时间）。仅更新非空字段。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求体 (JSON)**:
```json
{
  "taskId": "t_987654",                  // 必填
  "taskName": "新名称",                   // 可选，至少2字符
  "taskDescription": "新描述",            // 可选
  "taskStatus": 2,                       // 可选, 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
  "taskPriority": 2,                     // 可选, 0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)
  "startTime": "2024-01-02",             // 可选
  "dueTime": "2024-01-06",               // 可选
  "finishTime": "2024-01-06"             // 可选
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "taskId": "t_987654",
    "taskGroupId": "tg_123456",
    "taskGroupName": "开发组任务",
    "teamId": "team_001",
    "teamName": "后端开发组",
    "taskName": "新名称",
    "taskDescription": "新描述",
    "taskStatus": 2, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
    "taskPriority": 2, // 0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)
    "startTime": "2024-01-02",
    "dueTime": "2024-01-06",
    "finishTime": "2024-01-06",
    "isOwner": 1,
    "taskHelperList": [
      {
        "taskUserRelationshipId": "r_111",
        "userId": "u_me",
        "ifOwner": true,
        "userName": "MyName",
        "userAvatar": "http://example.com/avatar.jpg",
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "childTaskList": [],
    "attachments": [],
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-02T10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 3. 删除任务
- **接口路径**: `DELETE /api/tasks/{taskId}`
- **功能描述**: 删除任务及其关联的所有数据（附件、子任务、关系、审计日志等）。仅拥有者可操作。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": null,
  "code": 200,
  "timestamp": 1704074400000
}
```

## 4. 获取任务详情
- **接口路径**: `GET /api/tasks/{taskId}`
- **功能描述**: 获取任务的完整详情，包括协助者、子任务、附件。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "taskId": "t_987654",
    "taskGroupId": "tg_123456",
    "taskGroupName": "开发组任务",
    "teamId": "team_001",
    "teamName": "后端开发组",
    "taskName": "完成项目报告",
    "taskDescription": "这是任务的详细描述信息",
    "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
    "taskPriority": 3, // 0-P0(紧急), 1-P1(高), 2-P2(中), 3-P3(低), 4-P4(最低)
    "startTime": "2024-01-01",
    "dueTime": "2024-01-05",
    "finishTime": null,
    "isOwner": 1,
    "taskHelperList": [
      {
        "taskUserRelationshipId": "r_111",
        "userId": "u_me",
        "ifOwner": true,
        "userName": "MyName",
        "userAvatar": "http://example.com/avatar.jpg",
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "childTaskList": [
      {
        "childTaskId": "ct_111",
      "childTaskName": "收集数据",
      "childTaskStatus": 1, // 1-未开始, 2-进行中, 3-已完成, 4-已取消
      "childTaskIndex": 1,
        "childTaskAssigneeId": "u_001",
        "dueTime": "2024-01-03",
        "finishTime": null,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "attachments": []
  },
  "timestamp": 1704074400000
}
```

## 5. 获取任务族任务列表（分页）
- **接口路径**: `GET /api/tasks/taskgroups/{taskGroupId}/tasks/page`
- **功能描述**: 分页查询指定任务族下的任务。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `taskGroupId`: 路径参数 (必填)
    - `page`: 页码 (可选，默认1)
    - `size`: 每页条数 (可选，默认10)
    - `taskName`: 模糊搜索 (可选)
    - `taskStatus`: 状态列表，如 `1,2` (可选)
    - `taskPriority`: 优先级列表 (可选)
    - `startDueTime`: 截止日期起 (可选)
    - `EndDueTime`: 截止日期止 (可选)
    - `sortByMode`: 排序 (可选，0=按截止时间, 1=按优先级; 默认按创建时间)
    - `relatedToMe`: `true`/`false` (可选，是否只看与我相关的)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "taskId": "t_123",
      "taskName": "任务1",
      "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
      "isOwner": 1,
        "childTaskList": []
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1704074400000
}
```

## 6. 获取任务族任务列表（不分页）
- **接口路径**: `GET /api/tasks/taskgroups/{taskGroupId}/tasks`
- **功能描述**: 列出指定任务族下的所有任务（筛选条件同分页接口）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `taskGroupId`: 路径参数 (必填)
    - `taskName`: 模糊搜索 (可选)
    - `taskStatus`: 状态列表，如 `1,2` (可选)
    - `taskPriority`: 优先级列表 (可选)
    - `startDueTime`: 截止日期起 (可选)
    - `EndDueTime`: 截止日期止 (可选)
    - `sortByMode`: 排序 (可选，0=按截止时间, 1=按优先级; 默认按创建时间)
    - `relatedToMe`: `true`/`false` (可选，是否只看与我相关的)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "taskId": "t_123",
      "taskName": "任务1",
      "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
      "isOwner": 1,
      "childTaskList": []
    }
  ],
  "timestamp": 1704074400000
}
```

## 7. 获取我的任务列表（分页）
- **接口路径**: `GET /api/tasks/me/page`
- **功能描述**: 分页查询与当前用户相关（拥有或协助）的任务。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `page`, `size`: 分页 (可选)
    - `taskGroupId`: 任务族ID列表 (可选，筛选范围)
    - `taskName`: 模糊搜索 (可选)
    - `taskStatus`: 状态列表，如 `1,2` (可选)
    - `taskPriority`: 优先级列表 (可选)
    - `startDueTime`: 截止日期起 (可选)
    - `EndDueTime`: 截止日期止 (可选)
    - `sortByMode`: 排序 (可选，0=按截止时间, 1=按优先级; 默认按创建时间)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "taskId": "t_123",
        "taskName": "任务1",
        "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
        "isOwner": 1,
        "childTaskList": []
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1704074400000
}
```

## 8. 获取我的任务列表（不分页）
- **接口路径**: `GET /api/tasks/me`
- **功能描述**: 列出与当前用户相关的所有任务。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `taskGroupId`: 任务族ID列表 (可选，筛选范围)
    - `taskName`: 模糊搜索 (可选)
    - `taskStatus`: 状态列表，如 `1,2` (可选)
    - `taskPriority`: 优先级列表 (可选)
    - `startDueTime`: 截止日期起 (可选)
    - `EndDueTime`: 截止日期止 (可选)
    - `sortByMode`: 排序 (可选，0=按截止时间, 1=按优先级; 默认按创建时间)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "taskId": "t_123",
      "taskName": "任务1",
      "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
      "isOwner": 1,
      "childTaskList": []
    }
  ],
  "timestamp": 1704074400000
}
```

## 9. 获取我的日程视图
- **接口路径**: `GET /api/tasks/me/schedule`
- **功能描述**: 按年/月查询我的任务（按截止日期在当月内筛选）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
    - `year`: 年份 (必填，如 2024)
    - `month`: 月份 (必填，1-12)
    - `taskGroupId`: 任务族ID列表 (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "taskId": "t_123",
      "taskName": "任务1",
      "taskStatus": 1, // 0-禁用, 1-未开始, 2-进行中, 3-已完成, 4-已取消
      "startTime": "2024-01-01",
      "dueTime": "2024-01-05"
    }
  ],
  "timestamp": 1704074400000
}
```

## 10. 添加子任务
- **接口路径**: `POST /api/tasks/children`
- **功能描述**: 为已有任务添加子任务。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求体 (JSON)**:
```json
{
  "taskId": "t_987654",                  // 必填，父任务ID
  "childTaskName": "子任务名",            // 必填
  "childTaskStatus": 1,                  // 可选, 1-未开始, 2-进行中, 3-已完成, 4-已取消
  "dueTime": "2024-01-05",               // 必填
  "childTaskAssigneeId": "u_001"         // 必填
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "添加成功",
  "data": {
    "childTaskId": "ct_999",
    "childTaskName": "子任务名",
    "childTaskStatus": 1, // 1-未开始, 2-进行中, 3-已完成, 4-已取消
    "childTaskAssigneeId": "u_001",
    "dueTime": "2024-01-05"
  },
  "timestamp": 1704074400000
}
```

## 11. 更新子任务
- **接口路径**: `PUT /api/tasks/{taskId}/children`
- **功能描述**: 更新子任务信息。
    - 注意：路径中的 `{taskId}` 仅用于路由匹配，实际校验与更新依赖 Body 中的 `childTaskId`。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求体 (JSON)**:
```json
{
  "childTaskId": "ct_123",               // 必填
  "childTaskName": "新名称",              // 可选
  "childTaskStatus": 2,                  // 可选
  "dueTime": "2024-01-06",               // 可选
  "finishTime": "2024-01-06",            // 可选
  "childTaskAssigneeId": "u_002"         // 可选 (必须是合法协助者)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "childTaskId": "ct_123",
    "childTaskName": "新名称",
    "childTaskStatus": 2, // 1-未开始, 2-进行中, 3-已完成, 4-已取消
    "dueTime": "2024-01-06",
    "finishTime": "2024-01-06",
    "childTaskAssigneeId": "u_002"
  },
  "timestamp": 1704074400000
}
```

## 12. 删除子任务
- **接口路径**: `DELETE /api/tasks/{taskId}/children/{childTaskId}`
- **功能描述**: 删除指定的子任务。
    - 注意：路径中的 `{taskId}` 仅用于路由匹配，实际删除对象为 `{childTaskId}`。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `childTaskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1704074400000
}
```

## 13. 子任务排序
- **接口路径**: `PUT /api/tasks/{taskId}/children/order`
- **功能描述**: 调整子任务的顺序。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **请求体 (JSON)**:
```json
[
  "ct_1", "ct_3", "ct_2"                 // 必填，有序的子任务ID列表
]
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "排序成功",
  "data": [
    { "childTaskId": "ct_1", "childTaskIndex": 1 },
    { "childTaskId": "ct_3", "childTaskIndex": 2 },
    { "childTaskId": "ct_2", "childTaskIndex": 3 }
  ],
  "timestamp": 1704074400000
}
```

## 14. 获取子任务列表
- **接口路径**: `GET /api/tasks/{taskId}/children`
- **功能描述**: 获取指定任务的子任务列表。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "childTaskId": "ct_1",
      "childTaskName": "子任务1",
      "childTaskStatus": 1 // 1-未开始, 2-进行中, 3-已完成, 4-已取消
    },
    {
      "childTaskId": "ct_2",
      "childTaskName": "子任务2",
      "childTaskStatus": 2 // 1-未开始, 2-进行中, 3-已完成, 4-已取消
    }
  ],
  "timestamp": 1704074400000
}
```

## 15. 添加协助者
- **接口路径**: `POST /api/tasks/{taskId}/followers`
- **功能描述**: 为任务添加协助者（仅拥有者可操作）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求体 (JSON)**:
```json
{
  "taskId": "t_987654",                  // 必填
  "userId": "u_003"                      // 必填，用户ID
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "添加成功",
  "data": {
    "taskUserRelationshipId": "r_999",
    "userId": "u_003",
    "taskId": "t_987654",
    "ifOwner": false
  },
  "timestamp": 1704074400000
}
```

## 16. 删除协助者
- **接口路径**: `DELETE /api/tasks/{taskId}/followers/{taskHelperId}`
- **功能描述**: 移除协助者（或协助者自行退出）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId`, `taskHelperId` (这里的 ID 是指 **用户ID (User ID)**)。
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1704074400000
}
```

## 17. 上传附件
- **接口路径**: `POST /api/tasks/{taskId}/files`
- **功能描述**: 上传任务附件。
- **请求 Header 要求**: `Content-Type: multipart/form-data` 及 认证Header。
- **请求参数**:
    - `file`: 文件 (MultipartFile, 必填)
    - `remark`: 备注 (String, 可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "上传成功",
  "data": {
    "taskFileId": "f_123",
    "fileName": "document.pdf",
    "fileUrl": "http://minio/bucket/document.pdf",
    "fileSize": 10240,
    "uploadTime": "2024-01-01 10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 18. 删除附件
- **接口路径**: `DELETE /api/tasks/{taskId}/files/{taskFileId}`
- **功能描述**: 删除指定附件。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId`, `taskFileId` (附件记录ID, 必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1704074400000
}
```

## 19. 获取附件列表
- **接口路径**: `GET /api/tasks/{taskId}/files`
- **功能描述**: 获取任务的有效附件列表。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "taskFileId": "f_123",
      "fileName": "document.pdf",
      "fileUrl": "http://minio/bucket/document.pdf",
      "uploadTime": "2024-01-01 10:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```

## 20. 获取审计日志
- **接口路径**: `GET /api/tasks/{taskId}/audits`
- **功能描述**: 获取任务的操作审计日志。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "taskAuditId": "a_100",
      "operatorId": "u_001",
      "operatorName": "Alice",
      "action": "UPDATE_STATUS",
      "details": "Changed status from 1 to 2",
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```
