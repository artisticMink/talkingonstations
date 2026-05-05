package maver.talkingonstations.llm.dto

data class ModelSettings(
    val name: String,
    val maxTokens: Int,
    val system: String,
    val temperature: Float,
    val reasoning: String?,
    val topP: Float,
)