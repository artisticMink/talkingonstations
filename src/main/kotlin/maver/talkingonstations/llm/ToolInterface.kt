package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.ConversationUi
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult

interface ToolInterface {
    val isTransient: Boolean

    var enabled: Boolean
    var description: String
    var parameters: ToolParamInterface

    fun getName(): String
    fun execute(params: Map<String, String>, gameInfo: GameInfoInterface, conversationUi: ConversationUi?): ToolResult
    fun getKey(): String = this::class.java.simpleName
}
