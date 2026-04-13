# AI Assistant

本仓库用于实现 Android 端侧 AI 文档助手的第一阶段 MVP。

## 项目目标

第一阶段目标是完成单文档导入、文本提取、本地检索、AI 摘要、AI 问答的最小闭环。

## 仓库结构

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
backend/
  app/
    api/
    providers/
    schemas/
    services/
    utils/
docs/
```

## 第一阶段范围

- Android 侧完成文件导入、OCR 或文本提取、文档管理和本地检索
- 后端提供 `/health`、`/summarize`、`/ask`
- Android 与后端完成摘要和问答联调

## 当前状态

当前仓库已完成：

- Android 工程初始化
- 文档导入、OCR / PDF OCR
- Room 持久化
- 本地关键词搜索
- FastAPI 最小后端骨架
- Android Retrofit 网络层骨架
