package maver.talkingonstations.httpapi.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message

@Serializable
data class OpenrouterRequestBody(
    val model: String,
    val messages: List<OpenrouterMessage>,
    val reasoning: OpenrouterReasoning,
    @SerialName("max_tokens")
    val maxTokens: Int,
    val temperature: Float,
    @SerialName("top_p")
    val topP: Float,
    @SerialName("top_k")
    val topK: Float,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Float,
    @SerialName("presence_penalty")
    val presencePenalty: Float,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Float,
    val tools: List<ToolCallDefinition>? = null,
)

@Serializable
data class OpenrouterMessage(
    val role: String,
    // Nullable: an assistant message that only requests tools carries no text.
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OpenrouterToolCallResult>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
) {
    companion object {
        fun fromInstructions(instructions: String) =
            OpenrouterMessage(role = "system", content = instructions)

        fun fromMessages(messages: List<Message>): List<OpenrouterMessage> =
            messages
                .filter { it.role != ChatRoles.SYSTEM && it.role != ChatRoles.INFO }
                .map { message ->
                    when (message.role) {
                        // Tool Calling
                        ChatRoles.TOOL -> OpenrouterMessage(
                            role = "tool",
                            content = message.content,
                            toolCallId = message.toolCallId,
                        )
                        else -> OpenrouterMessage(
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

@Serializable
data class OpenrouterReasoning(
    val effort: String,
)
