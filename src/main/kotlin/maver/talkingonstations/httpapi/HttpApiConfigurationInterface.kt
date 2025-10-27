package maver.talkingonstations.httpapi

import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.llm.dto.ApiSettings

interface HttpApiConfigurationInterface {
    fun getApiConfiguration(): ApiSettings
    fun getModels(): List<ModelSettings>
}