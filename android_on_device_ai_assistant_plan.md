# Android 端侧 AI 助手项目规划

## 1. 项目目标

打造一个以 Android 为主端的本地 AI 文档助手，围绕以下能力展开：

- 图片/PDF/文本导入
- 本地 OCR
- 文档管理与检索
- AI 摘要
- AI 问答
- 后续扩展到本地知识库、多文档问答、结构化提取、端侧模型增强

项目定位不是做一个通用聊天机器人，而是做一个可持续使用的本地知识工具。核心价值是：

- 离用户文件更近
- 支持隐私敏感内容处理
- 在移动端具备较好的可用性
- 支持从 demo 逐步演进到可产品化版本

## 2. 产品范围

### 2.1 核心使用场景

- 用户导入图片、扫描件、PDF、纯文本文档
- App 对内容执行 OCR 或文本提取
- 用户查看原文、提取文本、AI 摘要
- 用户基于文档提问，系统结合检索结果进行回答
- 后续支持多文档问答、知识库组织、结构化提取和导出

### 2.2 非目标

当前阶段不做以下内容：

- 通用型 AI 助手
- 多 Agent 编排平台
- 自训练模型
- 复杂 SaaS 后台
- 一上来全量端侧 LLM 方案

## 3. 总体技术方案

## 3.1 Android 端技术选型

- UI：Jetpack Compose
- 架构：ViewModel + StateFlow
- 本地数据库：Room
- 依赖注入：Hilt
- 网络：Retrofit + OkHttp
- 文件导入：Storage Access Framework
- OCR：Google ML Kit Text Recognition
- PDF：文本型 PDF 优先直接提取，扫描型 PDF 渲染成图片后再做 OCR
- 序列化：kotlinx.serialization 或 Moshi，二选一即可

说明：

- 新项目直接用 Compose，开发效率更高
- 不引入重型 MVI 框架，先用轻量状态管理
- OCR 第一阶段优先使用成熟 SDK，不自己做模型训练

## 3.2 后端技术选型

建议使用轻后端，不要让 Android 客户端直接持有云模型 API Key。

推荐方案：

- 框架：FastAPI
- 部署：Docker / Docker Compose / Serverless 均可
- 作用：作为 AI 网关层，负责 prompt 组装、模型调用、限流、日志与后续扩展

后端第一阶段只需要少量接口：

- `/health`
- `/summarize`
- `/ask`
- `/extract`（可选，第二阶段更适合启用）

## 3.3 模型调用策略

采用分阶段策略：

- OCR：第一阶段即本地运行
- 检索：第一阶段本地关键词检索
- 摘要/问答：第一阶段先接云端模型
- 端侧 LLM：第三阶段再逐步引入

原因：

- 这样可以最快验证产品价值
- 避免一开始陷入端侧模型性能、兼容性和包体问题
- 后续再把简单任务迁移到端侧

## 3.4 数据流

一条典型链路如下：

1. 用户导入文件
2. App 完成 OCR 或文本提取
3. 保存原文件、提取文本、元数据
4. 对文本切块并建立索引
5. 用户发起摘要或提问
6. 本地先做检索，找出相关片段
7. 将问题与命中片段提交给后端
8. 后端调用模型生成摘要或答案
9. App 展示结果与引用来源

## 4. 工程模块建议

建议从一开始就按能力拆模块，避免后期功能堆在一个 app module 中。

### 4.1 Android 模块建议

- `app`
  - 组装应用、导航、主题、启动入口
- `core-model`
  - 通用数据模型
- `core-data`
  - Room、DAO、Repository
- `core-network`
  - Retrofit、接口定义、网络层封装
- `core-ml`
  - OCR、后续 embedding 与端侧模型封装
- `feature-import`
  - 文件导入、权限、媒体处理
- `feature-docs`
  - 文档列表、详情、原文展示、OCR 文本展示
- `feature-search`
  - 文本切块、检索、搜索结果展示
- `feature-ai`
  - 摘要、问答、结构化提取能力封装
- `feature-chat`
  - 问答会话 UI 与消息记录
- `feature-settings`
  - 模型设置、存储设置、实验功能开关

### 4.2 后端模块建议

- `api`
  - 对外接口层
- `service`
  - prompt 拼装、模型调用
- `schemas`
  - 请求响应结构
- `providers`
  - 模型厂商适配层
- `utils`
  - 日志、配置、通用工具

## 5. 数据结构建议

第一阶段先控制在最小闭环即可。

### 5.1 第一阶段主要表

- `documents`
  - 文档基本信息，如标题、类型、原始文件路径、创建时间
- `document_pages`
  - 页级文本与页码信息，可选
