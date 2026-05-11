package maver.talkingonstations.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maver.talkingonstations.TosInspector
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.httpapi.exception.HttpApiRequestException
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

/**
 * Wrapper class for a client implementing [HttpApiInterface]
 */
class LLMService(
    private val client: HttpApiInterface
) {
    suspend fun send(context: LLMContext, model: ModelSettings): Message {
        try {
            return withContext(Dispatchers.IO) {
                client.send(
                    instructions = context.getSystemInstructionsMerged(),
                    messages = context.getPublicMessageCopy(),
                    model = model,
                )
            }
        } catch (exception: HttpApiRequestException) {
            TosInspector.error("Request failed with status code ${exception.statusCode}", this::class )
            TosInspector.error("Request body: ${exception.message}", this::class )

            return Message(ChatRoles.INFO, "Request failed with status code ${exception.statusCode}. Please retry or consult starsector.log")
        }
    }
}
