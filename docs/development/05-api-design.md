# API Design Document

## 1. Scope

This document defines application-facing APIs only.

Provider-facing model APIs are not documented here. The implementation team should use the official model API manual for provider integration details.

## 2. General Conventions

Base path:

- `/api`

Authentication:

- Bearer token in the `Authorization` header

Content types:

- `application/json`
- `multipart/form-data`
- `text/event-stream`

Common response fields:

- `code`
- `message`
- `data`

## 3. Authentication APIs

### 3.1 Login

- Method: `POST`
- Path: `/api/auth/login`

Request body:

```json
{
  "username": "demo",
  "password": "demo-password"
}
```

Response body:

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

### 3.2 Current User

- Method: `GET`
- Path: `/api/auth/me`

## 4. Document APIs

### 4.1 Upload Document

- Method: `POST`
- Path: `/api/documents`

Request:

- Multipart field: `file`

Response body:

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

### 4.2 List Documents

- Method: `GET`
- Path: `/api/documents`

Query parameters:

- `page`
- `size`
- `status`

### 4.3 Get Document Detail

- Method: `GET`
- Path: `/api/documents/{documentId}`

### 4.4 Start Indexing

- Method: `POST`
- Path: `/api/documents/{documentId}/index`

Response body:

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

### 4.5 Get Latest Indexing Job

- Method: `GET`
- Path: `/api/documents/{documentId}/jobs/latest`

### 4.6 List Document Chunks

- Method: `GET`
- Path: `/api/documents/{documentId}/chunks`

Query parameters:

- `page`
- `size`

## 5. Chat APIs

### 5.1 Create Chat Session

- Method: `POST`
- Path: `/api/chat/sessions`

Request body:

```json
{
  "title": "Character Relation Analysis"
}
```

### 5.2 List Chat Sessions

- Method: `GET`
- Path: `/api/chat/sessions`

### 5.3 Get Chat Messages

- Method: `GET`
- Path: `/api/chat/sessions/{sessionId}/messages`

Query parameters:

- `page`
- `size`

### 5.4 Stream Query Answer

- Method: `POST`
- Path: `/api/chat/sessions/{sessionId}/query/stream`
- Response content type: `text/event-stream`

Request body:

```json
{
  "query": "What is the relationship between the two main characters?",
  "documentIds": [
    "uuid-1",
    "uuid-2"
  ]
}
```

SSE event types:

- `token`
- `complete`
- `error`

`token` event payload:

```json
{
  "traceId": "uuid",
  "content": "partial token text"
}
```

`complete` event payload:

```json
{
  "traceId": "uuid",
  "messageId": "uuid",
  "citationCount": 3,
  "finishedAt": "2026-03-12T10:03:00Z"
}
```

### 5.5 Submit Answer Feedback

- Method: `POST`
- Path: `/api/chat/traces/{traceId}/feedback`

Request body:

```json
{
  "feedbackType": "HELPFUL",
  "feedbackText": "The citations were accurate."
}
```

## 6. Trace APIs

### 6.1 Get Trace Detail

- Method: `GET`
- Path: `/api/chat/traces/{traceId}`

Response body:

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

## 7. Settings APIs

### 7.1 Get User Settings

- Method: `GET`
- Path: `/api/settings`

### 7.2 Update User Settings

- Method: `PUT`
- Path: `/api/settings`

Request body:

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

## 8. Health API

### 8.1 Health Check

- Method: `GET`
- Path: `/api/health`

Response body:

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

## 9. Error Response Format

All error responses must use the following structure:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Document ID is required",
  "data": null
}
```
