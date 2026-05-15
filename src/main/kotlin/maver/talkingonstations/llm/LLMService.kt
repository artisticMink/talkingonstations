package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import kotlinx.coroutines.CoroutineExceptionHandler
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
            return client.send(
                instructions = context.getSystemInstructionsMerged(),
                messages = context.getPublicMessageCopy().filter { message -> message.role !== ChatRoles.INFO },
                model = model
            )
        } catch (exception: HttpApiRequestException) {
            TosInspector.error("Request failed with status code ${exception.statusCode}", this::class )
            TosInspector.error("Response body: ${exception.responseBody}", this::class )

            if (Global.getSettings().isDevMode) TosInspector.error("Request body: ${exception.requestBody.toString()}", this::class )

            return Message(ChatRoles.INFO, "Request failed with status code ${exception.statusCode}. Please retry or consult starsector.log")
        }
    }
}
