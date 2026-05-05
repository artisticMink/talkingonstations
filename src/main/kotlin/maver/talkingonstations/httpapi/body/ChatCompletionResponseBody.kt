package maver.talkingonstations.httpapi.body

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionResponseBody(
    val id: String,
    val model: String,
    val choices: List<ChatCompletionsChoice>,
    val usage: ChatCompletionsUsage,
) {
    fun getLastMessage(): Message {
        val choice = choices.last()
        val role = when (choice.message.role) {
            "assistant" -> ChatRoles.ASSISTANT
            "user" -> ChatRoles.USER
            else -> ChatRoles.ASSISTANT
        }
        return Message(role, choice.message.content)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionsChoice(
    val index: Int,
    val message: ChatCompletionsChoiceMessage,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionsChoiceMessage(
    val role: String,
    val content: String,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionsUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)
