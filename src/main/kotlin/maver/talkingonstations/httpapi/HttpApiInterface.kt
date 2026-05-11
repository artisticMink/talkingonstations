package maver.talkingonstations.httpapi

import maver.talkingonstations.llm.dto.ApiSettings
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

interface HttpApiInterface {
    var apiSettings: ApiSettings

    fun send(instructions: String, messages: List<Message>, model: ModelSettings): Message
    fun getName(): String
    fun getModels(): Map<String, String>
    fun getDefaultModelName(): String
}
