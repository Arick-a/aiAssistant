package com.arick.aiassistant.ui

import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

data class AiSourceUiItem(
    val chunkId: String,
    val page: Int?,
    val quote: String,
) {
    fun displayLabel(): String = buildString {
        append("片段 ")
        append(chunkId)
        if (page != null) {
            append(" · 第 ")
            append(page)
            append(" 页")
        }
    }
}

data class BackendHealthUiState(
    val baseUrl: String,
    val status: String = "未检测",
    val detail: String = "点击检测后端，确认 Android 当前能访问 AI 服务。",
    val isChecking: Boolean = false,
)

fun formatAiRequestError(throwable: Throwable): String = when (throwable) {
    is SocketTimeoutException -> "AI 服务响应超时，请稍后重试或检查后端模型配置。"
    is HttpException -> when (throwable.code()) {
        400 -> "AI 请求参数不完整，请确认文档文本和问题内容。"
        401, 403 -> "AI 服务鉴权失败，请检查后端模型 API Key 配置。"
        429 -> "AI 服务请求过于频繁，请稍后重试。"
        in 500..599 -> "AI 后端服务异常，请查看后端日志。"
        else -> "AI 请求失败，HTTP ${throwable.code()}。"
    }
    is IOException -> "无法连接 AI 后端，请确认服务已启动、地址正确且手机能访问该网络。"
    else -> throwable.message ?: "AI 请求失败，请稍后重试。"
}

fun formatBackendHealthError(throwable: Throwable): String = when (throwable) {
    is SocketTimeoutException -> "检测超时，请确认后端服务和网络连通性。"
    is HttpException -> "后端响应异常，HTTP ${throwable.code()}。"
    is IOException -> "无法连接后端，请检查服务是否启动、IP 是否正确或是否需要 adb reverse。"
    else -> throwable.message ?: "后端检测失败。"
}
