package maver.talkingonstations.httpapi

import com.fs.starfarer.api.Global
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import lunalib.lunaSettings.LunaSettings
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.body.ChatCompletionsMessage
import maver.talkingonstations.httpapi.body.ChatCompletionRequestBody
import maver.talkingonstations.httpapi.body.ChatCompletionResponseBody
import maver.talkingonstations.httpapi.exception.HttpApiRequestException
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
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.getFloat

class ChatCompletionHttpApi : HttpApiInterface {
    private val client: OkHttpClient = OkHttpClient()

    private var activeModel: String = getDefaultModelName()
    private var activeModelSettings: ModelSettings = getDefaultModelSettings()

    override fun send(instructions: String, messages: List<Message>): Message {
        val chatMessages = mutableListOf(ChatCompletionsMessage.fromInstructions(instructions))
        chatMessages.addAll(ChatCompletionsMessage.fromMessages(messages))

        val requestBody = ChatCompletionRequestBody(
            model = activeModel,
            messages = chatMessages,
            maxTokens = activeModelSettings.maxTokens,
            temperature = activeModelSettings.temperature,
            topP = activeModelSettings.topP,
        )

        val jsonBody = Json.encodeToString(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(getApiConfiguration().url.toHttpUrl())
            .headers(Headers.headersOf(*getAuthHeader(), *getHeaders()))
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        val response: Response = client.newCall(request).execute()
        if (response.code != 200) {
            Global.getLogger(javaClass).warn("Request failed with error code ${response.code}")
            if (Global.getSettings().isDevMode) Global.getLogger(javaClass).warn("${response.body}")
            throw HttpApiRequestException("Request failed")
        }

        val responseBodyString = response.body?.string() ?: "{\"error\":\"Empty response body\"}"

        val jsonResponse: JsonElement = Json.parseToJsonElement(responseBodyString)
        val errorElem = jsonResponse.jsonObject["error"]
        if (errorElem != null && errorElem != JsonNull) {
            Global.getLogger(javaClass).error("Chat Completions API returned error: $jsonResponse")
            return Message(ChatRoles.INFO, "Could not get an answer. Check starsector.log for more information.")
        }

        try {
            val chatCompletionsResponse = Json.decodeFromString<ChatCompletionResponseBody>(responseBodyString)
            return chatCompletionsResponse.getLastMessage()
        } catch (exception: Exception) {
            Global.getLogger(javaClass).error("Error parsing response body: $exception")
            return Message(ChatRoles.INFO, "Could not process the answer. Check starsector.log for more information.")
        }
    }

    override fun getHeaders(): Array<String> {
        return arrayOf(
            "content-type", "application/json",
            "accept", "application/json",
            "X-Title", "Starsector/TalkingOnStations"
        )
    }

    override fun getName(): String {
        return "OpenAI Compatible (v1)"
    }

    override fun getModel(): String {
        return activeModel
    }

    override fun setModel(modelName: String) {
        activeModel = modelName
    }

    override fun setModelSettings(modelSettings: ModelSettings) {
        activeModelSettings = modelSettings
    }

    override fun getModelSettings(): ModelSettings {
        return activeModelSettings
    }

    private fun getAuthHeader(): Array<String> {
        return arrayOf(
            "Authorization", "Bearer ${getApiConfiguration().getApiKey()}",
        )
    }

    companion object ChatCompletionsHttpApiConfiguration {
        const val MODEL_FILE = "data/config/api/chat_completion.json"

        private val models: MutableList<ModelSettings> = mutableListOf()
        private val configuration: ApiSettings = ApiSettings(
            LunaSettings.getString("maver_talkingonstations", "tos_api") ?: "",
            LunaSettings.getString("maver_talkingonstations", "tos_apiUrl") ?: "",
            LunaSettings.getString("maver_talkingonstations", "tos_apiKey") ?: ""
        )

        private lateinit var defaultModelName: String
        private lateinit var defaultInstruction: String

        init {
            loadConfig()

            if (Global.getSettings().isDevMode)
                models.forEach {
                    Global.getLogger(javaClass).info("Found configuration for model: ${it.name}")
                }
        }

        fun create(): ChatCompletionHttpApi {
            return ChatCompletionHttpApi()
        }

        fun getModels(): List<ModelSettings> {
            return models.toList()
        }

        fun getApiConfiguration(): ApiSettings {
            return configuration
        }

        private fun getDefaultModelName(): String {
            return defaultModelName
        }

        private fun getDefaultModelSettings(): ModelSettings {
            val settings = models.find { it.name == defaultModelName }
            if (settings == null) throw Exception("No configuration found for default model $defaultModelName")

            return settings
        }

        private fun loadConfig() {
            val apiConfig = Global.getSettings().loadJSON(MODEL_FILE, "maver_talkingonstations")

            if (!apiConfig.has("defaultModel")) throw Exception("Config key defaultModel not found in $MODEL_FILE")
            apiConfig.getString("defaultModel")?.let { modelName ->
                defaultModelName = modelName
            }

            if (!apiConfig.has("defaultInstruction")) throw Exception("Config key defaultInstruction not found in $MODEL_FILE")
            apiConfig.getString("defaultInstruction")?.let { instruction ->
                defaultInstruction = instruction
            }

            apiConfig.getJSONArray("models")?.let { modelList ->
                for (i in 0 until modelList.length()) {
                    val model = modelList.getJSONObject(i)
                    validateModel(model)?.let {
                        models.add(getModelSettings(it))
                    }
                }
            }
        }

        private fun validateModel(model: JSONObject): JSONObject? {
            when (isValidSettingsJson(model)) {
                true -> return model
                else -> Global.getLogger(javaClass).warn("Invalid model configuration. Skipping.")
            }

            return null
        }

        private fun getModelSettings(json: JSONObject): ModelSettings {
            val modelJson: JSONObject = json.getJSONObject("model")
            val settingsJson: JSONObject = json.getJSONObject("settings")
            val samplerJson: JSONObject = settingsJson.getJSONObject("sampler")
            val system = settingsJson.getString("system").takeIf { it.isNotEmpty() } ?: defaultInstruction

            return ModelSettings(
                name = modelJson.getString("name"),
                maxTokens = modelJson.getInt("maxTokens"),
                system = system,
                reasoning = null,
                temperature = samplerJson.getFloat("temperature"),
                topP = samplerJson.getFloat("topP")
            )
        }

        private fun isValidSettingsJson(json: JSONObject): Boolean {
            // ToDo: Implement settings validation
            return true
        }
    }
}
