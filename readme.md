# AI Consultant

一个基于 `Spring Boot 3 + Vue 3 + LangChain4j + Redis + MinIO` 的本地化 AI 顾问项目。  
项目支持账号登录、会话管理、流式聊天、多轮记忆、在线知识库上传、RAG 文档检索增强、会话级文件记忆，以及图片上传后的多模态分析。

## 项目简介

这个项目的目标不是只做一个普通聊天框，而是围绕“上传资料后直接分析”来搭建完整链路：

- 用户可以上传 `PDF / Word / Excel / TXT / MD`
- 后端会解析文档、切片、向量化并写入 Redis 向量库
- 聊天时会自动做 RAG 检索，把相关片段拼进提示词
- 用户可以给某个会话绑定指定文件，实现“会话级文件记忆”
- 用户还可以上传图片，结合 `qwen-vl-plus` 做视觉分析
- 原始文件和图片会存到 MinIO，支持删除和会话内复用

## 技术栈

### 后端

- `Spring Boot 3.5.0`
- `Spring Web`
- `Spring WebFlux`
- `MyBatis`
- `MySQL`
- `Redis`
- `Sa-Token`
- `LangChain4j 1.0.1-beta6`
- `Qwen3.5-Plus`
- `Qwen-VL-Plus`
- `Apache Tika`
- `Apache POI`
- `MinIO`

### 前端

- `Vue 3`
- `Vue Router 4`
- `Vite 5`
- `Axios`
- `Tailwind CSS`
- `Marked`
- `highlight.js`

### 关键基础设施

- `Redis`
  用于聊天记忆、向量检索
- `MySQL`
  用于用户、会话、消息、文档元数据、会话文件绑定
- `MinIO`
  用于存储知识库原文件和聊天上传图片
- `Apache Tika Server / Tika 解析能力`
  用于统一解析 PDF / Word / Excel / TXT / MD

## 核心能力

### 1. 智能聊天

- 支持登录后多会话聊天
- 支持流式输出
- 支持历史消息记忆
- 支持重新生成最后一条 AI 回复

### 2. 在线知识库

- 上传 `PDF / DOC / DOCX / XLS / XLSX / TXT / MD`
- 文档元数据写入 MySQL
- 原始文档写入 MinIO
- 文本通过 Tika / POI 解析后切片
- 切片向量化后写入 Redis 向量库
- 支持知识库文档列表查看
- 支持删除文档，并同步清理：
  Redis 向量数据、MinIO 原文件、会话文件绑定、MySQL 文档状态

### 3. RAG 检索增强

- 聊天提问时自动向量检索相关文档片段
- 将命中的上下文拼接进系统提示词
- 回答优先基于已上传文档内容
- 如果会话绑定了指定文件，则优先只检索这些文件

### 4. 会话级文件记忆

- 一个会话可以绑定多个知识库文件
- 同一个用户的不同会话可以使用不同资料集
- 对于长期分析型任务，比“全库混检索”更可控

### 5. 图片分析

- 支持聊天区上传图片
- 图片先上传到 MinIO
- 发送消息时可将文字问题和图片一起提交
- 由视觉模型进行多模态分析
- 用户消息区会显示自己发送的图片

### 6. Excel 结构化解析

- 支持 `xls / xlsx`
- 后端会按工作表抽取结构化文本
- 以接近 Markdown 表格的形式转成可检索内容
- 适合财务表、统计表、名单表等结构化资料分析

## 项目结构

