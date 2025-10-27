package maver.talkingonstations.chat

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.command.InspectableInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.llm.LLMContext

class Chat(
    private val player: PersonAPI,
    private val npc: PersonAPI,
    private val market: MarketAPI,
    private val api: HttpApiInterface,
): LLMContext(ChatContext(player,npc,market)), InspectableInterface {
    var beforeContinueAsPlayer: ((message: String) -> Unit)? = null
    var afterChatResponse: ((message: String) -> Unit)? = null

    private val chatInstructions get() = getSystemMessagesMerged()
    private val chatHistory get() = getPublicMessages()
    private val enhancer: MessageEnhancer = MessageEnhancer(this)

    init {
        TosRegistry.register(this)
    }

    fun setModelSettings(modelSettings: ModelSettings) {
        return api.setModelSettings(modelSettings)
    }

    suspend fun continueChatAsPlayer(content: String) {
        if (content.isEmpty()) return

        addPlayerMessage(content)
        beforeContinueAsPlayer?.invoke(content)
        continueChat()
    }

    suspend fun continueChat() {
        if (chatHistory.isEmpty()) return

        val nextMessage: Message = withContext(Dispatchers.IO) {
            api.send( getChatInstructionsCopy(), getChatHistoryCopy())
        }

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
    fun getModelSettings(): ModelSettings = api.getModelSettings()
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
        var instructions = enhancer.replacePlayerPlaceholder(chatInstructions)
            instructions = enhancer.replaceNpcPlaceholder(instructions)
        return instructions
    }

    interface ChatContextInterface {
        val player: PersonAPI?
        val npc: PersonAPI?
        val market: MarketAPI?
    }

    data class ChatContext(
        override val player: PersonAPI? = null,
        override val npc: PersonAPI? = null,
        override val market: MarketAPI? = null
        ): ChatContextInterface
}