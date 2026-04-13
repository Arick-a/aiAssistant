from fastapi import APIRouter

from app.schemas.ai import AskRequest, AskResponse, HealthResponse, SummarizeRequest, SummarizeResponse
from app.services.ai_service import build_answer, build_summary


router = APIRouter()


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


@router.post("/summarize", response_model=SummarizeResponse)
def summarize(request: SummarizeRequest) -> SummarizeResponse:
    return build_summary(request)


@router.post("/ask", response_model=AskResponse)
def ask(request: AskRequest) -> AskResponse:
    return build_answer(request)
