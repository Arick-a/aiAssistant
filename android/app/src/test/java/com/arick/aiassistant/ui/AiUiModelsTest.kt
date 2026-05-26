package com.arick.aiassistant.ui

import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Test

class AiUiModelsTest {
    @Test
    fun `formatAiRequestError returns friendly timeout message`() {
        val message = formatAiRequestError(SocketTimeoutException("timeout"))

        assertEquals("AI 服务响应超时，请稍后重试或检查后端模型配置。", message)
    }

    @Test
    fun `source display label includes chunk and page`() {
        val source = AiSourceUiItem(
            chunkId = "doc-1-3",
            page = 2,
            quote = "命中的来源内容",
        )

        assertEquals("片段 doc-1-3 · 第 2 页", source.displayLabel())
    }
}
