# Backend

第一阶段后端提供最小接口闭环：

- `GET /health`
- `POST /summarize`
- `POST /ask`

## 本地启动

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

启动后可访问：

- `http://127.0.0.1:8000/health`
- `http://127.0.0.1:8000/docs`

## 当前实现说明

当前版本是联调占位实现：

- `/summarize` 基于文本启发式生成摘要和要点
- `/ask` 基于命中片段做简单排序并返回答案与来源

下一阶段可在 `services/` 中替换成真实云模型调用。
