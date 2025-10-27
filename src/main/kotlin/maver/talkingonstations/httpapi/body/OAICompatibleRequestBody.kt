package maver.talkingonstations.httpapi.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message

@Serializable
data class OAICompatibleRequestBody(
    val model: String,
    val instructions: String,
    val input: List<OAICompatibleRequestInput>,
    val reasoning: OAICompatibleRequestReasoning? = null,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int,
    val temperature: Float,
    @SerialName("top_p")
    val topP: Float,
)

@Serializable
data class OAICompatibleRequestInput(
    val type: String,
    val role: String,
    val id: String?,
    val status: String?,
    val content: List<OAICompatibleRequestContent>
) {
    companion object {
        fun fromMessages(messages: List<Message>) = messages
            .filter { it.role != ChatRoles.SYSTEM }
            .mapNotNull {
                if (it.role != ChatRoles.SYSTEM) OAICompatibleRequestInput(
                    type = "message",
                    role = it.role.name.lowercase(),
                    content = listOf(
                        OAICompatibleRequestContent(
                            "input_text",
                            it.content,
                        )
                    ),
                    id = if (it.role == ChatRoles.ASSISTANT) it.meta["id"] else null,
                    status = if (it.role == ChatRoles.ASSISTANT) it.meta["status"] else null,
                ) else null
            }.ifEmpty { listOf() }
    }
}

@Serializable
data class OAICompatibleRequestContent(
    val type: String,
    val text: String
)

@Serializable
data class OAICompatibleRequestReasoning(
    val effort: String
)