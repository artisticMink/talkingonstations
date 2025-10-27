package maver.talkingonstations.httpapi

import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

interface HttpApiInterface {
    fun send(instructions: String, messages: List<Message>): Message
    fun getName(): String
    fun getHeaders(): Array<String>
    fun getModel(): String
    fun setModel(modelName: String)
    fun setModelSettings(modelSettings: ModelSettings)
    fun getModelSettings(): ModelSettings
}