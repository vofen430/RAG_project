# 接口设计文档

## 1. 范围

本说明书仅定义应用对接接口。

模型供应商侧 API 不在此文档中展开。实现团队应参考官方 model API manual 完成供应商集成。

## 2. 通用约定

基础路径：

- `/api`

认证方式：

- 在 `Authorization` 请求头中使用 Bearer token

内容类型：

- `application/json`
- `multipart/form-data`
- `text/event-stream`

通用响应字段：

- `code`
- `message`
- `data`

## 3. 认证接口

### 3.1 登录

- Method: `POST`
- Path: `/api/auth/login`

请求体：

```json
{
  "username": "demo",
  "password": "demo-password"
}
```

响应体：

```json
{
  "code": "OK",
  "message": "Login succeeded",
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": "uuid",
      "username": "demo",
      "displayName": "Demo User",
      "roleCode": "ADMIN"
    }
  }
}
```

### 3.2 当前用户

- Method: `GET`
- Path: `/api/auth/me`

## 4. 文档接口

### 4.1 上传文档

- Method: `POST`
- Path: `/api/documents`

请求：

- Multipart 字段：`file`

响应体：

```json
{
  "code": "OK",
  "message": "Upload succeeded",
  "data": {
    "id": "uuid",
    "fileName": "sample.txt",
    "fileSizeBytes": 10240,
    "documentStatus": "UPLOADED",
    "createdAt": "2026-03-12T10:00:00Z"
  }
}
```

### 4.2 文档列表

- Method: `GET`
- Path: `/api/documents`

查询参数：

- `page`
- `size`
- `status`

### 4.3 文档详情

- Method: `GET`
- Path: `/api/documents/{documentId}`

### 4.4 启动索引

- Method: `POST`
- Path: `/api/documents/{documentId}/index`

响应体：

```json
{
  "code": "OK",
  "message": "Indexing started",
  "data": {
    "jobId": "uuid",
    "documentId": "uuid",
    "jobStatus": "RUNNING"
  }
}
```

### 4.5 获取最新索引任务

- Method: `GET`
- Path: `/api/documents/{documentId}/jobs/latest`

### 4.6 文档 chunk 列表

- Method: `GET`
- Path: `/api/documents/{documentId}/chunks`

查询参数：

- `page`
- `size`

## 5. 聊天接口

### 5.1 创建聊天会话

- Method: `POST`
- Path: `/api/chat/sessions`

请求体：

```json
{
  "title": "Character Relation Analysis"
}
```

### 5.2 聊天会话列表

- Method: `GET`
- Path: `/api/chat/sessions`

### 5.3 获取聊天消息

- Method: `GET`
- Path: `/api/chat/sessions/{sessionId}/messages`

查询参数：

- `page`
- `size`

### 5.4 流式查询答案

- Method: `POST`
- Path: `/api/chat/sessions/{sessionId}/query/stream`
- Response content type: `text/event-stream`

请求体：

```json
{
  "query": "What is the relationship between the two main characters?",
  "documentIds": [
    "uuid-1",
    "uuid-2"
  ]
}
```

SSE 事件类型：

- `token`
- `complete`
- `error`

`token` 事件载荷：

```json
{
  "traceId": "uuid",
  "content": "partial token text"
}
```

`complete` 事件载荷：

```json
{
  "traceId": "uuid",
  "messageId": "uuid",
  "citationCount": 3,
  "finishedAt": "2026-03-12T10:03:00Z"
}
```

### 5.5 提交答案反馈

- Method: `POST`
- Path: `/api/chat/traces/{traceId}/feedback`

请求体：

```json
{
  "feedbackType": "HELPFUL",
  "feedbackText": "The citations were accurate."
}
```

## 6. Trace 接口

### 6.1 获取 Trace 详情

- Method: `GET`
- Path: `/api/chat/traces/{traceId}`

响应体：

```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "traceId": "uuid",
    "userQuery": "question text",
    "rewrittenQuery": "rewritten question text",
    "answerText": "final answer with citations",
    "latencyMs": 2140,
    "evidenceItems": [
      {
        "citationNo": 1,
        "documentId": "uuid",
        "documentName": "sample.txt",
        "chunkId": "uuid",
        "chunkIndex": 12,
        "sectionLabel": "Chapter 3",
        "retrievalScore": 0.812341,
        "rerankScore": 0.954231,
        "contentText": "evidence content"
      }
    ]
  }
}
```

## 7. 设置接口

### 7.1 获取用户设置

- Method: `GET`
- Path: `/api/settings`

### 7.2 更新用户设置

- Method: `PUT`
- Path: `/api/settings`

请求体：

```json
{
  "embeddingModel": "embedding-model-id",
  "rerankModel": "rerank-model-id",
  "chatModel": "chat-model-id",
  "chunkSize": 500,
  "chunkOverlap": 100,
  "topK": 10,
  "topN": 5
}
```

## 8. 健康检查接口

### 8.1 Health Check

- Method: `GET`
- Path: `/api/health`

响应体：

```json
{
  "code": "OK",
  "message": "Healthy",
  "data": {
    "status": "UP",
    "database": "UP",
    "redis": "UP"
  }
}
```

## 9. 错误响应格式

所有错误响应必须使用以下结构：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Document ID is required",
  "data": null
}
```
