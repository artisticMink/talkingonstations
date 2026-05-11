package maver.talkingonstations.llm.dto

import maver.talkingonstations.TosSettings

data class ModelSettings(
    val name: String,
    val maxTokens: Int,
    val temperature: Float,
    val reasoningEffort: String,
    val topP: Float,
    val topK: Float
) {
    companion object {
        fun create(name: String): ModelSettings = ModelSettings(
            name = name,
            maxTokens = TosSettings.maxTokens,
            temperature = TosSettings.temperature.toFloat(),
            topP = TosSettings.topP.toFloat(),
            topK = TosSettings.topK.toFloat(),
            reasoningEffort = TosSettings.reasoningEffort,
        )
    }
}
