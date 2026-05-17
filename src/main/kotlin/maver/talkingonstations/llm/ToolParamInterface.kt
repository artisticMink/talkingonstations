package maver.talkingonstations.llm

import kotlinx.serialization.Serializable

interface ToolParamInterface {
    val parameters: Map<String,String>
}