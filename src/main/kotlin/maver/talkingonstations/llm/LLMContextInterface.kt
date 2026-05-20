package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.ConversationUiInterface
import maver.talkingonstations.llm.dto.GameInfoInterface

interface LLMContextInterface {
    val gameInfo: GameInfoInterface
    val conversationUi: ConversationUiInterface?
    fun getSystemInstructionsMerged(): String
}