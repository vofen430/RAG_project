# 版本管理标准操作规程（SOP）

> 本文档适用于 **Novel RAG 人物关系分析系统** — 一个 Java（Spring Boot）+ JavaScript（Vue 3）混合技术栈项目。

---

## 一、项目版本管理总览

本项目采用 **三层版本管理体系**，分别管控 **工具链版本**、**依赖版本** 和 **项目自身版本**。

```
                    版本管理体系
    ┌──────────────────────────────────────┐
    │  第1层：工具链版本（运行时环境）      │
    │  JDK · Maven · Node.js · npm        │
    ├──────────────────────────────────────┤
    │  第2层：依赖版本（第三方库）          │
    │  pom.xml + BOM · package-lock.json  │
    ├──────────────────────────────────────┤
    │  第3层：项目版本（本系统发布号）      │
    │  语义化版本 MAJOR.MINOR.PATCH       │
    └──────────────────────────────────────┘
```

---

## 二、版本管理文件清单

| 文件路径 | 管控内容 | 谁应该修改 | 是否提交 Git |
|---------|---------|-----------|-------------|
| `.gitignore` | Git 忽略规则 | 项目初始化时配置 | ✅ 提交 |
| `.java-version` | JDK 版本（供 SDKMan/jenv 读取） | 升级 JDK 时修改 | ✅ 提交 |
| `backend/pom.xml` | Java 依赖 + 项目版本 + JDK 编译目标 | 添加/升级依赖时修改 | ✅ 提交 |
| `backend/.mvn/wrapper/maven-wrapper.properties` | Maven 版本 | 升级 Maven 时修改 | ✅ 提交 |
| `backend/mvnw` / `mvnw.cmd` | Maven Wrapper 启动脚本 | 一般不修改 | ✅ 提交 |
| `frontend/.nvmrc` | Node.js 版本（供 nvm/fnm 读取） | 升级 Node 时修改 | ✅ 提交 |
| `frontend/package.json` | JS 依赖 + 项目版本 + 引擎约束 | 添加/升级依赖时修改 | ✅ 提交 |
| `frontend/package-lock.json` | JS 依赖精确锁定 | **自动生成，禁止手动编辑** | ✅ 提交 |
| `frontend/node_modules/` | 已安装的 JS 包 | 自动生成 | ❌ 不提交 |
| `backend/target/` | Java 编译产物 | 自动生成 | ❌ 不提交 |

---

## 三、当前锁定版本

### 3.1 工具链版本

| 工具 | 锁定版本 | 锁定文件 |
|------|---------|---------|
| **JDK** | 17 | `pom.xml` → `<java.version>17</java.version>` + `.java-version` |
| **Maven** | 3.9.13 | `backend/.mvn/wrapper/maven-wrapper.properties` |
| **Node.js** | ≥ 24.0.0 | `frontend/.nvmrc` + `package.json` → `engines` |
| **npm** | ≥ 11.0.0 | `frontend/package.json` → `engines` |

### 3.2 核心依赖版本

**Java 端（通过 Spring Boot BOM 统一管控）：**

| 依赖 | 版本来源 | 说明 |
|------|---------|------|
| Spring Boot | `3.2.5`（直接声明） | 父 POM，同时管控以下依赖版本 |
| Spring Web | 由 BOM 管理 | 无需显式指定版本号 |
| Spring WebFlux | 由 BOM 管理 | 无需显式指定版本号 |
| Jackson | 由 BOM 管理 | 无需显式指定版本号 |
| Lombok | 由 BOM 管理 | 无需显式指定版本号 |

> **什么是 BOM？** `spring-boot-starter-parent` 是 Spring Boot 提供的"物料清单"（Bill of Materials），它预定义了约 300 个常用依赖的经过兼容性测试的版本号。只要你的 `pom.xml` 继承了这个 parent，就不需要为这些依赖手动指定 `<version>`。

**JavaScript 端：**

