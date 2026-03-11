# Novel RAG — 小说人物关系分析系统

基于 **Spring Boot + Vue 3** 的 RAG（检索增强生成）系统，专注于分析长篇小说中的人物关系。

## 快速开始

```bash
# 后端（自动下载 Maven，无需手动安装）
cd backend
./mvnw.cmd spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`

## 技术栈与版本

| 技术 | 版本 | 管理方式 |
|------|------|---------|
| JDK | 17 | `.java-version` + `pom.xml` |
| Maven | 3.9.13 | Maven Wrapper (`mvnw.cmd`) |
| Spring Boot | 3.2.5 | `pom.xml` parent BOM |
| Node.js | ≥ 24 | `.nvmrc` + `package.json engines` |
| Vue 3 | latest | `package-lock.json` |
| SiliconFlow API | v1 | `application.yml` |

## 文档

- [版本管理 SOP](docs/版本管理SOP.md) — 版本管理标准操作规程
- [测试 SOP](docs/测试SOP.md) — 环境搭建与功能测试流程
