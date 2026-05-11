package maver.talkingonstations.httpapi.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message

@Serializable
data class ChatCompletionRequestBody(
    val model: String,
    val messages: List<ChatCompletionsMessage>,
    @SerialName("max_tokens")
    val maxTokens: Int,
    val temperature: Float,
    @SerialName("top_p")
    val topP: Float,
    @SerialName("reasoning_effort")
    val reasoningEffort: String,
)

@Serializable
data class ChatCompletionsMessage(
    val role: String,
    val content: String,
) {
    companion object {
        fun fromInstructions(instructions: String): ChatCompletionsMessage {
            return ChatCompletionsMessage(role = "system", content = instructions)
        }

        fun fromMessages(messages: List<Message>): List<ChatCompletionsMessage> {
            return messages
                .filter { it.role != ChatRoles.SYSTEM && it.role != ChatRoles.INFO }
                .map { ChatCompletionsMessage(role = it.role.name.lowercase(), content = it.content) }
        }
    }
}
