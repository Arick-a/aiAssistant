from __future__ import annotations

from app.core.config import settings
from app.providers.deepseek_provider import DeepSeekProvider
from app.schemas.ai import AskRequest, AskResponse, SourceItem, SummarizeRequest, SummarizeResponse


def build_summary(request: SummarizeRequest) -> SummarizeResponse:
    if should_use_deepseek():
        summary, key_points = DeepSeekProvider().summarize(request)
        if summary or key_points:
            return SummarizeResponse(
                documentId=request.documentId,
                summary=summary or "当前文档暂无可总结的文本内容。",
                keyPoints=key_points or ["暂无可提取要点"],
            )

    return build_heuristic_summary(request)


def build_answer(request: AskRequest) -> AskResponse:
    sources = select_sources(request)
    if not sources:
        return AskResponse(
            answer="当前没有可用的文档片段，无法基于资料回答。",
            sources=[],
        )

    if should_use_deepseek():
        answer = DeepSeekProvider().ask(request, sources)
        return AskResponse(
            answer=answer or "当前资料不足，无法基于文档回答。",
            sources=sources,
        )

    joined_sources = " ".join(source.quote for source in sources if source.quote)
    return AskResponse(
        answer="基于当前命中的文档片段，" + truncate_text(joined_sources, 220),
        sources=sources,
    )


def should_use_deepseek() -> bool:
    return settings.ai_provider.lower() == "deepseek" and bool(settings.deepseek_api_key)


def build_heuristic_summary(request: SummarizeRequest) -> SummarizeResponse:
    normalized_text = normalize_whitespace(request.text)
    sentences = split_sentences(normalized_text)
    summary = " ".join(sentences[:2]).strip()
    if not summary:
        summary = "当前文档暂无可总结的文本内容。"

    key_points = sentences[:3]
    if not key_points and normalized_text:
        key_points = truncate_lines(normalized_text, max_items=3, item_length=48)
    if not key_points:
        key_points = ["暂无可提取要点"]

    return SummarizeResponse(
        documentId=request.documentId,
        summary=summary if request.mode == "summary" else "；".join(key_points),
        keyPoints=key_points,
    )


def select_sources(request: AskRequest) -> list[SourceItem]:
    ranked_chunks = sorted(
        request.chunks,
        key=lambda item: score_chunk(item.text, request.question),
        reverse=True,
    )
    sources = [
        SourceItem(
            chunkId=chunk.chunkId,
            page=chunk.page,
            quote=truncate_text(normalize_whitespace(chunk.text), 180),
        )
        for chunk in ranked_chunks[:3]
    ]
    return sources


def score_chunk(text: str, question: str) -> int:
    normalized_text = normalize_whitespace(text).lower()
    keywords = [part for part in normalize_whitespace(question).lower().split(" ") if part]
    return sum(normalized_text.count(keyword) for keyword in keywords) + (1 if normalized_text else 0)


def split_sentences(text: str) -> list[str]:
    if not text:
        return []

    separators = ["。", "！", "？", ".", "!", "?", "\n"]
    current = [text]
    for separator in separators:
        parts: list[str] = []
        for item in current:
            parts.extend(item.split(separator))
        current = parts

    return [truncate_text(item.strip(), 100) for item in current if item.strip()]


def truncate_lines(text: str, max_items: int, item_length: int) -> list[str]:
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    return [truncate_text(line, item_length) for line in lines[:max_items]]


def truncate_text(text: str, limit: int) -> str:
    if len(text) <= limit:
        return text
    return text[: limit - 3].rstrip() + "..."


def normalize_whitespace(text: str) -> str:
    return " ".join(text.split())
