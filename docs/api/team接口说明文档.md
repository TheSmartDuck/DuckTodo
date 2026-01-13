# Team 模块接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `teamStatus` 枚举：0-禁用, 1-进行中, 2-已结束。
> 4. `userRole` 枚举：1-Owner, 2-Manager, 3-Member。
> 5. `userStatus` 枚举：1-Normal, 2-Inviting, 3-Rejected。

## 1. 创建团队
- **接口路径**: `POST /api/teams`
- **功能描述**: 创建团队，初始化任务族与成员关系，并可邀请成员。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "teamName": "新团队名称",         // 必填 (长度>=2)
  "teamDescription": "团队描述信息", // 可选
  "teamAvatar": "https://example.com/team-avatar.jpg",      // 可选
  "teamStatus": 1,                  // 可选 (枚举: 0-禁用, 1-进行中, 2-已结束; 默认1)
  "invitedMemberList": [            // 可选 (初始邀请成员列表)
    {
      "userId": "u_1001",           // 必填
      "memberRole": 2               // 必填 (枚举: 2-Manager, 3-Member)
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
    "teamId": "t_123456789",
    "teamName": "新团队名称",
    "teamDescription": "团队描述信息",
    "teamAvatar": "https://example.com/team-avatar.jpg",
    "teamStatus": 1, // 0-禁用, 1-进行中, 2-已结束
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 2. 邀请成员加入团队
- **接口路径**: `POST /api/teams/{teamId}/members`
- **功能描述**: 邀请成员加入团队（支持 manager/member 角色）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userId": "u_1003", // 必填 (被邀请用户ID)
  "userRole": 3       // 必填 (枚举: 2-Manager, 3-Member)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "邀请成功",
  "data": {
    "teamUserRelationId": "tur_987654321",
    "teamId": "t_123456789",
    "userId": "u_1003",
    "userRole": 3,   // 1-Owner, 2-Manager, 3-Member
    "userStatus": 2, // 1-Normal, 2-Inviting, 3-Rejected
    "teamIndex": 0,
    "joinTime": "2024-01-01T10:05:00"
  },
  "timestamp": 1704074700000
}
```

## 3. 删除团队
- **接口路径**: `DELETE /api/teams/{teamId}`
- **功能描述**: 删除团队及其关联数据。仅 Owner 可操作。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `teamId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": null,
  "timestamp": 1704074400000
}
```

## 4. 退出团队
- **接口路径**: `DELETE /api/teams/{teamId}/members/me`
- **功能描述**: 当前用户退出团队（Owner 不可退出，需先移交或删除团队）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `teamId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1704074400000
}
```

## 5. 删除成员
- **接口路径**: `DELETE /api/teams/{teamId}/members/{userId}`
- **功能描述**: 将指定成员移出团队（仅 Owner/Manager 可操作，不能移除 Owner）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `teamId`, `userId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1704074400000
}
```

## 6. 更新团队信息
- **接口路径**: `PUT /api/teams`
- **功能描述**: 修改团队名称、描述、状态。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "teamId": "t_123",              // 必填
  "teamName": "新名称",            // 可选 (长度>=2)
  "teamDescription": "新描述",     // 可选
  "teamStatus": 1                 // 可选 (枚举: 0-禁用, 1-进行中, 2-已结束)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "teamId": "t_123",
    "teamName": "新名称",
    "teamDescription": "新描述",
    "teamAvatar": "https://example.com/team-avatar.jpg",
    "teamStatus": 1, // 0-禁用, 1-进行中, 2-已结束
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-02T10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 7. 接受/拒绝邀请
- **接口路径**:
  - 接受: `PUT /api/teams/{teamId}/invites/me/accept`
  - 拒绝: `PUT /api/teams/{teamId}/invites/me/reject`
- **功能描述**: 处理团队加入邀请。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `teamId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1704074400000
}
```

## 8. 修改成员角色
- **接口路径**: `PUT /api/teams/{teamId}/members/{userId}/role`
- **功能描述**: 修改成员角色（仅 Owner/Manager 可操作）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userRole": 2 // 必填 (枚举: 2-Manager, 3-Member)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1704074400000
}
```

## 9. 获取我的团队列表
- **接口路径**: `GET /api/teams/me`
- **功能描述**: 查询本人已加入（Normal）的团队列表。
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
  "data": [
    {
      "teamId": "t_123456789",
      "teamName": "我的团队",
      "teamDescription": "描述",
      "teamAvatar": "https://example.com/team-avatar.jpg",
      "teamStatus": 1, // 0-禁用, 1-进行中, 2-已结束
      "myRole": 1,         // 1-Owner, 2-Manager, 3-Member
      "joinTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```

## 10. 获取团队成员列表
- **接口路径**: `GET /api/teams/{teamId}/members`
- **功能描述**: 分页查询团队成员。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**:
  - `page`: 页码 (可选, 默认1)
  - `size`: 页大小 (可选, 默认10)
  - `userName`: 用户名模糊搜索 (可选)
  - `userRole`: 角色筛选 (可选)
  - `userStatus`: 状态筛选 (可选)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "teamUserRelationId": "tur_111",
        "teamId": "t_123",
        "userId": "u_001",
        "userName": "张三",
        "userAvatar": "https://example.com/avatar.jpg",
        "userRole": 1, // 1-Owner, 2-Manager, 3-Member
        "userStatus": 1, // 1-Normal, 2-Inviting, 3-Rejected
        "joinTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1704074400000
}
```
