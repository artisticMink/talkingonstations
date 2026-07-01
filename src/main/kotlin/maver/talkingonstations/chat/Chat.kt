package maver.talkingonstations.chat

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import kotlinx.coroutines.launch
import maver.talkingonstations.TosBackgroundScope
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosSettings
import maver.talkingonstations.TosStrings
import maver.talkingonstations.campaign.TosCampaignNotifier
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.llm.LLMContext
import maver.talkingonstations.llm.LLMService
import maver.talkingonstations.llm.dto.ApiSettings
import maver.talkingonstations.llm.dto.GameInfo
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.llm.dto.TurnResult

/**
 * A conversation session between [player] and [npc] at a given [market].
 * Manages chat history, prompt composition, and LLM communication.
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

    // Invoked when a tool call deliberately ends the conversation.
    var onEnded: ((farewell: String) -> Unit)? = null
    var onProgress: ((message: Message) -> Unit) = {}

    var ended: Boolean = false
        private set

    private val chatHistory get() = messages
    private val api = HttpApiRegistry.getConversationApi()
    private val apiSettings = ApiSettings(api.getName(), TosSettings.apiUrl, TosSettings.apiKey)
    private val llmService: LLMService = LLMService(api, apiSettings)

    var modelSettings: ModelSettings = ModelSettings(TosSettings.apiModel)

    /**
     * Sends [content] as a player message and requests an LLM response.
     * Invokes [beforeContinueAsPlayer].
     *
     * @param content The player message content
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChatAsPlayer(content: String) {
        if (content.isEmpty() || ended) return

        addPlayerMessage(content)
        beforeContinueAsPlayer?.invoke(content)
        continueChat()
    }

    /**
     * Sends [content] as a npc message and requests an LLM response.
     *
     * @param content The npc message content
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChatAsNPC(content: String) {
        if (content.isEmpty() || ended) return

        addNpcMessage(content)
        continueChat()
    }

    /**
     * Sends [content] as a system message and requests an LLM response.
     *
     * @param content The system message content
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChatAsSystem(content: String) {
        if (content.isEmpty() || ended) return

        addSystemMessage(content)
        continueChat()
    }

    /**
     * Requests the next LLM response and appends it to the chat history.
     * Invokes [afterChatResponse], [onProgress], and [onEnded] when the NPC
     * hangs up.
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChat() {
        if (chatHistory.isEmpty() || ended) return

        // Exhaustive by the compiler's grace: a new TurnResult variant won't
        // build until every caller has decided what it means for them.
        when (val result = llmService.send(this, modelSettings, onProgress)) {
            is TurnResult.Reply -> {
                chatHistory.addAll(result.messages)
                chatHistory.lastOrNull()?.let { afterChatResponse?.invoke(it.content) }
            }

            is TurnResult.Ended -> {
                chatHistory.addAll(result.messages)
                // The farewell is recorded as a regular NPC line, so the
                // persisted transcript (and any later summary) contains the
                // goodbye instead of cutting off mid-scene.
                addNpcMessage(result.farewell)
                ended = true
                onEnded?.invoke(result.farewell)
            }

            is TurnResult.Failed -> {
                // INFO messages are shown to the user but filtered out of
                // LLM requests, so a failure never contaminates the history.
                chatHistory.add(result.notice)
                afterChatResponse?.invoke(result.notice.content)
            }
        }
    }

    /**
     * Removes the last NPC message and re-requests a response.
     * No-op if the last message is from the player or the conversation ended.
     *
     * Note: This function performs a network call.
     * */
    suspend fun retryLastMessage() {
        if (ended) return
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

    fun addSystemMessage(content: String) {
        chatHistory.add(
            Message(
                role = ChatRoles.SYSTEM,
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
            TosCampaignNotifier.enqueue("Updating memory for ${npc.nameString}...", npc.nameString)
            when (val result = llmService.send(this@Chat, modelSettings, tools = emptyList())) {
                is TurnResult.Reply -> {
                    val summary = result.messages.last()
                    TosEveryFrameScriptQueue.add { npc.memory.set(TosMemoryKeys.MEMORY_STORAGE, summary.content) }
                    TosCampaignNotifier.enqueue("...finished memory update for ${npc.nameString}", npc.nameString)
                }

                else -> {
                    TosCampaignNotifier.enqueue("...failed memory update for ${npc.nameString}", npc.nameString)
                    TosInspector.error(
                        "Post-chat summary failed for ${npc.nameString}",
                        this::class,
                    )
                }
            }
        }
    }

    private fun endsWithNpcMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.ASSISTANT
    private fun endsWithPlayerMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.USER
}