- `document_chunks`
  - 切块结果、所属文档、页码、chunk 顺序
- `conversations`
  - 对话会话，先支持单文档会话
- `messages`
  - 用户问题、AI 回答、时间戳

### 5.2 第二阶段扩展表

- `message_sources`
  - 一条回答对应的引用来源
- `document_tags`
  - 标签
- `collections`
  - 文档集合或文件夹
- `collection_documents`
  - 集合与文档关联
- `favorites`
  - 收藏问答或摘要
- `structured_results`
  - 结构化抽取结果

### 5.3 第三阶段扩展表

- `chunk_embeddings`
  - chunk 向量数据或索引引用
- `model_downloads`
  - 本地模型下载和状态管理
- `search_history`
  - 搜索历史
- `usage_stats`
  - 本地统计信息

## 6. 页面规划

### 6.1 第一阶段页面

- 首页
  - 最近文档
  - 快速导入入口
  - 最近使用记录
- 文档详情页
  - 原图/原文
  - OCR 文本
  - 摘要结果
  - 提问入口
- 搜索页
  - 文档内或文档列表的关键词搜索
- 问答页
  - 单文档问答
  - 答案与引用片段展示
- 设置页
  - 基础设置与实验配置

### 6.2 第二阶段页面增强

- 多文档搜索结果页
- 知识库集合页
- 标签筛选页
- 收藏页
- 结构化提取结果页
- 导出/分享面板

### 6.3 第三阶段页面增强

- 本地模型管理页
- 检索方式设置页
- 端侧/云端模式切换页
- 性能与存储状态页

## 7. 第一阶段实施计划

目标：做出一个可演示的 MVP，完成单文档导入、OCR、摘要、问答闭环。

### 7.1 要做的功能

1. 文件导入
- 拍照导入图片
- 相册选择图片
- 导入 PDF
- 导入 txt / md

2. 本地 OCR / 文本提取
- 图片 OCR
- PDF 文本提取
- 扫描型 PDF 转图片后 OCR
- 保存文本结果

3. 文档管理
- 文档列表
- 文档详情
- 删除文档
- 查看 OCR 文本

4. 本地检索
- 基于关键词的简单搜索
- 返回命中片段
- 高亮匹配文本

5. AI 摘要
- 生成整篇摘要
- 生成关键点
- 显示结果

6. AI 问答
- 基于单文档提问
- 检索相关 chunk
- 展示回答与引用来源

### 7.2 技术实现方案

- OCR：ML Kit
- 文本存储：Room
- 文本切块：本地实现简单 chunk 策略
- 检索：关键词检索
- AI 调用：Android 调 FastAPI，FastAPI 调云端模型
- Prompt：只做摘要和文档问答两套模板

### 7.3 第一阶段开发顺序

1. 搭建 Android 空项目、导航和基础模块
2. 接入文件导入能力
3. 实现 OCR / PDF 文本提取
4. 建立 Room 表和 Repository
5. 做文档列表和详情页
6. 做文本切块和关键词检索
7. 搭建 FastAPI 服务
8. 实现 `/summarize`
9. 实现 `/ask`
10. App 侧接入摘要与问答
11. 补充基础错误处理与 loading 状态

### 7.4 第一阶段交付标准

- 能导入图片/PDF/文本
- 能查看 OCR 或提取结果
- 能基于文档生成摘要
- 能基于文档提问
- 回答带来源片段
- 整体链路可稳定演示

## 8. 第二阶段实施计划

目标：从单文档 demo 升级成可持续使用的本地知识工具。

### 8.1 要做的功能

1. 会话能力
- 每篇文档独立对话
- 历史问答记录
- 会话重命名
- 收藏重要问答

2. 本地知识库
- 文档分组
- 标签
- 文件夹/集合
- 限定知识库范围问答

3. 多文档问答
- 跨文档检索
- 聚合多个来源回答
- 展示来源列表

4. 结构化提取
- 名片/通知/简历等模板提取
- 自动字段抽取
- 结果存储
- 导出结构化结果

5. 分享与导出
- 导出摘要
- 导出 OCR 文本
- 导出问答记录
- 系统分享

6. 本地向量检索
- 为 chunk 生成 embedding
- 保存向量
- 实现语义召回
- 做关键词 + 向量的混合检索

### 8.2 技术实现方案

- 会话系统：Room 新增 conversations、messages、message_sources
- 知识库系统：增加 collections、tags 相关表
- 结构化提取：先采用模板 + LLM JSON 输出
- 导出：基于系统文件写入和 Android 分享 Intent
- 向量检索：先做小规模本地 embedding + 相似度搜索，不急于引入重型向量数据库

