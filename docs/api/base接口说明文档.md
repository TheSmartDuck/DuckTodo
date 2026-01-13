# Base 基础接口说明文档

> **注意**：
> 1. 本文档中的接口通常 **无需鉴权**（Header 中不需要 Token），除非特别说明。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `userSex` 枚举：0-女, 1-男, 2-保密。

## 1. 系统健康检查
- **接口路径**: `/api/base/health`
- **请求方法**: `GET`
- **功能描述**: 检查系统运行状态及依赖服务连通性。
- **请求 Header 要求**: 无需鉴权
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "appName": "ducktodo-backend",
    "version": "0.0.1-SNAPSHOT",
    "serverTime": "2024-01-01 12:00:00",
    "timestamp": 1704081600000,
    "storagePath": "http://localhost:9000/ducktodo/"
  },
  "timestamp": 1704081600000
}
```

## 2. 登录
- **接口路径**: `/api/base/login`
- **请求方法**: `POST`
- **功能描述**: 用户登录，获取 JWT Token。
- **请求 Header 要求**: `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userName": "alice",             // 可选 (userName 与 userEmail 二选一必填)
  "userEmail": "alice@example.com",// 可选 (userName 与 userEmail 二选一必填)
  "userPassword": "your_password"  // 必填
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "<jwt_token>",
    "user": {
      "userId": "u_123",
      "userName": "alice",
      "userEmail": "alice@example.com",
      "userPhone": "13800000000",
      "userSex": 0, // 0-女, 1-男, 2-保密
      "userAvatar": "https://minio.example.com/ducktodo/default-user-avatar.png"
    }
  },
  "timestamp": 1730000000000
}
```

## 3. 注册
- **接口路径**: `/api/base/register`
- **请求方法**: `POST`
- **功能描述**: 注册新用户，并自动登录。
- **请求 Header 要求**: `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userName": "alice",             // 必填
  "userEmail": "alice@example.com",// 必填 (需符合邮箱格式)
  "userPassword": "your_password", // 必填
  "userPhone": "13800000000",      // 必填 (需符合11位手机号格式)
  "userSex": 0                     // 必填 (枚举: 0-女, 1-男, 2-保密)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "<jwt_token>",
    "user": {
      "userId": "u_123",
      "userName": "alice",
      "userEmail": "alice@example.com",
      "userPhone": "13800000000",
      "userSex": 0, // 0-女, 1-男, 2-保密
      "userAvatar": "https://minio.example.com/ducktodo/default-user-avatar.png"
    }
  },
  "timestamp": 1730000000000
}
```