```text
CONSULTANT
├─ .idea
├─ .vscode
├─ frontend
│  ├─ src
│  │  ├─ api
│  │  │  ├─ auth.js
│  │  │  └─ conversation.js
│  │  ├─ components
│  │  │  ├─ ChatContainer.vue
│  │  │  ├─ ChatWorkspace.vue
│  │  │  ├─ MarkdownRenderer.vue
│  │  │  ├─ MessageBubble.vue
│  │  │  └─ PromptTemplates.vue
│  │  ├─ router
│  │  │  └─ index.js
│  │  ├─ utils
│  │  │  └─ markdown.js
│  │  ├─ views
│  │  │  ├─ KnowledgeBase.vue
│  │  │  ├─ Login.vue
│  │  │  └─ Register.vue
│  │  ├─ App.vue
│  │  ├─ main.js
│  │  └─ style.css
│  ├─ index.html
│  ├─ package.json
│  ├─ postcss.config.js
│  ├─ tailwind.config.js
│  └─ vite.config.js
├─ src
│  └─ main
│     ├─ java
│     │  └─ com
│     │     └─ itheima
│     │        └─ consultant
│     │           ├─ aiservice
│     │           │  └─ ConsultantService.java
│     │           ├─ common
│     │           │  └─ Result.java
│     │           ├─ config
│     │           │  ├─ CommonConfig.java
│     │           │  ├─ GlobalExceptionHandler.java
│     │           │  ├─ SaTokenConfig.java
│     │           │  ├─ VisionModelConfig.java
│     │           │  └─ WebConfig.java
│     │           ├─ controller
│     │           │  ├─ AssetController.java
│     │           │  ├─ AuthController.java
│     │           │  ├─ ChatController.java
│     │           │  ├─ ConversationController.java
│     │           │  └─ DocumentController.java
│     │           ├─ dto
│     │           │  ├─ ChatStreamRequest.java
│     │           │  ├─ ConversationDocumentSelectionRequest.java
│     │           │  └─ UploadedImageAsset.java
│     │           ├─ entity
│     │           │  ├─ Conversation.java
│     │           │  ├─ Message.java
│     │           │  ├─ User.java
│     │           │  └─ UserDocument.java
│     │           ├─ mapper
│     │           │  ├─ ConversationDocumentBindingMapper.java
│     │           │  ├─ ConversationMapper.java
│     │           │  ├─ MessageMapper.java
│     │           │  ├─ UserDocumentMapper.java
│     │           │  └─ UserMapper.java
│     │           ├─ repository
│     │           │  └─ RedisChatMemoryStore.java
│     │           ├─ service
│     │           │  ├─ AuthService.java
│     │           │  ├─ ConversationService.java
│     │           │  ├─ DocumentPipelineService.java
│     │           │  ├─ DocumentService.java
│     │           │  ├─ MinioStorageService.java
│     │           │  └─ RagChatService.java
│     │           └─ ConsultantApplication.java
│     └─ resources
│        ├─ static
│        ├─ application.yml
│        └─ schema.sql
├─ target
├─ build-frontend.bat
├─ run-dev.bat
├─ pom.xml
└─ readme.md
```

## 环境要求

建议本地准备以下环境：

- `JDK 17`
- `Maven 3.9+`
- `Node.js 18+`
- `npm 9+`
- `MySQL 8`
- `Redis 7+`
- `MinIO`
- `Apache Tika`

## 默认端口与配置

当前项目默认使用以下本地服务：

- 前端开发服务器：`http://localhost:3000`
- 后端接口：`http://localhost:8080`
- Redis：`localhost:6379`
- MySQL：`localhost:3307`
- MinIO：`http://127.0.0.1:9000`
- Tika：`http://127.0.0.1:9998`

`application.yml` 中的关键配置如下：

- 数据库：`consultant`
- MySQL 用户名：`root`
- MySQL 密码：`1234`
- MinIO Bucket：`ros123`
- MinIO Access Key：`minioadmin`
- MinIO Secret Key：`minioadmin`

另外还需要准备模型 API Key：

```bash
API-KEY=你的通义千问兼容模式 API Key
```

Windows PowerShell 示例：

```powershell
$env:API-KEY="你的API_KEY"
```

## 数据库准备

### 1. 创建数据库

先确保 MySQL 中存在数据库：

