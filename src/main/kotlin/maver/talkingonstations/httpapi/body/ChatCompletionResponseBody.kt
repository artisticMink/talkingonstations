package maver.talkingonstations.httpapi.body

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ToolCall

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionResponseBody(
    val id: String,
    val model: String,
    val choices: List<ChatCompletionsChoice>,
    val usage: ChatCompletionsUsage? = null,
) {
    fun getLastMessage(): Message {
        val choice = choices.last()
        val message = choice.message
        val role = when (message.role) {
            "assistant" -> ChatRoles.ASSISTANT
            "user" -> ChatRoles.USER
            else -> ChatRoles.ASSISTANT
        }
        val toolCalls = message.toolCalls.map { call ->
            ToolCall(
                id = call.id,
                name = call.function.name,
                arguments = parseArguments(call.function.arguments),
            )
        }
        // content is null when the model only returns tool calls.
        return Message(role, message.content.orEmpty(), toolCalls)
    }

    /**
     * OpenAI-style tool arguments arrive as a JSON-encoded string. v1 keeps
     * every parameter a string, so decode to a flat map; a malformed payload
     * yields an empty map rather than failing the whole response.
     */
    private fun parseArguments(raw: String): Map<String, String> =
        try {
            Json.decodeFromString<Map<String, String>>(raw)
        } catch (exception: Exception) {
            emptyMap()
        }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionsChoice(
    val index: Int,
    val message: ChatCompletionsChoiceMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ChatCompletionsChoiceMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenrouterToolCallResult> = emptyList(),
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
