package maver.talkingonstations.llm.dto

import kotlinx.serialization.Serializable
import maver.talkingonstations.llm.ToolParamInterface

@Serializable
data class ToolParameters(
    override val parameters: Map<String, String>,
) : ToolParamInterface