```sql
CREATE DATABASE consultant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 启动时自动初始化

项目已经开启：

- `spring.sql.init.mode=always`
- `spring.sql.init.encoding=UTF-8`

启动时会自动执行 `src/main/resources/schema.sql` 中的初始化脚本。  
如果你还有用户表、会话表、消息表等自定义初始化 SQL，请确保它们也已经建好或补充到初始化脚本中。

## 如何启动项目

### 方式一：前后端分开开发运行

#### 1. 启动后端

在项目根目录执行：

```bash
mvn spring-boot:run
```

如果你的机器有多个 JDK，请确认 Maven 实际走的是 `JDK 17`。

#### 2. 启动前端

进入前端目录：

```bash
cd frontend
npm install
npm run dev
```

启动后访问：

```text
http://localhost:3000
```

前端会通过 Vite 代理把 `/api` 请求转发到 `http://localhost:8080`。

### 方式二：使用项目自带脚本

根目录提供了两个脚本：

- `run-dev.bat`
  同时启动前后端开发环境
- `build-frontend.bat`
  安装前端依赖并构建到 Spring Boot 静态资源目录

### 方式三：构建前端后由 Spring Boot 统一提供页面

```bash
cd frontend
npm install
npm run build
cd ..
mvn spring-boot:run
```

前端构建产物会输出到：

```text
src/main/resources/static
```

这样可以直接通过后端访问页面。

## 当项目没有 node_modules 时，怎么跑起来

这是最常见的新机器启动场景。只要按下面步骤来就可以：

### 1. 确认已安装 Node.js

执行：

```bash
node -v
npm -v
```

如果命令不存在，先安装 Node.js 18 或更高版本。

### 2. 进入前端目录安装依赖

```bash
cd frontend
npm install
```

执行完成后会自动生成：

```text
frontend/node_modules
```

### 3. 启动前端开发服务器

```bash
npm run dev
```

### 4. 如果只想构建，不想开前端开发服务

```bash
npm run build
```

### 5. 常见问题

- `npm install` 很慢
  可以切换更快的 npm 源或公司内部镜像源
- `node_modules` 不要手动提交到 Git
  这是依赖目录，应通过 `package.json` 重新安装生成
- 安装后仍然报前端依赖缺失
  删除 `frontend/node_modules` 和 `package-lock.json` 后重新执行 `npm install`

## 在线知识库功能说明

在线知识库是这个项目最核心的差异化能力之一。

### 整体链路

1. 用户在知识库页上传文件
2. 后端先把原始文件存入 MinIO
3. 再解析文本内容
4. 按片段切分文档
5. 调用嵌入模型生成向量
6. 把向量片段写入 Redis
7. 把文件元数据写入 MySQL
8. 聊天时基于问题做 RAG 检索并回答

### 当前支持的知识库文件类型

- `PDF`
- `DOC`
- `DOCX`
- `XLS`
- `XLSX`
- `TXT`
- `MD`

### 文档上传后会发生什么

#### 普通文档

- `PDF / Word / TXT / MD`
  由 `Apache Tika` 统一抽取文本

#### Excel 文档

- `XLS / XLSX`
  由 `Apache POI` 做结构化读取
- 按工作表整理成可检索文本
- 保留表头和主要行数据，便于 AI 做表格分析

### 文档删除时会发生什么

删除知识库文档时，系统会同步处理：

- MinIO 中的原始文件
- Redis 中该文档对应的切片向量
- 会话与文档之间的绑定关系
- MySQL 中的文档状态

### 会话级文件记忆

聊天页支持“会话文件”能力：

- 可以为某个会话绑定指定文档
- 绑定后，这个会话优先只检索这些文档
- 不绑定时，默认会检索当前用户全部知识库

这对以下场景很有用：

- 一个会话专门分析某个合同
- 一个会话专门分析某个财务表
- 一个会话专门围绕一组制度文件做问答

## 图片上传与视觉分析功能

除了知识库文档，这个项目还支持图片分析：

