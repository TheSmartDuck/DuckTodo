# TaskGroup 模块接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `groupStatus` 枚举：0-禁用, 1-正常。
> 4. `userRole` 枚举：1-Owner, 2-Manager, 3-Member。
> 5. `userStatus` 枚举：1-Normal, 2-Inviting, 3-Rejected。
> 6. 颜色格式需为 `#xxxxxx`（6 位十六进制）。

## 1. 创建私有任务族
- **接口路径**: `POST /api/taskgroups`
- **功能描述**: 创建私有任务族（仅当前登录用户）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "groupName": "我的任务族",        // 必填 (长度>=2)
  "groupDescription": "示例描述",   // 可选
  "groupStatus": 1,                // 可选 (枚举: 0-禁用, 1-正常; 默认1)
  "groupColor": "#409EFF"          // 可选 (默认系统颜色)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "taskGroupId": "tg_123456789",
    "teamId": "",
    "groupName": "我的任务族",
    "groupDescription": "示例描述",
    "groupStatus": 1,                // 0-禁用, 1-正常
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 2. 更新私有任务族
- **接口路径**: `PUT /api/taskgroups`
- **功能描述**: 更新私有任务族信息（仅创建者）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "taskGroupId": "tg_123456789",         // 必填
  "groupName": "我的任务族（更新）",       // 必填 (长度>=2)
  "groupDescription": "更新描述",          // 可选
  "groupStatus": 1                        // 可选 (枚举: 0-禁用, 1-正常)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "taskGroupId": "tg_123456789",
    "groupName": "我的任务族（更新）",
    "groupDescription": "更新描述",
    "groupStatus": 1,                // 0-禁用, 1-正常
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-02T10:00:00"
  },
  "timestamp": 1704074400000
}
```

## 3. 删除私有任务族
- **接口路径**: `DELETE /api/taskgroups/{taskGroupId}`
- **功能描述**: 删除私有任务族及其关联的所有数据（任务、子任务、节点、文件等）。仅创建者可操作。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `taskGroupId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": true,
  "timestamp": 1704074400000
}
```

## 4. 交换任务族排序
- **接口路径**: `PUT /api/taskgroups/order`
- **功能描述**: 交换个人两个任务族的排序。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "taskGroupId1": "tg_1", // 必填
  "taskGroupId2": "tg_2"  // 必填
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

## 5. 修改任务族颜色
- **接口路径**: `PUT /api/taskgroups/{taskGroupId}/members/me/color`
- **功能描述**: 修改当前用户对该任务族的颜色标记（个性化）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "groupColor": "#FF0000" // 必填 (6位Hex)
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

## 6. 修改任务族别名
- **接口路径**: `PUT /api/taskgroups/{taskGroupId}/members/me/alias`
- **功能描述**: 修改当前用户对该任务族的别名（个性化）。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "groupAlias": "我的别名" // 必填
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

## 7. 获取我的任务族列表
- **接口路径**: `GET /api/taskgroups/me`
- **功能描述**: 列出当前用户相关任务族（按关系排序）。
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
      "taskGroupId": "tg_123456789",
      "teamId": "",
      "groupName": "我的任务族",
      "groupDescription": "示例描述",
      "groupStatus": 1,
      "myRelation": {
          "alias": "别名",
          "color": "#409EFF"
      },
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```