### 8.3 第二阶段开发顺序

1. 会话能力
2. 知识库组织能力
3. 多文档问答
4. 结构化提取
5. 导出与分享
6. 本地向量检索

### 8.4 第二阶段交付标准

- 用户可长期保存并继续使用历史资料
- 可对单文档和多文档进行问答
- 可对常见文档执行结构化提取
- 可导出摘要、OCR、问答结果
- 检索体验优于第一阶段纯关键词方案

## 9. 第三阶段实施计划

目标：增强端侧能力，把项目从“本地知识工具”升级为“端侧优先的智能助手”。

### 9.1 要做的功能

1. 端侧模型引入
- 接入本地 embedding 模型
- 接入端侧摘要模型或轻量问答模型
- 支持模型下载与管理

2. 云端/端侧混合策略
- 简单摘要走端侧
- 复杂问答走云端
- 用户可切换模式
- 提供失败兜底逻辑

3. 检索增强
- 语义检索优化
- rerank 策略
- 更稳定的混合召回与排序

4. 多模态增强
- 图片内容问答
- OCR 与图像理解结合
- 文档截图快速问答

5. 产品化增强
- 使用统计
- 性能监控
- 本地缓存策略
- 模型与存储占用管理

### 9.2 技术实现方案

- 端侧模型：根据设备能力逐步接入 LiteRT-LM、llama.cpp 或其他可落地方案
- Embedding：优先引入体积较小、设备兼容性好的本地 embedding 模型
- 模式路由：根据任务复杂度决定走本地还是云端
- 监控：记录模型下载状态、推理耗时、检索耗时、失败率

### 9.3 第三阶段开发顺序

1. 端侧 embedding
2. 本地语义检索优化
3. 端侧摘要能力
4. 云端/端侧混合路由
5. 本地模型管理页
6. 多模态增强
7. 性能与稳定性治理

### 9.4 第三阶段交付标准

- 至少部分 AI 能力可在端侧离线使用
- 用户可切换端侧/云端模式
- 检索、摘要、问答体验进一步提升
- 产品在真实设备上具备可持续运行能力

## 10. Prompt 与 AI 接口建议

### 10.1 第一阶段 prompt 类型

- 摘要 prompt
  - 生成简短摘要
  - 生成关键点
  - 生成待办/日期/联系人提取结果
- 文档问答 prompt
  - 明确要求仅依据给定资料回答
  - 若资料不足则直接说明
  - 要求输出引用片段编号

### 10.2 第二阶段 prompt 类型

- 多文档问答 prompt
- 模板化结构化提取 prompt
- 会话延续 prompt

### 10.3 第三阶段 prompt 类型

- 端侧轻量 prompt
- 云端增强 prompt
- 按任务路由的 prompt 模板

## 10.4 第一阶段接口定义草图

这一部分用于帮助后续快速搭建 FastAPI 最小后端，并让 Android 侧尽早完成联调。

### 10.4.1 `/health`

用途：

- 健康检查
- 确认后端是否可达

请求：

- `GET /health`

响应示例：

```json
{
  "status": "ok"
}
```

### 10.4.2 `/summarize`

用途：

- 基于 OCR 或文档提取文本生成摘要

请求示例：

```json
{
  "documentId": "doc_001",
  "title": "sample.pdf",
  "text": "完整文本内容",
  "mode": "summary"
}
```

字段说明：

- `documentId`：文档 ID
- `title`：文档标题
- `text`：待总结的原始文本
- `mode`：摘要模式，可先支持 `summary` 和 `key_points`

响应示例：

```json
{
  "documentId": "doc_001",
  "summary": "这是摘要结果",
  "keyPoints": [
    "要点1",
    "要点2",
    "要点3"
  ]
}
```

### 10.4.3 `/ask`

用途：

- 基于文档命中片段进行问答

请求示例：

```json
{
  "documentId": "doc_001",
  "question": "这份文档的核心结论是什么？",
  "chunks": [
    {
      "chunkId": "c1",
      "page": 1,
      "text": "命中的片段 1"
    },
    {
      "chunkId": "c2",
      "page": 2,
      "text": "命中的片段 2"
    }
  ]
}
```

响应示例：

```json
{
  "answer": "基于资料得出的回答",
  "sources": [
    {
      "chunkId": "c1",
      "page": 1,
      "quote": "命中的片段 1"
    },
    {
      "chunkId": "c2",
      "page": 2,
      "quote": "命中的片段 2"
    }
  ]
}
```

### 10.4.4 `/extract`

用途：

