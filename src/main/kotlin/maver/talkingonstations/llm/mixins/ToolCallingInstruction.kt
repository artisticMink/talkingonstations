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
        p("To interact with the player and the in-game world of starsector, there might be various tools available which you can use at your own discretion.")
        p("Separate complex tool call tasks into multiple tool calls.")
    }
}
