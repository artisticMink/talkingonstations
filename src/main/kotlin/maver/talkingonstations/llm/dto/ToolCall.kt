package maver.talkingonstations.llm.dto

/**
 * A single tool call the model requested.
 */
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: String = "{}",
)
