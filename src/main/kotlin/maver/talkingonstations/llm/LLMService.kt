package maver.talkingonstations.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

/**
 * Wrapper class for a client implementing [HttpApiInterface]
 */
class LLMService(
    private val client: HttpApiInterface
) {
    suspend fun send(context: LLMContext, model: ModelSettings): Message {
        return withContext(Dispatchers.IO) {
            client.send(
                instructions = context.getSystemInstructionsMerged(),
                messages = context.getPublicMessageCopy(),
                model = model,
            )
        }
    }
}
