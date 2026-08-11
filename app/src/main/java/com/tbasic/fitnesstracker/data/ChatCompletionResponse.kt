package com.tbasic.fitnesstracker.data

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Usage
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val provider: String? = null,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String
)
