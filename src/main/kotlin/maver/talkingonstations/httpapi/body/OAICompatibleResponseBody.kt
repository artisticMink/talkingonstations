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
data class OAICompatibleResponseBody(
    val id: String,
    @SerialName("object")
    val objectName: String,
    @SerialName("created_at")
    val createdAt: Int,
    val model: String,
    val output: List<OAICompatibleResponseOutput>,
    val usage: OAICompatibleResponseUsage
) {
    fun getLastMessage() = output.last().let {
        Message(
            when (it.role) {
                "assistant" -> ChatRoles.ASSISTANT
                "user" -> ChatRoles.USER
                else -> ChatRoles.USER
            },
            it.content.first().text
        ).apply {
            it.id?.let { id -> meta["id"] = id }
            it.status?.let { status -> meta["status"] = status }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OAICompatibleResponseOutput(
    val type: String,
    val id: String? = null,
    val status: String? = null,
    val role: String,
    val content: List<OAICompatibleResponseOutputContent>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OAICompatibleResponseOutputContent(
    val type: String,
    val text: String,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OAICompatibleResponseUsage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

