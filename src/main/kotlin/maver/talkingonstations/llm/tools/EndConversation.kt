package maver.talkingonstations.llm.tools

import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolParamInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult

class EndConversation : ToolInterface {
    override var enabled = false
    override lateinit var description: String
    override lateinit var parameters: ToolParamInterface

    override fun getName(): String = "end_conversation"

    /**
     * Terminal tool: its `last_message` argument *is* the NPC's closing line,
     * so the loop stops here with no further model round trip.
     */
    override fun execute(params: Map<String, String>, game: GameInfoInterface): ToolResult =
        ToolResult(text = params["last_message"].orEmpty(), terminal = true)
}
