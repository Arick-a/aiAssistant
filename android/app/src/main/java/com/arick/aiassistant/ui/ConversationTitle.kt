package com.arick.aiassistant.ui

import com.arick.aiassistant.core.model.DocumentConversation

private val numberedNewConversationRegex = Regex("""^新会话 (\d+)$""")

internal fun nextConversationTitle(
    conversations: List<DocumentConversation>,
): String {
    val usedNumbers = conversations
        .mapNotNull { conversation ->
            numberedNewConversationRegex
                .matchEntire(conversation.title.trim())
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
        }
        .toSet()

    var nextNumber = 1
    while (nextNumber in usedNumbers) {
        nextNumber += 1
    }
    return "新会话 $nextNumber"
}
