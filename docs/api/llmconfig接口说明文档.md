# LLM 配置接口说明文档

> **注意**：
> 1. 所有接口均需鉴权。
> 2. 返回格式统一为 `R<T>`，包含 `success`、`code`、`message`、`data`、`timestamp`。
> 3. `llmModelThinking` 枚举：0-否, 1-是。

## 1. 获取当前用户的 LLM 配置列表
- **接口路径**: `GET /api/llm-configs`
- **功能描述**: 获取当前登录用户的所有 LLM 配置。
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
      "userLlmConfigId": "llm_123456789",
      "userId": "u_123456",
      "llmProvider": "openai",
      "llmApiKey": "sk-xxxxxxxx",
      "llmApiUrl": "https://api.openai.com/v1",
      "llmModelName": "gpt-4",
      "llmModelTemperature": 0.7,
      "llmModelThinking": 0, // 0-否, 1-是
      "isDelete": 0,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1704074400000
}
```

## 2. 添加 LLM 配置
- **接口路径**: `POST /api/llm-configs`
- **功能描述**: 为当前用户添加一个新的 LLM 配置。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "llmProvider": "openai",             // 必填
  "llmApiKey": "sk-new-api-key",       // 必填
  "llmApiUrl": "https://api.openai.com/v1",      // 必填
  "llmModelName": "gpt-3.5-turbo",     // 必填
  "llmModelTemperature": 0.8,          // 可选 (默认值视后端而定)
  "llmModelThinking": 0                // 可选 (0-否, 1-是; 默认0)
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "userLlmConfigId": "llm_new_123",
    "userId": "u_123456",
    "llmProvider": "openai",
    "llmApiKey": "sk-new-api-key",
    "llmApiUrl": "https://api.openai.com/v1",
    "llmModelName": "gpt-3.5-turbo",
    "llmModelTemperature": 0.8,
    "llmModelThinking": 0,               // 0-否, 1-是
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-03T12:00:00"
  },
  "timestamp": 1704247200000
}
```

## 3. 更改 LLM 配置
- **接口路径**: `PUT /api/llm-configs/{configId}`
- **功能描述**: 更新指定 ID 的 LLM 配置信息。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
    - `Content-Type: application/json`
- **请求参数 (JSON)**:
```json
{
  "llmProvider": "ollama",             // 可选
  "llmApiKey": "new-key",              // 可选
  "llmApiUrl": "http://localhost:11434/api/generate", // 可选
  "llmModelName": "llama3",            // 可选
  "llmModelTemperature": 0.5,          // 可选
  "llmModelThinking": 1                // 可选, 0-否, 1-是
}
```
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "userLlmConfigId": "llm_new_123",
    "userId": "u_123456",
    "llmProvider": "ollama",
    "llmApiKey": "new-key",
    "llmApiUrl": "http://localhost:11434/api/generate",
    "llmModelName": "llama3",
    "llmModelTemperature": 0.5,
    "llmModelThinking": 1,               // 0-否, 1-是
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-04T12:00:00"
  },
  "timestamp": 1704247200000
}
```

## 4. 删除 LLM 配置
- **接口路径**: `DELETE /api/llm-configs/{configId}`
- **功能描述**: 删除指定的 LLM 配置。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `configId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1704247200000
}
```

## 5. 获取单个 LLM 配置
- **接口路径**: `GET /api/llm-configs/{configId}`
- **功能描述**: 获取指定 ID 的配置详情。
- **请求 Header 要求**:
    - `Authorization: Bearer <token>` 或 `token: <token>`
    - `access_token: <AK>` 与 `secret_Token: <SK>` (仅限 AK/SK 认证)
- **请求参数**: 路径参数 `configId` (必填)
- **返回数据 (JSON)**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "userLlmConfigId": "llm_new_123",
    "userId": "u_123456",
    "llmProvider": "openai",
    "llmApiKey": "sk-new-api-key",
    "llmApiUrl": "https://api.openai.com/v1",
    "llmModelName": "gpt-3.5-turbo",
    "llmModelTemperature": 0.8,
    "llmModelThinking": 0,               // 0-否, 1-是
    "isDelete": 0,
    "createTime": "2024-01-03T12:00:00",
    "updateTime": "2024-01-03T12:00:00"
  },
  "timestamp": 1704247200000
}
```
