package com.arick.aiassistant.core.ml

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class DocumentTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractFromImage(uri: Uri): ExtractionResult = withContext(Dispatchers.IO) {
        val image = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(image).await()
        ExtractionResult(
            text = result.text.trim(),
            note = if (result.text.isBlank()) "图片 OCR 未识别到文本。" else null,
        )
    }

    suspend fun extractFromPdf(
        uri: Uri,
        maxPages: Int = 8,
    ): ExtractionResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            ?: return@withContext ExtractionResult(
                text = "",
                note = "PDF 无法读取。",
            )

        fileDescriptor.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val pagesToProcess = minOf(renderer.pageCount, maxPages)
                val pageTexts = buildList {
                    repeat(pagesToProcess) { index ->
                        val pageText = renderer.renderPageToBitmap(index).let { bitmap ->
                            bitmap.useBitmap { renderBitmap ->
                                recognizeBitmap(renderBitmap)
                            }
                        }
                        if (pageText.isNotBlank()) {
                            add("第 ${index + 1} 页\n$pageText")
                        }
                    }
                }

                val note = buildPdfNote(
                    pageCount = renderer.pageCount,
                    processedPages = pagesToProcess,
                    hasText = pageTexts.isNotEmpty(),
                )

                ExtractionResult(
                    text = pageTexts.joinToString(separator = "\n\n"),
                    note = note,
                )
            }
        }
    }

    private suspend fun recognizeBitmap(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return recognizer.process(image).await().text.trim()
    }

    private fun PdfRenderer.renderPageToBitmap(index: Int): Bitmap {
        return openPage(index).use { page ->
            val scale = 2
            val bitmap = createBitmap(page.width * scale, page.height * scale)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        }
    }

    private fun buildPdfNote(
        pageCount: Int,
        processedPages: Int,
        hasText: Boolean,
    ): String {
        if (!hasText) {
            return "PDF 已完成 OCR，但当前未识别到文本。"
        }
        return if (pageCount > processedPages) {
            "PDF 共 $pageCount 页，当前为控制耗时仅 OCR 前 $processedPages 页。"
        } else {
            "PDF 已完成 $processedPages 页 OCR。"
        }
    }
}

private inline fun <T> Bitmap.useBitmap(block: (Bitmap) -> T): T {
    return try {
        block(this)
    } finally {
        recycle()
    }
}
