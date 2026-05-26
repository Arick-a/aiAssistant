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
- Android 与后端摘要 / 问答接口联调入口

## 本地联调

### 启动后端

后端使用 Python + FastAPI 开发。首次启动需要先创建虚拟环境并安装依赖：

```bash
./scripts/start_backend.sh
```

脚本会自动创建 `backend/.venv`、安装 `backend/requirements.txt`，并启动 `uvicorn`。
启动前会打印本机访问地址和当前局域网访问地址，真机调试优先使用 `LAN` / `Health` 对应地址。

如果需要指定监听地址或端口：

```bash
BACKEND_HOST=0.0.0.0 BACKEND_PORT=8000 ./scripts/start_backend.sh
```

也可以手动执行：

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

启动后可验证：

```bash
curl http://127.0.0.1:8000/health
```

期望返回：

```json
{"status":"ok"}
```

### Android 访问后端

后端地址配置在：

```text
android/core-network/src/main/java/com/arick/aiassistant/core/network/NetworkConfig.kt
```

不同运行环境使用不同地址：

- Android 模拟器访问电脑后端：`http://10.0.2.2:8000/`
- 真机和电脑在同一局域网：`http://<电脑局域网 IP>:8000/`
- 当前开发机示例：`http://192.168.7.133:8000/`

如果真机访问超时，先用手机浏览器打开：

```text
http://<电脑局域网 IP>:8000/health
```

如果浏览器也打不开，通常是 WiFi 开启了设备隔离。可以改用手机热点、无隔离局域网，或通过 USB 调试使用 `adb reverse`。

## AI 接口说明

当前 `/summarize` 和 `/ask` 支持通过后端接入 DeepSeek 云模型。模型 API Key 只配置在后端，不放到 Android 客户端。

后端环境变量示例：

```bash
AI_PROVIDER=deepseek
AI_MODEL=deepseek-v4-flash
DEEPSEEK_API_KEY=你的 DeepSeek API Key
DEEPSEEK_BASE_URL=https://api.deepseek.com
```

如果没有配置 `DEEPSEEK_API_KEY`，后端会回退到本地启发式占位实现，方便继续做联调。

当前实现：

- `/summarize`：配置 DeepSeek 后调用真实模型生成摘要和要点
- `/ask`：Android 传入文档 chunks，后端选取相关来源片段后调用真实模型回答
- 来源片段仍由后端返回给 Android 展示

后续可以继续补充 embedding、向量检索和更完整的 RAG 流程。
