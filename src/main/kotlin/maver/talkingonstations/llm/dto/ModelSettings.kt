package maver.talkingonstations.llm.dto

data class ModelSettings(
    val name: String,
    val maxTokens: Int,
    val system: String,
    val temperature: Float,
    /**
     * @link https://openrouter.ai/docs/api-reference/responses-api/reasoning#reasoning-effort-levels
     */
    val reasoning: String?,
    val topP: Float,
)