- 第二阶段结构化提取使用
- 第一阶段可以先预留，不必实现完整版本

请求示例：

```json
{
  "documentId": "doc_001",
  "template": "resume",
  "text": "待抽取文本"
}
```

响应示例：

```json
{
  "documentId": "doc_001",
  "template": "resume",
  "result": {
    "name": "张三",
    "phone": "13800000000",
    "email": "demo@example.com"
  }
}
```

## 10.5 第一阶段 Android 目录结构草图

建议在第一阶段就把 Android 工程按后续可扩展方式组织好，避免所有逻辑堆在 `app` 里。

```text
android/
  app/
  core-model/
  core-data/
  core-network/
  core-ml/
  feature-import/
  feature-docs/
  feature-search/
  feature-ai/
  feature-chat/
  feature-settings/
```

### 各模块职责

- `app`
  - Application
  - Hilt 初始化
  - 导航图
  - 主题与全局入口

- `core-model`
  - Document
  - Chunk
  - Conversation
  - Message
  - AI 请求与响应模型

- `core-data`
  - Room Database
  - DAO
  - Repository
  - 本地数据源

- `core-network`
  - Retrofit Service
  - DTO
  - 网络层封装

- `core-ml`
  - OCR 封装
  - 文本切块器
  - 后续 embedding / 本地模型能力

- `feature-import`
  - 文件导入
  - 相机 / 相册
  - 权限

- `feature-docs`
  - 文档列表页
  - 文档详情页
  - OCR 文本查看

- `feature-search`
  - 搜索页
  - 本地检索逻辑
  - 搜索结果展示

- `feature-ai`
  - 摘要请求
  - 结构化提取请求
  - AI 状态管理

- `feature-chat`
  - 问答页
  - 消息列表
  - 来源展示

- `feature-settings`
  - 服务地址配置
  - 调试开关
  - 实验功能入口

## 10.6 第一阶段后端目录结构草图

```text
backend/
  app/
    api/
    providers/
    schemas/
    services/
    utils/
    main.py
  requirements.txt
  .env.example
  README.md
```

### 各目录职责

- `api/`
  - 路由层
  - 定义 `/health`、`/summarize`、`/ask`

- `providers/`
  - 云模型厂商适配层
  - 后续可支持多个 provider

- `schemas/`
  - Pydantic 请求响应结构

- `services/`
  - prompt 拼装
  - 模型调用
  - 结果解析

- `utils/`
  - 配置读取
  - 日志
  - 通用函数

- `main.py`
  - FastAPI 入口

## 10.7 第一阶段落地任务拆分建议

后续如果要用 Codex 分任务推进，建议按下面顺序拆，不要同时平推所有内容。

### Task 1：创建仓库与基础目录

- 创建 monorepo
- 建立 `android/`、`backend/`、`docs/`
- 初始化 README
- 初始化 `.gitignore`

### Task 2：Android 工程初始化

- 创建 Compose 项目
- 接入 Hilt
- 搭建导航和主题
- 建立基础模块

### Task 3：文档导入与 OCR

- 图片导入
- PDF 导入
- ML Kit OCR
- 文本结果保存

### Task 4：本地数据层

- 定义 Room 表
- 建立 DAO 和 Repository
- 实现文档列表和详情读取

### Task 5：搜索能力

- 文本切块
- 关键词检索
- 搜索结果展示

### Task 6：后端最小闭环

- FastAPI 启动
- `/health`
- `/summarize`
- `/ask`

### Task 7：Android 与后端联调

- Retrofit 接口定义
- 摘要页面联调
- 问答页面联调
- 异常处理和 loading 状态

### Task 8：MVP 收尾

- 页面细节优化
- 本地错误提示
- 调试入口
- README 运行文档

## 11. 风险与注意事项

### 11.1 Android 侧风险

- PDF 解析与 OCR 质量不稳定
- 大文档会带来内存与性能压力
- 文本切块和检索策略不合理会直接影响问答质量

### 11.2 AI 侧风险

- 云端模型成本不可控
- 客户端直连模型服务存在 API Key 泄漏风险
- OCR 噪声会降低摘要与问答质量
- 端侧模型引入过早会拖慢项目进度

### 11.3 产品侧风险

- 一开始功能做太多，项目容易失控
- 如果没有会话、导出、知识库组织能力，用户难以持续使用
- 如果没有来源引用，问答可信度会不足

## 12. 开发环境与本机配置

这一部分用于明确本项目在本机开发时需要准备的环境，以及是否需要额外安装前后端专用编辑器。

### 12.1 是否需要单独安装 FastAPI 编辑器

不需要。