| 依赖 | `package.json` 声明 | `package-lock.json` 锁定 | 说明 |
|------|---------------------|-------------------------|------|
| Vue 3 | 由 Vite 模板引入 | 精确锁定 | 核心框架 |
| Vue Router | `^4.6.4` | 精确锁定 | 路由管理 |
| Axios | `^1.13.6` | 精确锁定 | HTTP 客户端 |
| Marked | `^17.0.4` | 精确锁定 | Markdown 渲染 |
| Vite | `^7.3.1`（dev） | 精确锁定 | 构建工具 |

> **Caret (`^`) 语义：** `^4.6.4` 表示"兼容 4.x.x 系列，最低 4.6.4"。在 `npm install` 时，如果 `package-lock.json` 存在，则安装锁定的精确版本；如果执行 `npm update`，则在 `^` 范围内升级到最新版。

### 3.3 项目自身版本

| 模块 | 当前版本 | 声明位置 |
|------|---------|---------|
| 后端（Java） | `1.0.0` | `backend/pom.xml` → `<version>` |
| 前端（JavaScript） | `1.0.0` | `frontend/package.json` → `version` |

> **两端版本号应保持一致**，统一遵循语义化版本规范（SemVer）。

---

## 四、标准操作流程

### 4.1 新成员首次搭建环境

```
步骤 1：安装 JDK 17
       → Windows: choco install openjdk --version=17
       → macOS:   brew install openjdk@17
       → 或使用 SDKMan: sdk install java 17-open

步骤 2：克隆项目
       → git clone <仓库地址>

步骤 3：启动后端（Maven Wrapper 会自动下载正确版本的 Maven）
       → cd backend
       → ./mvnw.cmd spring-boot:run       (Windows)
       → ./mvnw spring-boot:run           (macOS/Linux)

步骤 4：安装 Node.js 24
       → 如果使用 nvm：nvm install （自动读取 .nvmrc）
       → 如果使用 fnm：fnm use    （自动读取 .nvmrc）
       → 或手动安装：choco install nodejs-lts

步骤 5：启动前端
       → cd frontend
       → npm install     （根据 package-lock.json 安装精确版本）
       → npm run dev
```

> **⚠️ 注意：** 禁止使用 `npm install --force` 或删除 `package-lock.json` 后重新安装，这会导致依赖版本漂移。

### 4.2 添加新的 Java 依赖

```
步骤 1：检查该依赖是否已被 Spring Boot BOM 管理
       → 查阅：https://docs.spring.io/spring-boot/appendix/dependency-versions/
       → 如果已被管理，在 pom.xml 中只需添加 groupId 和 artifactId，不加 version

步骤 2：如果未被 BOM 管理，须显式指定版本号
       → 建议在 <properties> 中定义版本变量：
         <my-lib.version>2.1.0</my-lib.version>
       → 在 <dependency> 中引用：
         <version>${my-lib.version}</version>

步骤 3：验证编译
       → ./mvnw.cmd clean compile

步骤 4：提交变更
       → git add pom.xml
       → git commit -m "deps(backend): add xxx dependency v2.1.0"
```

### 4.3 添加新的 JavaScript 依赖

```
步骤 1：使用 npm install 添加（自动更新 package.json 和 package-lock.json）
       → 运行时依赖：npm install <包名>
       → 开发时依赖：npm install <包名> --save-dev

步骤 2：验证构建
       → npm run build

步骤 3：提交两个文件
       → git add package.json package-lock.json
       → git commit -m "deps(frontend): add xxx v1.2.3"
```

> **⚠️ 关键规则：** `package.json` 和 `package-lock.json` 必须一起提交，缺一不可。

### 4.4 升级依赖版本

**Java 端：**
```
# 升级 Spring Boot 版本（同时升级所有 BOM 管控的依赖）
→ 修改 pom.xml 中 <parent> 的 <version>
→ 运行：./mvnw.cmd clean compile -U
→ 运行测试：./mvnw.cmd test
→ 确认无报错后提交
```

