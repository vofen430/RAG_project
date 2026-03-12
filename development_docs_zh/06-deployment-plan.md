# 本地部署方案文档

## 1. 部署范围

本项目仅部署在本地机器上。

部署内容包括：

- 前端应用
- 后端应用
- PostgreSQL
- Redis
- 本地存储目录

## 2. 运行端口

保留的本地端口：

- Frontend: `5173`
- Backend: `8080`
- PostgreSQL: `5432`
- Redis: `6379`

## 3. 所需本地目录

本地机器必须提供以下目录：

- 项目根目录
- 本地文档存储目录
- 本地应用日志目录
- 当容器卷映射需要时的本地 PostgreSQL 数据目录
- 当容器卷映射需要时的本地 Redis 数据目录

## 4. 所需环境变量

后端环境变量：

- `SPRING_PROFILES_ACTIVE=local`
- `SERVER_PORT=8080`
- `DB_HOST=127.0.0.1`
- `DB_PORT=5432`
- `DB_NAME=rag_local`
- `DB_USERNAME=rag_user`
- `DB_PASSWORD=rag_password`
- `REDIS_HOST=127.0.0.1`
- `REDIS_PORT=6379`
- `JWT_SECRET=replace-with-local-secret`
- `LOCAL_STORAGE_ROOT=/absolute/path/to/storage`
- `MODEL_PROVIDER_API_KEY=replace-with-local-key`

前端环境变量：

- `VITE_API_BASE_URL=http://127.0.0.1:8080/api`

## 5. 本地服务布局

### 5.1 后端进程

后端以一个 Spring Boot 进程运行，并连接：

- 本机 PostgreSQL
- 本机 Redis
- 本地文件系统中的本地存储路径

### 5.2 前端进程

前端在开发阶段以一个 Vite 进程运行，在本地发布验证阶段以一个本地静态进程运行。

### 5.3 数据库进程

PostgreSQL 本地运行，并启用 pgvector。

### 5.4 缓存进程

Redis 作为单实例本地运行。

## 6. 本地数据目录布局

本地存储根目录应包含：

- `/documents`
- `/tmp`
- `/exports`
- `/logs`

## 7. 启动顺序

要求的启动顺序如下：

1. PostgreSQL
2. Redis
3. Backend
4. Frontend

## 8. 迁移顺序

要求的迁移顺序如下：

1. 启动 PostgreSQL
2. 启用 pgvector
3. 启动后端
4. 后端启动时自动执行 Flyway migration

## 9. 运行检查

本地部署完成后，检查：

- PostgreSQL 可连接
- Redis 可连接
- 后端健康检查接口可访问
- 前端首页可访问
- 登录可用
- 文档上传可用
- 索引可用
- 聊天流式输出可用
- trace 详情加载可用

## 10. 本地备份范围

本地备份范围包括：

- PostgreSQL 数据
- 本地源文档
- 排障时需要的本地日志目录

## 11. 本地停止顺序

要求的停止顺序如下：

1. Frontend
2. Backend
3. Redis
4. PostgreSQL
