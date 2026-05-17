package maver.talkingonstations.llm.dto

/**
 * The result of a tool call
 */
data class ToolResult(
    val text: String,
    val terminal: Boolean = false,
)
