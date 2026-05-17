package maver.talkingonstations.llm.dto

import maver.talkingonstations.chat.ChatRoles

data class Message(
    val role: ChatRoles,
    var content: String,
    /** Tool calls an ASSISTANT message is requesting. */
    val toolCalls: List<ToolCall> = listOf(),
    /** Set on a TOOL message: the id of the call it answers. */
    val toolCallId: String? = null,
)