- 聊天区可上传图片
- 图片先上传到 MinIO
- 可与文本问题一起发送
- 后端走视觉模型流式分析
- 发送后的图片会显示在用户消息中

适合场景：

- 截图分析
- 报表截图问答
- 界面截图问题定位
- 凭证、清单、图片资料辅助分析

## 主要页面

- 登录页
- 注册页
- 聊天页
- 我的知识库页

## 开发建议

### 推荐开发顺序

1. 先确认 `MySQL / Redis / MinIO / Tika` 都能连通
2. 启动后端
3. 启动前端
4. 先测试注册、登录、聊天
5. 再测试知识库上传
6. 最后测试图片分析和会话级文件记忆

### 启动前自检

- `JAVA_HOME` 是否是 `JDK 17`
- `API-KEY` 是否已配置
- MySQL 端口是否真的是 `3307`
- Redis 是否启动
- MinIO Bucket `ros123` 是否存在
- Tika 是否已启动并可访问

## 常见问题

### 1. 没有 node_modules

执行：

```bash
cd frontend
npm install
```

### 2. 前端能开，接口请求失败

通常检查：

- 后端是否已启动
- Vite 代理目标是否仍是 `8080`
- 登录 token 是否过期

### 3. 上传文档超时

项目已经对知识库上传单独放宽了超时时间。  
如果仍然超时，通常是：

- 文件过大
- 本地模型 / 嵌入接口响应慢
- Tika 服务异常

### 4. PDF 上传时报解析错误

优先检查：

- 当前 JDK 是否为 17
- Maven 是否重新编译过最新代码
- 是否存在旧版 PDFBox 与 Tika 冲突

### 5. 文档删除后刷新又回来

如果出现这种情况，通常是删除链路中 Redis / MySQL 有一步失败。  
当前版本已经对 Redis 向量删除做了兼容处理，正常情况下刷新后不会再回显已删除文档。

## 页面截图

<img width="2549" height="1403" alt="image" src="https://github.com/user-attachments/assets/c0c5fe46-ff56-4c2e-a4ce-acf73edd35ef" />
<img width="2549" height="1403" alt="image" src="https://github.com/user-attachments/assets/a96f29e0-5c9e-4686-ad0b-b1b301444af2" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-12-14" src="https://github.com/user-attachments/assets/7cabf7d4-0245-4e8b-80b8-0c5a6c7b7103" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-12-28" src="https://github.com/user-attachments/assets/acee4ceb-764a-413e-8526-41876588a4c8" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-13-58" src="https://github.com/user-attachments/assets/2d7236b4-4d02-4073-b9d5-54c8cfbf3798" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-14-07" src="https://github.com/user-attachments/assets/b321dca6-3f14-4b9b-b6c8-0d94c56f19ec" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-14-36" src="https://github.com/user-attachments/assets/dd8ddf0e-8dcf-47a0-9a74-ce4270bcf664" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-15-36" src="https://github.com/user-attachments/assets/cbcd03b8-2a77-48f9-934b-755e5bf7e3cb" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-15-41" src="https://github.com/user-attachments/assets/56f68b42-8f40-477f-9966-15f439baafee" />
<img width="1275" height="702" alt="Snipaste_2026-05-06_22-15-53" src="https://github.com/user-attachments/assets/799cebce-00c9-4718-b6a2-17cad8626283" />
<img width="1275" height="702" alt="选着会话文件" src="https://github.com/user-attachments/assets/a115f006-884f-4d89-8d45-943f027382d1" />
<img width="1275" height="702" alt="在线知识库" src="https://github.com/user-attachments/assets/60003ef2-6986-4c14-8d9b-9bd245c5a06a" />


## ⚠️ 编码规范（重要）

本项目统一使用 UTF-8 编码（无 BOM）

AI / Codex 注意：
- 禁止使用 GBK / ANSI
- 所有文件必须显式 UTF-8 编码
- 禁止生成乱码中文

否则视为错误实现
