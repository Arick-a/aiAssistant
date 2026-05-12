from __future__ import annotations

import json

import httpx

from app.core.config import settings
from app.schemas.ai import AskRequest, SourceItem, SummarizeRequest


class DeepSeekProvider:
    def __init__(self) -> None:
        if not settings.deepseek_api_key:
            raise ValueError("DEEPSEEK_API_KEY is required when AI_PROVIDER=deepseek")

        self.base_url = settings.deepseek_base_url.rstrip("/")
        self.model = settings.ai_model
        self.timeout = settings.ai_request_timeout_seconds
        self.headers = {
            "Authorization": f"Bearer {settings.deepseek_api_key}",
            "Content-Type": "application/json",
        }

    def summarize(self, request: SummarizeRequest) -> tuple[str, list[str]]:
        content = self._chat(
            messages=[
                {
                    "role": "system",
                    "content": (
                        "你是一个严谨的中文文档摘要助手。"
                        "请只依据用户提供的文档内容生成结果，不要编造。"
                        "必须输出 JSON，字段为 summary 和 keyPoints。"
                    ),
                },
                {
                    "role": "user",
                    "content": (
                        f"文档标题：{request.title}\n"
                        f"摘要模式：{request.mode}\n\n"
                        f"文档内容：\n{trim_text(request.text, 24000)}"
                    ),
                },
            ],
            response_format={"type": "json_object"},
        )
        parsed = parse_json_object(content)
        summary = str(parsed.get("summary") or "").strip()
        key_points = parsed.get("keyPoints") or parsed.get("key_points") or []
        if not isinstance(key_points, list):
            key_points = []
        key_points = [str(item).strip() for item in key_points if str(item).strip()]
        return summary, key_points

    def ask(self, request: AskRequest, sources: list[SourceItem]) -> str:
        source_text = "\n\n".join(
            f"[{source.chunkId}]"
            f"{f' 第 {source.page} 页' if source.page is not None else ''}\n"
            f"{source.quote}"
            for source in sources
        )
        return self._chat(
            messages=[
                {
                    "role": "system",
                    "content": (
                        "你是一个中文文档问答助手。"
                        "必须只依据给定来源片段回答。"
                        "如果来源片段不足以回答，请明确说资料不足。"
                        "回答要简洁，并在相关结论后标注来源 chunkId。"
                    ),
                },
                {
                    "role": "user",
                    "content": (
                        f"问题：{request.question}\n\n"
                        f"来源片段：\n{source_text}"
                    ),
                },
            ],
        ).strip()

    def _chat(
        self,
        messages: list[dict[str, str]],
        response_format: dict[str, str] | None = None,
    ) -> str:
        payload: dict[str, object] = {
            "model": self.model,
            "messages": messages,
            "temperature": 0.2,
        }
        if response_format is not None:
            payload["response_format"] = response_format

        with httpx.Client(timeout=self.timeout) as client:
            response = client.post(
                f"{self.base_url}/chat/completions",
                headers=self.headers,
                json=payload,
            )
            response.raise_for_status()
            data = response.json()

        return data["choices"][0]["message"]["content"]


def parse_json_object(content: str) -> dict[str, object]:
    normalized = content.strip()
    if normalized.startswith("```"):
        normalized = normalized.strip("`")
        if normalized.startswith("json"):
            normalized = normalized[4:].strip()
    try:
        parsed = json.loads(normalized)
    except json.JSONDecodeError:
        return {}
    return parsed if isinstance(parsed, dict) else {}


def trim_text(text: str, limit: int) -> str:
    normalized = " ".join(text.split())
    if len(normalized) <= limit:
        return normalized
    return normalized[: limit - 3].rstrip() + "..."
