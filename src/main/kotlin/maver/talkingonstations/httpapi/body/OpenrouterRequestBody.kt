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
)

@Serializable
data class OpenrouterMessage(
    val role: String,
    val content: String,
) {
    companion object {
        fun fromInstructions(instructions: String) =
            OpenrouterMessage(role = "system", content = instructions)

        fun fromMessages(messages: List<Message>): List<OpenrouterMessage> =
            messages
                .filter { it.role != ChatRoles.SYSTEM && it.role != ChatRoles.INFO }
                .map { OpenrouterMessage(role = it.role.name.lowercase(), content = it.content) }
    }
}

@Serializable
data class OpenrouterReasoning(
    val effort: String,
)
