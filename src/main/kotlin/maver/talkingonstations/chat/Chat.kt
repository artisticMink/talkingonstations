package maver.talkingonstations.chat

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.command.InspectableInterface
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.llm.LLMContext
import maver.talkingonstations.llm.LLMService
import maver.talkingonstations.llm.dto.GameInfo
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings

/**
 * Represents a chat interaction between a player and an NPC, along with associated functionality
 * for managing messages, enhancing content, and submitting it to a client.
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
    private val enhancer: MessageEnhancer = MessageEnhancer(this)
    private val llmService: LLMService = LLMService(HttpApiRegistry.getSelectedApi())

    init {
        TosRegistry.register(this)

        val instruction = llmService.getModelSettings().system
        if (instruction.isNotEmpty()) this.systemInstructions["main"] = instruction
    }

    fun setModelSettings(modelSettings: ModelSettings) {
        return llmService.setModelSettings(modelSettings)
    }

    /**
     * Continues the conversation as the player.
     *
     * - Adds the given string as a new player message and invokes
     *    the associated callback function.
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
     * Continues the ongoing chat session by sending the current chat context
     * and instructions to the API, receiving a response message, and appending
     * it to the chat history. Applies any necessary transformations or enhancements
     * to the message.
     *
     * - If the chat history is empty, it returns without performing any action,
     *   else appends the last message received to the conversation.
     * - Optionally invokes a callback function, if defined, with the content of
     *   the last chat message.
     *
     * Note: This function performs a network call.
     */
    suspend fun continueChat() {
        if (chatHistory.isEmpty()) return

        val nextMessage: Message = llmService.send(this)

        enhancer.revertMessage(nextMessage)
        chatHistory.add(nextMessage)
        afterChatResponse?.invoke(chatHistory.last().content)
    }

    suspend fun retryLastMessage() {
        if (!endsWithPlayerMessage()) {
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
    fun getModelSettings(): ModelSettings = llmService.getModelSettings()
    fun getPlayer(): PersonAPI = player
    fun getNpc(): PersonAPI = npc

    private fun endsWithNpcMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.ASSISTANT
    private fun endsWithPlayerMessage() = chatHistory.isNotEmpty() && chatHistory.last().role == ChatRoles.USER

    override fun canInspect(): List<String> {
        return listOf("chatHistory", "instructions")
    }

    override fun inspect(item: String): String {
        return when (item) {
            "chatHistory" -> getChatHistoryCopy().joinToString("\n\n")
            "instructions" -> getChatInstructionsCopy()
            else -> return ""
        }
    }

    private fun getChatHistoryCopy(): List<Message> {
        val chatHistoryCopy = chatHistory.filter { it.role == ChatRoles.USER || it.role == ChatRoles.ASSISTANT }.map { it.copy() }
        enhancer.prepareAll(chatHistoryCopy)
        return chatHistoryCopy
    }

    private fun getChatInstructionsCopy(): String {
        var instructions = enhancer.replacePlayerPlaceholder(getSystemInstructionsMerged())
        instructions = enhancer.replaceNpcPlaceholder(instructions)
        return instructions
    }
}