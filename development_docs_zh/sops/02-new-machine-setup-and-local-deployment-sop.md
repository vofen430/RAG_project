# 新机器环境配置与本地部署 SOP

## 1. 目的

本 SOP 定义新本地机器的标准初始化步骤，以及 RAG 项目的本地部署流程。

## 2. 所需软件

在新机器上安装以下软件：

- Git
- Docker Engine
- Docker Compose v2
- Java 17
- Node.js 24
- npm 11

## 3. 所需本地服务

新机器必须运行：

- PostgreSQL 16 并启用 pgvector
- Redis 7

## 4. 项目拉取 SOP

步骤：

1. 创建本地 workspace 目录。
2. 将项目仓库克隆到 workspace 目录。
3. 切换到要求的分支。
4. 在开始部署前确认仓库为 clean 状态。

## 5. 本地目录 SOP

在本地机器上创建以下目录：

- 项目 workspace 目录
- 本地文档存储根目录
- 本地日志目录
- 本地临时目录

## 6. 环境文件 SOP

准备本地环境配置，至少包含以下值：

- 后端端口
- PostgreSQL host
- PostgreSQL port
- PostgreSQL database name
- PostgreSQL username
- PostgreSQL password
- Redis host
- Redis port
- JWT secret
- 本地存储根目录
- 模型供应商 API key
- 前端 API base URL

## 7. PostgreSQL 初始化 SOP

步骤：

1. 在本地启动 PostgreSQL。
2. 创建 `rag_local` 数据库。
3. 创建应用数据库用户。
4. 赋予所需数据库权限。
5. 在 `rag_local` 中启用 `vector` 扩展。

## 8. Redis 初始化 SOP

步骤：

1. 在本地启动 Redis。
2. 验证 Redis 在配置端口上可访问。

## 9. 后端配置 SOP

步骤：

1. 进入 `backend` 目录。
2. 确认 Java 17 已激活。
3. 确认后端环境变量已配置。
4. 执行 Maven 依赖解析。
5. 启动后端应用。
6. 验证数据库 migration 已成功完成。
7. 验证后端 health 接口返回成功。

## 10. 前端配置 SOP

步骤：

1. 进入 `frontend` 目录。
2. 确认 Node.js 24 和 npm 11 已激活。
3. 安装前端依赖。
4. 配置前端本地环境。
5. 启动前端。
6. 打开本地前端地址。

## 11. 本地部署验证 SOP

启动后，验证：

1. 登录页可以加载。
2. 用户登录成功。
3. 文档上传成功。
4. 索引可成功启动。
5. 索引可成功完成。
6. 聊天流式输出可用。
7. trace 详情可以打开。
8. citation 证据可以打开。

## 12. 关闭 SOP

按以下顺序停止本地服务：

1. 前端进程
2. 后端进程
3. Redis
4. PostgreSQL

## 13. 交接记录 SOP

部署完成后记录以下信息：

- 机器名称
- 部署日期
- 分支名
- 后端启动结果
- 前端启动结果
- PostgreSQL 检查结果
- Redis 检查结果
- 功能验证结果
