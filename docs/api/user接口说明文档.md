# User 用户接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `userSex` 枚举：0-女, 1-男, 2-保密。

## 1. 获取当前用户信息
- **接口路径**: `/api/user/me`
- **请求方法**: `GET`
- **功能描述**: 获取当前登录用户的详细信息。
- **请求 Header 要求**:
  - `Authorization: Bearer <token>` 或 `token: <token>`
  - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "OK",
  "data": {
    "userId": "u_123",
    "userName": "alice",
    "userEmail": "alice@example.com",
    "userPhone": "13800000000",
    "userSex": 0, // 0-女, 1-男, 2-保密
    "userAvatar": "https://example.com/user-avatar.jpg",
    "userRemark": null,
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-01T12:00:00"
  },
  "timestamp": 1730000000000
}
```

## 2. 根据 ID 获取用户信息
- **接口路径**: `/api/user/{userId}`
- **请求方法**: `GET`
- **功能描述**: 查询指定用户ID的公开信息。
- **请求 Header 要求**:
  - `Authorization: Bearer <token>` 或 `token: <token>`
  - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `userId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "OK",
  "data": {
    "userId": "u_123",
    "userName": "alice",
    "userEmail": "alice@example.com",
    "userPhone": "13800000000",
    "userSex": 0, // 0-女, 1-男, 2-保密
    "userAvatar": "https://example.com/user-avatar.jpg",
    "userRemark": null,
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-01T12:00:00"
  },
  "timestamp": 1730000000000
}
```

## 3. 更新我的资料
- **接口路径**: `/api/user/me`
- **请求方法**: `PUT`
- **功能描述**: 修改当前用户的个人信息（不支持修改用户名）。
- **请求 Header 要求**:
  - `Authorization: Bearer <token>` 或 `token: <token>`
  - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
  - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userEmail": "alice_new@example.com", // 可选
  "userPhone": "13800000000",           // 可选
  "userSex": 0,                         // 可选 (枚举: 0-女, 1-男, 2-保密)
  "userRemark": "个人备注"               // 可选
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": "u_123",
    "userName": "alice",
    "userEmail": "alice_new@example.com",
    "userPhone": "13800000000",
    "userSex": 0, // 0-女, 1-男, 2-保密
    "userAvatar": "https://example.com/user-avatar.jpg",
    "userRemark": "个人备注",
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-02T10:00:00"
  },
  "timestamp": 1730000000000
}
```

## 4. 获取 AK/SK
- **接口路径**: `/api/user/me/access-keys`
- **请求方法**: `GET`
- **功能描述**: 获取当前用户的 API 访问密钥。
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
  "data": {
    "userAccesskey": "ak_1234567890abcdef",
    "userSecretkey": "sk_1234567890abcdef"
  },
  "timestamp": 1730000000000
}
```

## 5. 更新 AK/SK
- **接口路径**: `/api/user/me/access-keys`
- **请求方法**: `PUT`
- **功能描述**: 更新当前用户的 API 访问密钥。
- **请求 Header 要求**:
  - `Authorization: Bearer <token>` 或 `token: <token>`
  - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
  - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "userAccesskey": "new_ak_1234567890abcdef", // 必填
  "userSecretkey": "new_sk_1234567890abcdef"  // 必填
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "userAccesskey": "new_ak_1234567890abcdef",
    "userSecretkey": "new_sk_1234567890abcdef"
  },
  "timestamp": 1730000000000
}
```

## 6. 删除 AK/SK
- **接口路径**: `/api/user/me/access-keys`
- **请求方法**: `DELETE`
- **功能描述**: 删除（清空）当前用户的 API 访问密钥。
- **请求 Header 要求**:
  - `Authorization: Bearer <token>` 或 `token: <token>`
  - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 无
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": {
    "userAccesskey": null,
    "userSecretkey": null
  },
  "timestamp": 1730000000000
}
```
