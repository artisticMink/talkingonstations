package maver.talkingonstations.llm.dto

import maver.talkingonstations.chat.ChatRoles

data class Message(
    val role: ChatRoles,
    var content: String,
    val toolCalls: List<ToolCall> = listOf(),
    val toolCallId: String? = null,
    val isTransient: Boolean = false,
    val reasoning: String? = null,
    val usage: Usage? = null,
)