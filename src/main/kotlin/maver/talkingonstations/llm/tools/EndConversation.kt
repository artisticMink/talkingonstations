package maver.talkingonstations.llm.tools

import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolParamInterface
import maver.talkingonstations.llm.dto.ConversationUi
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult

class EndConversation() : ToolInterface {
    override val isTransient: Boolean = true
    override var enabled = false
    override lateinit var description: String
    override lateinit var parameters: ToolParamInterface

    override fun getName(): String = "end_conversation"

    override fun execute(params: Map<String, String>, gameInfo: GameInfoInterface, conversationUi: ConversationUi?): ToolResult {
        conversationUi?.forceEnd(params["last_message"] ?: "Only static noise remains....")
        return ToolResult("Connection ended.")
    }
}