**JavaScript 端：**
```
# 查看可升级的包
→ npm outdated

# 在 caret 范围内升级（安全升级）
→ npm update

# 升级到最新大版本（需谨慎，可能有 Breaking Changes）
→ npm install <包名>@latest

# 升级后验证
→ npm run build
→ 确认无报错后提交 package.json 和 package-lock.json
```

### 4.5 发布新版本

当需要发布新版本时，**前后端版本号同步更新**：

```
步骤 1：确定新版本号（遵循语义化版本）
       → 修复 Bug：PATCH + 1（例如 1.0.0 → 1.0.1）
       → 新增功能：MINOR + 1（例如 1.0.0 → 1.1.0）
       → 不兼容变更：MAJOR + 1（例如 1.0.0 → 2.0.0）

步骤 2：更新后端版本
       → 修改 backend/pom.xml 中的 <version>

步骤 3：更新前端版本
       → 修改 frontend/package.json 中的 "version"

步骤 4：提交并打标签
       → git add -A
       → git commit -m "release: v1.1.0"
       → git tag v1.1.0
       → git push && git push --tags
```

### 4.6 升级工具链版本

| 升级项 | 修改文件 | 验证命令 |
|-------|---------|---------|
| JDK | `.java-version` + `pom.xml` 的 `<java.version>` | `./mvnw.cmd clean compile` |
| Maven | `backend/.mvn/wrapper/maven-wrapper.properties` 的 `distributionUrl` | `./mvnw.cmd --version` |
| Node.js | `frontend/.nvmrc` + `package.json` 的 `engines.node` | `node --version` |
| npm | `package.json` 的 `engines.npm` | `npm --version` |

---

## 五、常见问题

### Q1：为什么后端依赖没有写 `<version>`？
因为本项目继承了 `spring-boot-starter-parent` BOM。BOM 会为约 300 个常用依赖提供经过兼容性测试的版本号。只要依赖在 BOM 管控范围内，就不需要也**不应该**自己指定版本号。

### Q2：`package-lock.json` 冲突了怎么办？
```bash
# 接受当前分支的 package.json，然后重新生成 lock 文件
git checkout --ours package.json
npm install
git add package.json package-lock.json
git commit
```

### Q3：Maven Wrapper（`mvnw`）是什么？为什么不直接用 `mvn`？
Maven Wrapper 是一个脚本，它会自动下载 `maven-wrapper.properties` 中指定版本的 Maven。好处是：
- **新成员不需要手动安装 Maven**，运行 `./mvnw.cmd` 即可
- **所有人使用完全相同的 Maven 版本**，避免"在我机器上是好的"问题

### Q4：Caret（`^`）和 Tilde（`~`）有什么区别？
| 符号 | 含义 | 示例 |
|------|------|------|
| `^1.2.3` | 兼容 `1.x.x`，≥ `1.2.3` | 可升级到 `1.9.9`，不会到 `2.0.0` |
| `~1.2.3` | 兼容 `1.2.x`，≥ `1.2.3` | 可升级到 `1.2.9`，不会到 `1.3.0` |
| `1.2.3` | 精确锁定 | 只能是 `1.2.3` |

### Q5：我应该把 `node_modules/` 和 `target/` 提交到 Git 吗？
**绝对不要。** 这些是自动生成的产物，已在 `.gitignore` 中排除。提交它们会导致仓库体积爆炸且产生无意义的冲突。

---

## 六、版本管理检查清单

在每次合并代码前，请确认以下事项：

- [ ] 前后端项目版本号一致（`pom.xml` 与 `package.json`）
- [ ] 新增的 Java 依赖：BOM 管控的未写 version，非 BOM 的在 `<properties>` 中声明了版本变量
- [ ] 新增的 JS 依赖：`package.json` 和 `package-lock.json` 同时提交
- [ ] 未提交 `node_modules/`、`target/`、`.env` 等忽略文件
- [ ] `mvnw.cmd clean compile` 通过
- [ ] `npm run build` 通过
