# DingTalk Robot 配置接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。

## 1. 获取当前用户的钉钉机器人配置列表
- **接口路径**: `GET /api/dingtalk-robot-configs`
- **功能描述**: 获取当前登录用户的所有钉钉机器人配置。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "userDingtalkRobotId": "robot_123456789",
      "userId": "u_123456",
      "robotName": "监控报警机器人",
      "dingtalkRobotToken": "your_robot_token",
      "dingtalkRobotSecret": "your_robot_secret",
      "dingtalkRobotKeyword": "报警",
      "isDelete": 0,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```

## 2. 添加钉钉机器人配置
- **接口路径**: `POST /api/dingtalk-robot-configs`
- **功能描述**: 为当前用户添加一个新的钉钉机器人配置。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "robotName": "新机器人",             // 必填
  "dingtalkRobotToken": "token_example_123456",   // 必填
  "dingtalkRobotSecret": "secret_example_123456", // 必填
  "dingtalkRobotKeyword": "关键字"      // 必填 (钉钉安全设置中的自定义关键字)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "userDingtalkRobotId": "robot_new_123",
    "userId": "u_123456",
    "robotName": "新机器人",
    "dingtalkRobotToken": "token_example_123456",
    "dingtalkRobotSecret": "secret_example_123456",
    "dingtalkRobotKeyword": "关键字",
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-03T12:00:00"
  },
  "timestamp": 1704247200000
}
```

## 3. 更改钉钉机器人配置
- **接口路径**: `PUT /api/dingtalk-robot-configs/{robotId}`
- **功能描述**: 更新指定 ID 的钉钉机器人配置信息。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "robotName": "更新名称",             // 可选
  "dingtalkRobotToken": "new_token",   // 可选
  "dingtalkRobotSecret": "new_secret", // 可选
  "dingtalkRobotKeyword": "new_key"    // 可选
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "userDingtalkRobotId": "robot_new_123",
    "userId": "u_123456",
    "robotName": "更新名称",
    "dingtalkRobotToken": "new_token",
    "dingtalkRobotSecret": "new_secret",
    "dingtalkRobotKeyword": "new_key",
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-04T12:00:00"
  },
  "timestamp": 1704247200000
}
```

## 4. 删除钉钉机器人配置
- **接口路径**: `DELETE /api/dingtalk-robot-configs/{robotId}`
- **功能描述**: 删除指定的钉钉机器人配置。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `robotId` (必填)
- **返回数据 (JSON)**: 成功返回 true。

## 5. 获取单个钉钉机器人配置
- **接口路径**: `GET /api/dingtalk-robot-configs/{robotId}`
- **功能描述**: 获取指定 ID 的配置详情。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `robotId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "userDingtalkRobotId": "robot_new_123",
    "userId": "u_123456",
    "robotName": "新机器人",
    "dingtalkRobotToken": "token_example_123456",
    "dingtalkRobotSecret": "secret_example_123456",
    "dingtalkRobotKeyword": "关键字",
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-03T12:00:00"
  },
  "timestamp": 1704247200000
}
```
