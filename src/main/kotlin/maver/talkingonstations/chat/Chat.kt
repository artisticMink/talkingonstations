package maver.talkingonstations.chat

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import kotlinx.coroutines.launch
import maver.talkingonstations.TosBackgroundScope
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosStrings
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
) : LLMContext(GameInfo(player, npc, market)) {
    var beforeContinueAsPlayer: ((message: String) -> Unit)? = null
    var afterChatResponse: ((message: String) -> Unit)? = null
    var onProgress: ((message: Message) -> Unit) = {}

    private val chatHistory get() = publicMessages
    private val api = HttpApiRegistry.getSelectedApi()
    private val llmService: LLMService = LLMService(api)

    var modelSettings: ModelSettings = ModelSettings.create(api.getDefaultModelName())

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
     * Invokes [afterChatResponse], [onProgress]
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChat() {
        if (chatHistory.isEmpty()) return

        val responseMessage: Message = llmService.send(this, modelSettings, onProgress)

        chatHistory.add(responseMessage)
        chatHistory.lastOrNull()?.let { afterChatResponse?.invoke(it.content) }
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

    /**
     * Summarizes the chat thus far
     *
     * Starts a background request, then propagates back to main thread to update the person memory.
     */
    fun summarizeToNpcMemory() {
            addPlayerMessage(TosStrings.Prompt.SUMMARY)
            TosBackgroundScope.scope.launch {
                val summary = llmService.send(this@Chat, modelSettings)
                if (summary.role == ChatRoles.INFO) {
                    // ToDo: Add user-facing error message
                    TosInspector.error(
                        "Post-chat summary failed for ${npc.nameString}",
                        this::class,
                    )
                } else {
                    TosEveryFrameScriptQueue.add { npc.memory.set(TosMemoryKeys.MEMORY_STORAGE, summary.content) }
                }
            }
    }

    private fun endsWithNpcMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.ASSISTANT
    private fun endsWithPlayerMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.USER
}