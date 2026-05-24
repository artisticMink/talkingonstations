package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

class ToolCallingInstruction : ContextMixinInterface {
    override var enabled: Boolean = TosSettings.isToolCallingEnabled
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String = markdown {
        h2("Tool Calling")
        p("You can use tool calls to fetch additional information or initiate narrative events you deem appropriate.")
        p("Separate complex tool call tasks into multiple sequential tool calls.")
    }
}
