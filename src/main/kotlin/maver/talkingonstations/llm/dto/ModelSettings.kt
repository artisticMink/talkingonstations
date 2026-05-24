package maver.talkingonstations.llm.dto

import maver.talkingonstations.TosSettings

data class ModelSettings(
    val id: String,
    val maxTokens: Int = TosSettings.maxTokens,
    val temperature: Float = TosSettings.temperature.toFloat(),
    val topP: Float = TosSettings.topP.toFloat(),
    val topK: Float = TosSettings.topK.toFloat(),
    val reasoningEffort: String = TosSettings.reasoningEffort,
)
