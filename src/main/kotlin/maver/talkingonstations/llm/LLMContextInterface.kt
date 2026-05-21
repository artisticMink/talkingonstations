package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.ConversationUiInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message

/**
 * Interface for a basic LLM context that can be passed to [LLMService.send]
 */
interface LLMContextInterface {
    /**
     * Information about the game state must be present
     */
    val gameInfo: GameInfoInterface

    /**
     * A reference to the calling ui can be passed if ui interaction from within
     * the context, i.e. Tool calling with UI manipulation, is needed.
     */
    val conversationUi: ConversationUiInterface?

    /**
     * A message chain must be accessible
     */
    val messages: MutableList<Message>

    /**
     * Must return a String with system instructions
     */
    fun getSystemBlock(): String

    /**
     * Must return a copy of messages
     */
    fun getMessagesCopy(): List<Message>
}