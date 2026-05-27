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
    @SerialName("top_k")
    val topK: Float,
    @SerialName("reasoning_effort")
    val reasoningEffort: String? = null,
    val tools: List<ToolCallDefinition>? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Float,
    @SerialName("presence_penalty")
    val presencePenalty: Float,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Float,
)

@Serializable
data class ChatCompletionsMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenrouterToolCallResult>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
) {
    companion object {
        fun fromInstructions(instructions: String) =
            ChatCompletionsMessage(role = "system", content = instructions)

        fun fromMessages(messages: List<Message>): List<ChatCompletionsMessage> =
            messages
                .filter { it.role != ChatRoles.SYSTEM && it.role != ChatRoles.INFO }
                .map { message ->
                    when (message.role) {
                        ChatRoles.TOOL -> ChatCompletionsMessage(
                            role = "tool",
                            content = message.content,
                            toolCallId = message.toolCallId,
                        )
                        else -> ChatCompletionsMessage(
                            role = message.role.name.lowercase(),
                            content = message.content,
                            toolCalls = message.toolCalls
                                .ifEmpty { null }
                                ?.map(OpenrouterToolCallResult::fromDomain),
                        )
                    }
                }
    }
}