- FastAPI 是 Python Web 框架，不是独立开发工具
- 只需要安装 Python 环境即可
- 编辑器可以继续使用 Android Studio / IntelliJ
- 如果希望 Python 开发体验更顺手，也可以额外安装 VS Code，但不是必须

结论：

- 不需要专门安装所谓的 “FastAPI 编辑器”
- 需要的是 Python 运行环境和常规代码编辑器

### 12.2 本机建议安装的软件

#### Android 开发环境

- Android Studio
- JDK 17
- Android SDK
- Gradle

#### Python 后端环境

- Python 3.11 或 3.12
- pip
- venv（使用 Python 自带虚拟环境即可）

常用 Python 后端依赖包括：

- fastapi
- uvicorn
- pydantic
- httpx

#### Git 与代码托管

- Git
- GitHub 账号
- SSH key 或 Personal Access Token

#### 可选但推荐的软件

- Docker Desktop
- Postman 或 Bruno
- VS Code

说明：

- Docker 主要用于后续部署或快速本地启动后端环境
- Postman/Bruno 用于调试 FastAPI 接口
- VS Code 适合写 Python，但如果已经习惯 Android Studio / IntelliJ，也可以不装

### 12.3 建议的本机最小开发环境

如果以“能快速开工”为标准，本机至少准备以下环境：

- Android Studio
- JDK 17
- Python 3.11+
- Git
- GitHub 账号

如果希望后续部署和接口调试更顺手，再补：

- Docker Desktop
- Postman 或 Bruno
- VS Code（可选）

## 13. GitHub 仓库组织建议

### 13.1 是否需要拆成多个仓库

当前阶段不建议一开始拆多个仓库。

原因：

- 你是一个人开发
- 当前目标是先完成 MVP
- Android 与后端接口会频繁一起调整
- 单仓库更便于统一管理文档、接口和版本

因此，第一阶段推荐：

- 使用一个 GitHub 仓库

### 13.2 推荐仓库结构

建议使用 monorepo 方式，结构示例如下：

```text
android-ai-assistant/
  android/
  backend/
  docs/
  README.md
  .gitignore
```

说明：

- `android/`：Android 客户端工程
- `backend/`：FastAPI 后端
- `docs/`：需求文档、技术方案、接口说明、阶段计划
- `README.md`：项目说明、运行方式、目录结构

### 13.3 什么时候考虑拆仓

只有在以下情况出现时，再考虑拆成两个仓库：

- 前后端已经长期稳定并独立迭代
- 开始多人协作
- 后端需要独立部署、独立权限管理
- Android 与后端发布节奏明显不同

那时可拆成：

- `android-ai-assistant-android`
- `android-ai-assistant-backend`

但对当前阶段来说，不建议一开始就拆。

### 13.4 推荐的版本管理方式

建议采用以下策略：

- 当前阶段使用单仓库
- 每个阶段建立清晰的 milestone
- 文档、Android、后端代码一并提交
- README 中写清本地运行步骤

这样更适合后续通过 Codex 按阶段持续推进。

## 14. 实施原则

- 先做闭环，再做增强
- 先做单文档，再做多文档
- 先做关键词检索，再做向量检索
- 先做云端摘要问答，再逐步引入端侧模型
- 始终优先保证 Android 真机上的可用性和响应速度
- 每一阶段都需要有独立可演示、可验证的成果

## 15. 后续使用 Codex 的建议方式

后续可按以下方式分阶段推进：

### 15.1 第一阶段
让 Codex 先完成：

- Android 项目骨架搭建
- 基础模块拆分
- 文件导入与 OCR 接入
- Room 表与 Repository 设计
- 文档列表/详情页
- FastAPI 最小后端
- 摘要与单文档问答接口打通

### 15.2 第二阶段
让 Codex 继续完成：

- 会话能力
- 收藏与导出
- 知识库分组与标签
- 多文档问答
- 结构化提取
- 本地向量检索

### 15.3 第三阶段
让 Codex 继续完成：

- 端侧 embedding 与模型接入
- 云端/端侧路由策略
- 本地模型管理
- 多模态问答增强
- 性能治理与产品化细节

## 16. 最终结论

这个项目最适合按“三阶段演进”实施：

- 第一阶段：做成能跑通的单文档 AI 助手 MVP
- 第二阶段：做成本地知识库和多文档工具产品
- 第三阶段：做成带端侧模型能力的智能助手

整个项目的关键不是一开始追求最强 AI，而是持续围绕以下闭环推进：

- 文件进入系统
- 文本被提取和管理
- 内容可被检索
- 用户可获得可信的摘要和问答结果
- 系统逐步增强为真正可长期使用的本地知识助手
