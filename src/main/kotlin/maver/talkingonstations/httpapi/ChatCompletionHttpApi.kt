package maver.talkingonstations.httpapi

import com.fs.starfarer.api.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import maver.talkingonstations.TosStrings
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.body.ChatCompletionRequestBody
import maver.talkingonstations.httpapi.body.ChatCompletionResponseBody
import maver.talkingonstations.httpapi.body.ChatCompletionsMessage
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
import okhttp3.Response
import okhttp3.coroutines.executeAsync

class ChatCompletionHttpApi : HttpApiInterface {
    private val client = OkHttpClient()
    private val configPath = "${TosStrings.Path.CONFIG_FOLDER}api/chat_completion.json"

    private val models: Map<String, String>
    private val defaultModelId: String
    private val defaultModelName: String

    override var supportsToolCalling: Boolean = false
    override lateinit var apiSettings: ApiSettings

    init {
        val json = Global.getSettings().loadJSON(configPath, TosStrings.ModConfig.ID)
        defaultModelId = json.getString("defaultModel")
        val modelsJson = json.getJSONObject("models")
        models = buildMap {
            val keys = modelsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                put(key, modelsJson.getString(key))
            }
        }
        defaultModelName = models.entries.firstOrNull { it.value == defaultModelId }?.key
            ?: throw Exception("defaultModel '$defaultModelId' not found in models map of $configPath")
    }

    override suspend fun send(instructions: String, messages: List<Message>, model: ModelSettings, tools: List<ToolInterface>): Message {
        val modelId = models[model.name] ?: defaultModelId
        val chatMessages = mutableListOf(ChatCompletionsMessage.fromInstructions(instructions))
        chatMessages.addAll(ChatCompletionsMessage.fromMessages(messages))

        val requestBody = ChatCompletionRequestBody(
            model = modelId,
            messages = chatMessages,
            maxTokens = model.maxTokens,
            temperature = model.temperature,
            topP = model.topP,
            reasoningEffort = model.reasoningEffort
        )

        val jsonBody = Json.encodeToString(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val header = Headers.headersOf(*getAuthHeader(), *getHeaders())
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
                    Global.getLogger(javaClass).error("Chat Completions API returned error: $jsonResponse")
                    return@withContext Message(ChatRoles.INFO, "Could not get an answer. Check starsector.log for more information.")
                }

                return@withContext try {
                    Json.decodeFromString<ChatCompletionResponseBody>(responseBodyString).getLastMessage()
                } catch (exception: Exception) {
                    Global.getLogger(javaClass).error("Error parsing response body: $exception")
                    Message(ChatRoles.INFO, "Could not process the answer. Check starsector.log for more information.")
                }
            }
        }
    }

    override fun getName() = "Chat-Completion"
    override fun getModels(): Map<String, String> = models
    override fun getDefaultModelName(): String = defaultModelName

    private fun getHeaders() = arrayOf(
        "content-type", "application/json",
        "accept", "application/json",
        "X-Title", "Starsector/TalkingOnStations"
    )

    private fun getAuthHeader() = arrayOf(
        "Authorization", "Bearer ${apiSettings.getApiKey()}",
    )
}
