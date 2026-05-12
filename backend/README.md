# Backend

第一阶段后端提供最小接口闭环：

- `GET /health`
- `POST /summarize`
- `POST /ask`

## 本地启动

后端使用 Python + FastAPI。建议使用虚拟环境，避免污染系统 Python：

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

如果需要代码变更后自动重载，可以在本机终端尝试：

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

启动后可访问：

- `http://127.0.0.1:8000/health`
- `http://127.0.0.1:8000/docs`

健康检查：

```bash
curl http://127.0.0.1:8000/health
```

期望返回：

```json
{"status":"ok"}
```

## Android 联调地址

Android 端后端地址配置在：

```text
android/core-network/src/main/java/com/arick/aiassistant/core/network/NetworkConfig.kt
```

常见场景：

- Android 模拟器：使用 `http://10.0.2.2:8000/`
- Android 真机：使用 `http://<电脑局域网 IP>:8000/`
- USB 调试 + `adb reverse`：可使用 `http://127.0.0.1:8000/`

真机调试前，建议先在手机浏览器访问：

```text
http://<电脑局域网 IP>:8000/health
```

如果无法访问，通常是公司 / 校园 WiFi 开启了设备隔离，需要切换到手机热点、无隔离 WiFi，或使用 USB 端口转发。

USB 端口转发示例：

```bash
adb reverse tcp:8000 tcp:8000
```

## 当前实现说明

当前版本支持两种模式：

- 配置 `AI_PROVIDER=deepseek` 和 `DEEPSEEK_API_KEY` 时，`/summarize` 与 `/ask` 会调用 DeepSeek 云模型
- 未配置 `DEEPSEEK_API_KEY` 时，自动回退到本地启发式占位实现，便于离线联调

环境变量可以参考 `.env.example`：

```bash
AI_PROVIDER=deepseek
AI_MODEL=deepseek-v4-flash
DEEPSEEK_API_KEY=你的 DeepSeek API Key
DEEPSEEK_BASE_URL=https://api.deepseek.com
AI_REQUEST_TIMEOUT_SECONDS=30
```

本地配置示例：

```bash
cp .env.example .env
# 编辑 .env，填入 DEEPSEEK_API_KEY
```

当前还没有使用 embedding、向量数据库或完整 RAG。问答接口会先对 Android 传入的 chunks 做简单关键词排序，选取来源片段，再交给 DeepSeek 生成回答。

后续可继续补充：

- 更多模型厂商 provider
- 更稳定的 prompt 模板与结构化输出解析
- 更合理的文档切块与 Top-K 选择策略
- embedding 和向量检索
- 基于来源片段的 RAG 问答

## 接口示例

### `POST /summarize`

请求：

```json
{
  "documentId": "doc-1",
  "title": "sample.txt",
  "text": "这是一段文档内容。这里是第二句话。",
  "mode": "summary"
}
```

响应：

```json
{
  "documentId": "doc-1",
  "summary": "这是一段文档内容 这里是第二句话",
  "keyPoints": ["这是一段文档内容", "这里是第二句话"]
}
```

### `POST /ask`

请求：

```json
{
  "documentId": "doc-1",
  "question": "文档主要讲什么？",
  "chunks": [
    {
      "chunkId": "doc-1-0",
      "page": null,
      "text": "这是一段文档内容。"
    }
  ]
}
```

响应：

```json
{
  "answer": "基于当前命中的文档片段，这是一段文档内容。",
  "sources": [
    {
      "chunkId": "doc-1-0",
      "page": null,
      "quote": "这是一段文档内容。"
    }
  ]
}
```
