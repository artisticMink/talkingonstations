package maver.talkingonstations.llm

import maver.talkingonstations.TosSettings
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.llm.dto.Message
import java.util.*

open class LLMContext(
    private val chatContext: Chat.ChatContextInterface
) {
    private var systemInstructions: SortedMap<String, String> = sortedMapOf()
    private val publicMessages: MutableList<Message> = mutableListOf()
    private val contextProvider: List<ContextProviderInterface> = TosSettings.getContextProvider()

    fun getSystemInstructions() = systemInstructions
    fun getSystemMessagesMerged() =
        systemInstructions.values.joinToString("\n\n") +
        contextProvider.filter { it.enabled }.joinToString { it.takeIf { it.canExecute(chatContext) }?.getText(chatContext) + "\n\n" }

    fun getPublicMessages() = publicMessages
}