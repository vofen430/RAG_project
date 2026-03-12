# RAG 方案设计文档

## 1. 目标

RAG 方案必须支持：

- 文档上传
- 本地索引
- 检索增强答案生成
- 基于 citation 的可解释性
- 持久化 query trace 查看

## 2. 支持的文档范围

首期交付范围支持基于文本的文档接入，使用本地文件存储和持久化索引记录。

## 3. 流水线定义

RAG 流水线包含以下阶段：

1. 文件接入
2. 文本提取
3. 文本规范化
4. chunk 生成
5. 元数据提取
6. embedding 生成
7. 向量持久化
8. 相似度检索
9. reranking
10. prompt 组装
11. 流式答案生成
12. citation 持久化
13. trace 持久化

## 4. 接入设计

### 4.1 文件接入

后端接收上传文件后执行：

- 文件大小校验
- 文件类型校验
- 编码校验
- 所有权绑定
- 本地路径分配

### 4.2 文本提取

后端提取纯文本并保存：

- 原始文本
- 规范化文本
- 源文件路径
- 文件 hash

## 5. Chunking 设计

chunking 阶段必须生成稳定且可持久化的 chunks。

chunk 生成规则：

- 使用按段落感知的切分
- 存在章节或分段时优先保留边界
- 在相邻 chunks 之间保留 overlap
- 分配稳定的 chunk index
- 持久化 chunk 的 start offset 和 end offset
- 持久化简短 chunk summary 供查看使用

## 6. 元数据提取

每个 chunk 必须带有检索和可解释性所需的元数据字段。

必需元数据：

- Document ID
- Chunk index
- Section label
- Start offset
- End offset
- Entity list
- Language code
- Hash value

## 7. Embedding 设计

embedding 生成规则：

- 对每个规范化后的 chunk 生成一条 embedding
- 每个 chunk 存储一个 vector
- 对当前激活模型基线保持固定的 embedding 维度
- 持久化索引时使用的 embedding model 标识

## 8. Retrieval 设计

retrieval 阶段使用 PostgreSQL 中的 pgvector 实现。

retrieval 规则：

- 对传入用户问题生成一条 query embedding
- 在用户已授权的文档范围内执行 top-k 相似度搜索
- 过滤 inactive 或 failed 的文档记录
- 同时返回 chunk 文本和 chunk 元数据
- 持久化原始 retrieval rank 和 similarity score

## 9. Reranking 设计

reranking 阶段接收已召回 chunks，并输出更小的有序证据集合。

reranking 规则：

- 输入使用用户 query 和已召回 chunk 文本
- 输出排序后的证据列表
- 持久化 rerank score 和 rerank position
- 最终证据集大小由配置固定

## 10. Prompt 组装设计

prompt 组装阶段必须构建受控的答案上下文。

prompt 规则：

- 包含用户 query
- 仅包含最终证据集
- 为每个证据项包含 citation 编号
- 包含 section 和 source location 元数据
- 要求答案仅基于已引用证据
- 要求在证据不足时明确说明

## 11. 答案生成设计

答案生成阶段必须将最终答案流式输出到前端，并持久化完整输出。

输出规则：

- 将 token 内容流式传给前端
- 持久化最终答案内容
- 持久化答案完成时间
- 持久化答案状态

## 12. 可解释性设计

可解释性是必选能力，不是可选附加项。

必须输出的可解释性内容：

- 答案中的 citation 标记
- 该答案的证据列表
- 每个 citation 的证据文本
- 每个 citation 的文档名
- 每个 citation 的 section label
- 每个 citation 的 chunk index
- 每个 citation 的 similarity score
- 每个 citation 的 rerank score

## 13. Trace 持久化设计

每次 query 都必须生成一条 trace 记录。

trace 记录必须包含：

- Query text
- Query rewrite text（如适用）
- Retrieval candidate set
- Retrieval scores
- 最终 reranked evidence set
- Prompt version
- Answer record ID
- Total latency
- 请求失败时的 error details

## 14. 聊天会话设计

聊天层必须持久化：

- Chat session 元数据
- User messages
- Assistant messages
- Message order
- 每条 assistant answer 关联的 trace ID

## 15. 失败处理

流水线必须对以下失败状态进行持久化：

- 上传失败
- 提取失败
- chunking 失败
- embedding 失败
- retrieval 失败
- reranking 失败
- answer generation 失败

## 16. 本地运行约束

本方案仅面向本地部署。

本地运行要求：

- 所有服务运行在同一台本地机器
- 所有源文件保留在本地磁盘
- PostgreSQL 和 Redis 本地运行
- 应用日志保留在本地磁盘
