package maver.talkingonstations.chat

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.InspectableInterface
import maver.talkingonstations.TosInspector
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.llm.LLMContext
import maver.talkingonstations.llm.LLMService
import maver.talkingonstations.llm.dto.GameInfo
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

/**
 * A conversation session between [player] and [npc] at a given [market].
 * Manages chat history, prompt composition, and LLM communication.
 *
 * Register callbacks via [beforeContinueAsPlayer] and [afterChatResponse]
 * to hook into the conversation lifecycle.
 *
 * @property player The player involved in the conversation
 * @property npc The NPC involved in the conversation
 * @property market The market from which this conversation has been triggered
 */
class Chat(
    private val player: PersonAPI,
    private val npc: PersonAPI,
    private val market: MarketAPI,
) : LLMContext(GameInfo(player, npc, market)), InspectableInterface {
    var beforeContinueAsPlayer: ((message: String) -> Unit)? = null
    var afterChatResponse: ((message: String) -> Unit)? = null

    private val chatHistory get() = publicMessages
    private val api = HttpApiRegistry.getSelectedApi()
    private val llmService: LLMService = LLMService(api)

    var modelSettings: ModelSettings = ModelSettings.create(api.getDefaultModelName())

    init {
        TosInspector.register(this)
    }

    /**
     * Sends [content] as a player message and requests an LLM response.
     * Invokes [beforeContinueAsPlayer].
     *
     * @param content The player message content
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChatAsPlayer(content: String) {
        if (content.isEmpty()) return

        addPlayerMessage(content)
        beforeContinueAsPlayer?.invoke(content)
        continueChat()
    }

    /**
     * Requests the next LLM response and appends it to the chat history.
     * Invokes [afterChatResponse]
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChat() {
        if (chatHistory.isEmpty()) return

        val responseMessage: Message = llmService.send(this, modelSettings)

        chatHistory.add(responseMessage)
        afterChatResponse?.invoke(chatHistory.last().content)
    }

    /**
     * Removes the last NPC message and re-requests a response.
     * No-op if the last message is from the player.
     * */
    suspend fun retryLastMessage() {
        if (chatHistory.isNotEmpty() && !endsWithPlayerMessage()) {
            chatHistory.removeLast()
            continueChat()
        }
    }

    fun addPlayerMessage(content: String) {
        chatHistory.add(
            Message(
                role = ChatRoles.USER,
                content = content
            )
        )
    }

    fun addNpcMessage(content: String) {
        chatHistory.add(
            Message(
                role = ChatRoles.ASSISTANT,
                content = content
            )
        )
    }

    fun getChatMessages(): List<Message> = chatHistory.toList()
    fun getPlayer(): PersonAPI = player
    fun getNpc(): PersonAPI = npc

    private fun endsWithNpcMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.ASSISTANT
    private fun endsWithPlayerMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.USER

    override fun canInspect(): List<String> {
        return listOf("chatHistory", "instructions")
    }

    override fun inspect(item: String): String {
        return when (item) {
            "chatHistory" -> getPublicMessageCopy().joinToString("\n\n")
            else -> ""
        }
    }
}