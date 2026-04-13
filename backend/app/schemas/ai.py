from typing import List, Optional

from pydantic import BaseModel


class HealthResponse(BaseModel):
    status: str


class SummarizeRequest(BaseModel):
    documentId: str
    title: str
    text: str
    mode: str = "summary"


class SummarizeResponse(BaseModel):
    documentId: str
    summary: str
    keyPoints: List[str]


class ChunkItem(BaseModel):
    chunkId: str
    page: Optional[int] = None
    text: str


class AskRequest(BaseModel):
    documentId: str
    question: str
    chunks: List[ChunkItem]


class SourceItem(BaseModel):
    chunkId: str
    page: Optional[int] = None
    quote: str


class AskResponse(BaseModel):
    answer: str
    sources: List[SourceItem]
