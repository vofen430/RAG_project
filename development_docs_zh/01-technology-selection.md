# 技术选型说明书

## 1. 项目范围

本项目是一个本地部署的 RAG 应用，用于实现文档上传、索引、检索、可解释回答生成和聊天交互。

本目录中的技术栈为本项目交付基线。

## 2. 后端技术

- Java 17
- Spring Boot 3.2.x
- Spring Web
- Spring WebFlux
- Spring Security
- Spring Validation
- MyBatis-Plus
- Flyway

## 3. 前端技术

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios
- Fetch with Server-Sent Events
- Marked 用于 Markdown 渲染

## 4. 数据与存储技术

- PostgreSQL 16
- pgvector
- Redis 7
- 本地文件存储用于源文档

## 5. RAG 与应用集成技术

- 外部 embedding 服务集成
- 外部 reranking 服务集成
- 外部 chat completion 服务集成
- 结构化 prompt 模板
- 检索 trace 持久化
- 基于 citation 的答案渲染

模型供应商侧 API 细节不在本说明书范围内。实现团队应基于供应商 API manual 完成请求与响应处理。

## 6. 部署与运行技术

- Docker Engine
- Docker Compose
- Maven Wrapper
- Node.js 24
- npm 11

## 7. 测试技术

- JUnit 5
- Mockito
- Spring Boot Test
- Vitest
- Playwright

## 8. 可观测性与运维

- Spring Boot Actuator
- SLF4J
- Logback

## 9. 标准版本基线

- Java 17
- Spring Boot 3.2.5
- Node.js 24
- npm 11
- PostgreSQL 16
- Redis 7
- Docker Compose v2
