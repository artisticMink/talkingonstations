package maver.talkingonstations.llm.tools

import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolParamInterface

class EndConversation: ToolInterface {
    override var enabled = false
    override lateinit var description: String
    override lateinit var parameters: ToolParamInterface

    override fun getName(): String = "end_conversation"

    override fun execute(params: Map<String,String>): Boolean {
        return true
    }

}