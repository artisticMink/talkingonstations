package maver.talkingonstations.httpapi.body

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ToolCall
import maver.talkingonstations.llm.dto.Usage

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
                arguments = call.function.arguments,
            )
        }

        // Try to catch varying field names
        val reasoning = listOfNotNull(message.reasoning, message.reasoningContent)
            .firstOrNull { it.isNotBlank() }

        // content is null when the model only returns tool calls.
        return Message(
            role = role,
            content = message.content.orEmpty(),
            toolCalls = toolCalls,
            reasoning = reasoning,
            usage = usage?.let { Usage(it.promptTokens, it.completionTokens, it.totalTokens) },
        )
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
    val reasoning: String? = null,
    @SerialName("reasoning_content")
    val reasoningContent: String? = null,
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
