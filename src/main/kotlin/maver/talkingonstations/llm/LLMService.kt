package maver.talkingonstations.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

class LLMService(
    private val api: HttpApiInterface
) {
    suspend fun send(context: LLMContext): Message {
        return withContext(Dispatchers.IO) {
            api.send(
                instructions = context.getSystemInstructionsMerged(),
                messages = context.getPublicMessageCopy()
            )
        }
    }

    fun setModelSettings(modelSettings: ModelSettings) {
        api.setModelSettings(modelSettings)
    }

    fun getModelSettings(): ModelSettings = api.getModelSettings()
}