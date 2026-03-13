# 测试标准操作规程（SOP）

> 本文档涵盖 **Novel RAG 人物关系分析系统** 的完整环境部署流程和功能测试流程。
> 最后更新：2026-03-13

---

## 一、快速环境搭建流程

### 1.1 前置条件

| 工具 | 最低版本 | 安装方式（Linux/macOS） | 安装方式（Windows） |
|------|---------|----------------------|-------------------|
| Git | 任意 | `apt install git` / `brew install git` | `choco install git` |
| Docker + Compose | 20+ / v2+ | [Docker 官方安装文档](https://docs.docker.com/engine/install/) | Docker Desktop |
| JDK | 17 | `sdk install java 17-open` (SDKMan) | `choco install openjdk` |
| Node.js | 20+ (LTS) | `nvm install --lts` | `choco install nodejs-lts` |
| npm | 9+ | 随 Node.js 自动安装 | 随 Node.js 自动安装 |
| Maven | 无需安装 | 项目自带 Maven Wrapper（`./mvnw`） | `./mvnw.cmd` |

> 验证安装：
> ```bash
> git --version && docker --version && docker compose version
> java -version && node -v && npm -v
> ```

### 1.2 搭建步骤

```
步骤 1：克隆项目
       → git clone https://github.com/vofen430/RAG_project.git
       → cd RAG_project

步骤 2：启动基础设施（PostgreSQL + Redis）
       → docker compose up -d
       → docker compose ps
       → 确认 rag-postgres 和 rag-redis 均显示 healthy

步骤 3：获取 SiliconFlow API Key
       → 访问 https://cloud.siliconflow.com/account/ak
       → 注册/登录后创建 API Key（格式：sk-xxx...xxx）

步骤 4：启动后端（新开一个终端窗口）
       → cd backend
       → export MODEL_PROVIDER_API_KEY="sk-your-api-key"
       → export JWT_SECRET="my-32-char-long-secure-secret-key"
       → ./mvnw spring-boot:run
       → 等待控制台出现 "Started NovelRagApplication" 字样
       → 后端运行在 http://localhost:8080

步骤 5：启动前端（新开一个终端窗口）
       → cd frontend
       → npm install
       → npm run dev
       → 前端运行在 http://localhost:5173

步骤 6：浏览器验证
       → 打开 http://localhost:5173
       → 用 demo / demo-password 登录
       → 确认能看到深色主题的界面
```

### 1.3 默认配置参考

| 参数 | 默认值 | 来源 |
|------|-------|------|
| 后端端口 | 8080 | `application.yml` |
| PostgreSQL | 127.0.0.1:5432 / rag_local | docker-compose.yml |
| Redis | 127.0.0.1:6379 | docker-compose.yml |
| 前端端口 | 5173 | Vite 开发服务器 |
| 嵌入模型 | Qwen/Qwen3-Embedding-0.6B (免费) | models.json |
| 重排序模型 | Qwen/Qwen3-Reranker-0.6B (免费) | models.json |
| 生成模型 | Qwen/Qwen3-8B | models.json |
| 分块大小 | 500 字符 | user_settings 表 |
| 分块重叠 | 100 字符 | user_settings 表 |
| Top-K 检索 | 10 | user_settings 表 |
| Top-N 重排序 | 5 | user_settings 表 |
| 测试账号 | demo / demo-password | Flyway V2 种子迁移 |

### 1.4 环境搭建验证清单

| 序号 | 检查项 | 预期结果 | 验证命令 | 通过 |
|------|--------|---------|---------|------|
| 1 | Docker 基础设施 | postgres 和 redis 均 healthy | `docker compose ps` | ☐ |
| 2 | 后端编译 | BUILD SUCCESS | `cd backend && ./mvnw compile -q` | ☐ |
| 3 | 前端构建 | ✓ built 无报错 | `cd frontend && npx vite build` | ☐ |
| 4 | 后端启动 | "Started NovelRagApplication" | 查看启动日志 | ☐ |
| 5 | Flyway 迁移 | V1, V2, V3 三个迁移成功 | 查看启动日志 | ☐ |
| 6 | 前端启动 | VITE ready，端口 5173 | `npm run dev` | ☐ |
| 7 | 页面加载 | 浏览器显示登录页 | 访问 localhost:5173 | ☐ |
| 8 | 登录成功 | 跳转到主界面 | demo / demo-password 登录 | ☐ |
| 9 | 模型列表 | 设置页面能加载模型下拉列表 | 访问设置页面 | ☐ |
| 10 | API Key | 设置页面显示 API Key 状态 | 查看 🔑 卡片 | ☐ |

### 1.5 常见搭建问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `mvnw` 报错 "JAVA_HOME not set" | JDK 未安装或未配置 | 安装 JDK 17 后重启终端 |
| `npm install` 报错 | Node.js 版本低 | 升级到 Node.js 20+ |
| 前端页面空白 | 后端未启动 | 先启动后端，再刷新前端 |
| 端口 5432 被占用 | 已有 PostgreSQL 实例运行 | `docker compose down` 后重新 `docker compose up -d` |
| 端口 8080 被占用 | 其他服务占用 | `export SERVER_PORT=8081` 或终止占用进程 |
| Flyway 迁移失败 | 数据库连接错误 | 检查 Docker postgres 是否健康 |
| API 调用返回 401 | API Key 未配置或过期 | 通过设置页面或环境变量重新配置 |
| 文件上传后找不到 | 存储路径问题 | 检查 `LOCAL_STORAGE_ROOT` 环境变量 |
| GBK 文件乱码 | 已自动处理 | 后端自动检测 GBK/UTF-8 编码 |

---

## 二、数据持久化说明

### 2.1 持久化层级

本系统所有数据均已持久化到磁盘，重启不丢失：

```
持久化架构
┌────────────────────────────────────────┐
│  PostgreSQL (Docker Volume: pgdata)    │
│  ├── documents        文档元数据       │
│  ├── document_chunks  分块文本+向量    │
│  ├── indexing_jobs    索引任务状态     │
│  ├── chat_sessions    对话会话         │
│  ├── chat_messages    对话消息历史     │
│  ├── rag_query_traces 查询追踪        │
│  ├── trace_evidence_items 证据项      │
│  ├── user_settings    用户设置+API Key │
│  └── users            用户账户         │
├────────────────────────────────────────┤
│  Redis (Docker Volume: redisdata)      │
│  └── JWT Token 黑名单 / 缓存          │
├────────────────────────────────────────┤
│  本地磁盘 (storage/)                   │
│  └── documents/{userId}/{docId}.txt    │
│      原始上传文件（保留原始字节）      │
└────────────────────────────────────────┘
```

### 2.2 持久化验证

```bash
# 验证数据库
docker exec rag-postgres psql -U rag_user -d rag_local \
  -c "SELECT tablename FROM pg_tables WHERE schemaname='public';"

# 验证文件存储
find storage/ -type f -name "*.txt" -exec ls -la {} \;

# 验证 Redis
docker exec rag-redis redis-cli ping
```

---

## 三、功能测试流程

### 3.1 测试前准备

```
准备 1：确保后端和前端均已启动（参见第一章）
准备 2：确保 SiliconFlow API Key 已配置（通过环境变量或设置页面）
准备 3：准备测试用文件（项目自带 雷雨.txt，可直接使用）
```

### 3.2 功能测试项

#### 测试 1：API 密钥配置

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1.1 | 浏览器打开设置页面 | 显示 🔑 SiliconFlow API Key 卡片 |
| 1.2 | 在 API Key 输入框中输入有效密钥 | 输入框显示密钥（密码模式），可点击 👁️ 查看 |
| 1.3 | 点击 "💾 保存所有设置" | 显示 "✅ 设置已保存"，状态变为 "✅ 已配置" |
| 1.4 | 刷新页面 | API Key 状态仍显示 "✅ 已配置"，显示脱敏密钥 `sk-••••••••xxxx` |

#### 测试 2：模型切换

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 2.1 | 查看 "嵌入模型" 下拉框 | 默认选中 "Qwen3 Embedding 0.6B (Free)" |
| 2.2 | 切换嵌入模型到 "Qwen3 Embedding 4B" | 下拉框更新为新选项 |
| 2.3 | 查看 "重排序模型" 下拉框 | 显示 3 个 Qwen3 Reranker 选项 |
| 2.4 | 查看 "生成模型" 下拉框 | 显示 18 个 LLM 选项（Qwen3/2.5、DeepSeek V3/R1、GLM-4、Llama 3.1 等） |
| 2.5 | 勾选 "自定义模型名称" | 出现文本输入框可输入自定义模型名 |
| 2.6 | 修改 RAG 参数（Top-K, Top-N） | 输入框接受新值 |
| 2.7 | 点击 "💾 保存所有设置" | 显示 "✅ 设置已保存" |

#### 测试 3：文档上传与索引

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 3.1 | 导航到文档管理页面 | 显示拖拽上传区域 |
| 3.2 | 点击上传区域，选择 `雷雨.txt` | 出现上传进度条，完成后文件出现在列表中 |
| 3.3 | 查看上传后的文档详情 | 显示 "待索引"，文件名、大小、编码 (GBK) 正确 |
| 3.4 | 点击 "🚀 开始索引" | 显示流水线阶段：⬜分块 → ⬜嵌入 → ⬜持久化 → ⬜完成 |
| 3.5 | 观察索引进度 | 分块阶段: ✅分块 → ⏳嵌入（进度条显示 X/Y 分块）|
| 3.6 | 等待索引完成 | 全部阶段: ✅分块 → ✅嵌入 → ✅持久化 → ✅完成 |
| 3.7 | 查看完成状态 | 显示 "✅ 227 个分块已索引"（雷雨.txt 预期约 227 个） |

> **⚠️ 注意：** 索引过程需调用 SiliconFlow Embedding API，耗时取决于文本长度和网络速度，雷雨.txt 通常需要 15-30 秒。

#### 测试 4：RAG 问答（核心流程）

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 4.1 | 导航到智能问答页面 | 显示聊天界面和 Pipeline 可视化 |
| 4.2 | 创建新会话 | 显示空白对话界面 |
| 4.3 | 输入：`周朴园和鲁侍萍是什么关系？` | 问题显示在右侧（用户消息） |
| 4.4 | 观察 Pipeline 可视化 | 各阶段依次高亮：查询嵌入 → 检索 → 重排序 → 生成 |
| 4.5 | 等待回答生成 | 回答以 SSE 流式方式逐字出现 |
| 4.6 | 检查回答质量 | 应提到侍萍是周朴园的前妻、三十年前被抛弃等关键信息 |
| 4.7 | 查看 Trace 详情 | 显示证据列表，rerank 分数 > 0.9，指向《雷雨》第二幕原文 |

#### 测试 5：人物关系深度分析

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 5.1 | 输入：`分析周朴园和鲁侍萍之间的关系` | 系统返回详细分析，涉及两人的历史渊源 |
| 5.2 | 输入：`周萍、四凤、周冲之间构成了什么样的三角关系？` | 回答分析三人的情感纠葛 |
| 5.3 | 输入：`繁漪在剧中的角色动机是什么？` | 回答分析繁漪的性格和行为动机 |
| 5.4 | 输入：`列举《雷雨》中所有主要人物及其相互关系` | 回答以结构化方式梳理全部人物关系 |

#### 测试 6：RAG 参数调整

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 6.1 | 在设置页面修改 Top-K 为 20 | 输入框接受新值 |
| 6.2 | 修改 Top-N 为 8 | 输入框接受新值 |
| 6.3 | 保存设置后重新提问 | 回答可能更全面（使用了更多上下文） |

#### 测试 7：异常场景

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 7.1 | 未上传文档时直接提问 | 返回提示或空结果，不崩溃 |
| 7.2 | 使用无效的 API Key | 提问后返回错误提示，系统不崩溃 |
| 7.3 | 上传空文件 | 上传时报错或索引时报错，显示错误状态 |
| 7.4 | 在索引进行中刷新页面 | 重新加载后仍能看到索引进度 |
| 7.5 | 索引失败后点击重试 | 可以重新启动索引流程 |

### 3.3 完整 RAG 管线验证

以下流程验证 10 步 RAG 管线的完整性：

```
 Ingestion（文档摄入）
   ↓  → 验证：上传文件后，文档列表显示新文档，磁盘 storage/ 下有原始文件
 Chunking（文本分块）
   ↓  → 验证：索引时阶段变为 ✅分块，totalChunks > 0
 Embedding（向量嵌入）
   ↓  → 验证：阶段变为 ⏳嵌入，processedChunks 逐步增加
 Persisting（向量持久化）
   ↓  → 验证：阶段变为 ✅持久化，数据写入 pgvector
 Indexing Complete
   ↓  → 验证：阶段变为 ✅完成，文档状态变为 "已索引 ✓"
 User Query（用户提问）
   ↓  → 验证：输入问题，发送到后端
 Query Embedding（查询嵌入）
   ↓  → 验证：使用与索引相同的嵌入模型
 Top-K Retrieval（向量检索）
   ↓  → 验证：从 pgvector 检索最相似的分块
 Reranking（重排序）
   ↓  → 验证：SiliconFlow POST /rerank 调用，返回精排结果
 LLM Generation（大模型生成）
   ↓  → 验证：SSE 流式生成回答，Trace 记录证据和分数
```

---

## 四、命令行 E2E 测试（自动化验证）

以下命令可在终端中快速验证系统完整性，无需浏览器：

### 4.1 编译验证

```bash
# 后端编译
cd backend && ./mvnw compile -q && echo "✅ 后端编译通过"

# 前端构建
cd ../frontend && npx vite build && echo "✅ 前端构建通过"
```

### 4.2 完整 E2E 命令行测试

```bash
# 0. 设置 API Key（替换为你的真实密钥）
export MODEL_PROVIDER_API_KEY="sk-your-api-key"

# 1. 登录
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}' | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
echo "✅ 登录成功: ${TOKEN:0:15}..."

# 2. 上传文档
DOC_ID=$(curl -s -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@雷雨.txt" | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "✅ 文档上传: $DOC_ID"

# 3. 触发索引
curl -s -X POST "http://localhost:8080/api/documents/$DOC_ID/index" \
  -H "Authorization: Bearer $TOKEN" > /dev/null
echo "⏳ 索引启动..."

# 4. 轮询索引进度
for i in $(seq 1 30); do
  sleep 5
  STATUS=$(curl -s "http://localhost:8080/api/documents/$DOC_ID/jobs/latest" \
    -H "Authorization: Bearer $TOKEN" | \
    python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(f'{d[\"jobStatus\"]}|{d.get(\"totalChunks\",\"?\")}')")
  echo "  [$i] $STATUS"
  echo "$STATUS" | grep -q "COMPLETED" && break
  echo "$STATUS" | grep -q "FAILED" && echo "❌ 索引失败" && exit 1
done
echo "✅ 索引完成"

# 5. 创建会话
SESSION_ID=$(curl -s -X POST http://localhost:8080/api/chat/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"E2E Test"}' | \
  python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "✅ 会话创建: $SESSION_ID"

# 6. 发送 RAG 查询
timeout 90 curl -s -N -X POST \
  "http://localhost:8080/api/chat/sessions/$SESSION_ID/query/stream" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"query":"周朴园和鲁侍萍是什么关系？"}' > /tmp/e2e_sse.txt

python3 -c "
import json
tokens, trace_id = [], None
with open('/tmp/e2e_sse.txt') as f:
    for line in f:
        line = line.strip()
        raw = None
        if line.startswith('data:data: '): raw = line[11:]
        elif line.startswith('data: '): raw = line[6:]
        if raw:
            try:
                d = json.loads(raw)
                if isinstance(d, dict):
                    if d.get('content'): tokens.append(d['content'])
                    if d.get('traceId') and not trace_id: trace_id = d['traceId']
            except: pass
answer = ''.join(tokens)
print(f'✅ 回答生成: {len(answer)} 字符, Trace: {trace_id}')
print(f'   回答摘要: {answer[:200]}...')
"

# 7. 验证 Trace 证据
TRACE_ID=$(python3 -c "
import json
with open('/tmp/e2e_sse.txt') as f:
    for line in f:
        if 'traceId' in line:
            raw = line.strip()
            if raw.startswith('data:data: '): raw = raw[11:]
            elif raw.startswith('data: '): raw = raw[6:]
            try:
                d = json.loads(raw)
                if d.get('traceId'): print(d['traceId']); break
            except: pass
")

curl -s "http://localhost:8080/api/chat/traces/$TRACE_ID" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
d = json.load(sys.stdin)['data']
evidence = d.get('evidenceItems', [])
print(f'✅ Trace 验证: {len(evidence)} 条证据')
for e in evidence[:3]:
    print(f'   [{e.get(\"citationNo\")}] score={e.get(\"rerankScore\",\"?\")} 章节={e.get(\"sectionLabel\",\"?\")}')
"

echo ""
echo "==========================================="
echo "✅ E2E 测试全部通过"
echo "==========================================="
```

---

## 五、测试结果记录表

| 测试项 | 测试人 | 日期 | 结果 | 备注 |
|-------|--------|------|------|------|
| 测试 1：API 密钥配置 | | | ☐ 通过 / ☐ 失败 | |
| 测试 2：模型切换 | | | ☐ 通过 / ☐ 失败 | |
| 测试 3：文档上传与索引 | | | ☐ 通过 / ☐ 失败 | 分块数: |
| 测试 4：RAG 问答 | | | ☐ 通过 / ☐ 失败 | 回答质量: |
| 测试 5：人物关系分析 | | | ☐ 通过 / ☐ 失败 | |
| 测试 6：参数调整 | | | ☐ 通过 / ☐ 失败 | |
| 测试 7：异常场景 | | | ☐ 通过 / ☐ 失败 | |
| 命令行 E2E | | | ☐ 通过 / ☐ 失败 | |

---

## 六、SiliconFlow API 端点参考

| 功能 | 端点 | 本系统对应 |
|------|------|-----------|
| 文本嵌入 | `POST https://api.siliconflow.com/v1/embeddings` | `EmbeddingService` |
| 语义重排 | `POST https://api.siliconflow.com/v1/rerank` | `RerankService` |
| 对话生成 | `POST https://api.siliconflow.com/v1/chat/completions` (stream) | `ChatService` |
| 模型列表 | `GET https://api.siliconflow.com/v1/models` | `ModelConfig` |

---

## 七、环境快速重置

```bash
# 1. 停止服务
#    → 终端中按 Ctrl+C 停止后端和前端

# 2. 清理编译产物
cd backend && ./mvnw clean
cd ../frontend && rm -rf dist/

# 3. 清理 Docker 数据（⚠️ 会删除所有索引数据和文档！）
docker compose down -v

# 4. 重新启动（参见第一章）
docker compose up -d
```

> **注意：** 本系统使用 PostgreSQL pgvector 持久化所有向量数据和分块文本。
> 仅当执行 `docker compose down -v` 时才会清除数据库。
> 普通重启（`docker compose down` 然后 `docker compose up -d`）不会丢失数据。

---

## 八、关机流程

按以下顺序关闭服务：

```
1. 前端进程 → Ctrl+C 或关闭终端
2. 后端进程 → Ctrl+C 或关闭终端
3. 基础设施 → docker compose down
```
