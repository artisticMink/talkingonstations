package maver.talkingonstations.httpapi.body

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ToolCall

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterResponseBody(
    val id: String,
    val model: String,
    val choices: List<OpenrouterResponseChoice>,
    val usage: OpenrouterResponseUsage,
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
                // Raw passthrough by design: parse/validate happens once, in
                // LLMService, against the tool's declared schema.
                arguments = call.function.arguments,
            )
        }
        // content is null when the model only returns tool calls.
        return Message(role, message.content.orEmpty(), toolCalls)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterResponseChoice(
    val index: Int,
    val message: OpenrouterResponseMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterResponseMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenrouterToolCallResult> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterResponseUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterToolCallResult(
    val id: String,
    val type: String = "function",
    val function: OpenrouterToolCallFunction,
) {
    companion object {
        fun fromDomain(call: ToolCall) = OpenrouterToolCallResult(
            id = call.id,
            function = OpenrouterToolCallFunction(
                name = call.name,
                arguments = call.arguments,
            ),
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OpenrouterToolCallFunction(
    val name: String,
    /** JSON-encoded argument object; see [OpenrouterResponseBody.getLastMessage]. */
    val arguments: String,
)
