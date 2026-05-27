package maver.talkingonstations.httpapi

import com.fs.starfarer.api.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.body.OpenrouterMessage
import maver.talkingonstations.httpapi.body.OpenrouterReasoning
import maver.talkingonstations.httpapi.body.OpenrouterRequestBody
import maver.talkingonstations.httpapi.body.OpenrouterResponseBody
import maver.talkingonstations.httpapi.body.ToolCallDefinition
import maver.talkingonstations.httpapi.exception.HttpApiRequestException
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.dto.ApiSettings
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync
import java.util.concurrent.TimeUnit

class OpenrouterHttpApi : HttpApiInterface {
    private val client = OkHttpClient.Builder()
        // Fixes one instance of lazy class loading triggering SecurityException
        .retryOnConnectionFailure(false)
        // OpenRouter can be a bit of a slug sometimes
        .connectTimeout(30000, TimeUnit.MILLISECONDS)
        .readTimeout(30000, TimeUnit.MILLISECONDS)
        .build()

    override var supportsToolCalling: Boolean = false

    override suspend fun send(
        apiSettings: ApiSettings,
        instructions: String,
        messages: List<Message>,
        model: ModelSettings,
        tools: List<ToolInterface>,
    ): Message {
        val chatMessages = mutableListOf(OpenrouterMessage.fromInstructions(instructions))
        chatMessages.addAll(OpenrouterMessage.fromMessages(messages))

        val requestBody = OpenrouterRequestBody(
            model = model.id,
            messages = chatMessages,
            maxTokens = model.maxTokens,
            temperature = model.temperature,
            topP = model.topP,
            topK = model.topK,
            reasoning = OpenrouterReasoning(model.reasoningEffort),
            tools = if (supportsToolCalling) ToolCallDefinition.fromTools(tools).ifEmpty { null } else emptyList(),
            frequencyPenalty = model.frequencyPenalty,
            repetitionPenalty = model.repetitionPenalty,
            presencePenalty = model.presencePenalty,
        )

        //if (Global.getSettings().isDevMode) TosInspector.debug(requestBody.toString(), this::class)

        val jsonBody = Json.encodeToString(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val header = Headers.headersOf(*getAuthHeader(apiSettings), *getHeaders())
        val request = Request.Builder()
            .url(apiSettings.url.toHttpUrl())
            .headers(header)
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        val call = client.newCall(request)

        return call.executeAsync().use { response ->
            withContext(Dispatchers.IO) {
                if (response.code != 200) {
                    Global.getLogger(javaClass).warn("Request failed with error code ${response.code}")
                    throw HttpApiRequestException(
                        message = "Request failed",
                        statusCode = response.code,
                        responseBody = response.body.string(),
                        requestBody = jsonBody,
                    )
                }

                val responseBodyString = response.body.string() ?: "{\"error\":\"Empty response body\"}"

                val jsonResponse: JsonElement = Json.parseToJsonElement(responseBodyString)
                val errorElem = jsonResponse.jsonObject["error"]
                if (errorElem != null && errorElem != JsonNull) {
                    Global.getLogger(javaClass).error("OpenRouter API returned error: $jsonResponse")
                    return@withContext Message(ChatRoles.INFO, "Could not get an answer. Check starsector.log for more information.")
                }

                return@withContext try {
                    Json.decodeFromString<OpenrouterResponseBody>(responseBodyString).getLastMessage()
                } catch (exception: Exception) {
                    Global.getLogger(javaClass).error("Error parsing response body: $exception")
                    Message(ChatRoles.INFO, "Could not process the answer. Check starsector.log for more information.")
                }
            }
        }
    }

    override fun getName() = "Openrouter"

    private fun getHeaders() = arrayOf(
        "content-type", "application/json",
        "accept", "application/json",
        "X-Title", "Starsector/TalkingOnStations",
    )

    private fun getAuthHeader(apiSettings: ApiSettings) = arrayOf(
        "Authorization", "Bearer ${apiSettings.getApiKey()}",
    )
}
