package com.arick.aiassistant.core.ml

import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.core.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordSearchEngine @Inject constructor() {
    fun search(
        documents: List<ImportedDocument>,
        query: String,
    ): List<SearchResult> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        return documents.flatMap { document ->
            chunkText(document.extractedText).mapIndexedNotNull { index, chunk ->
                val matchCount = chunk.countOccurrences(normalizedQuery)
                if (matchCount == 0) {
                    null
                } else {
                    SearchResult(
                        documentId = document.id,
                        documentTitle = document.title,
                        chunkIndex = index,
                        snippet = chunk.trim(),
                        matchCount = matchCount,
                    )
                }
            }
        }.sortedWith(
            compareByDescending<SearchResult> { it.matchCount }
                .thenBy { it.documentTitle }
                .thenBy { it.chunkIndex },
        )
    }

    private fun chunkText(
        text: String,
        chunkSize: Int = 240,
        chunkOverlap: Int = 60,
    ): List<String> {
        val normalized = text.trim()
        if (normalized.isBlank()) return emptyList()

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < normalized.length) {
            val end = minOf(start + chunkSize, normalized.length)
            chunks += normalized.substring(start, end)
            if (end == normalized.length) break
            start = maxOf(0, end - chunkOverlap)
        }
        return chunks
    }
}

private fun String.countOccurrences(query: String): Int {
    val source = lowercase()
    val target = query.lowercase()
    var count = 0
    var startIndex = 0
    while (true) {
        val foundIndex = source.indexOf(target, startIndex)
        if (foundIndex < 0) return count
        count += 1
        startIndex = foundIndex + target.length
    }
}
