package maver.talkingonstations.llm.mixins

import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

class BaseInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String = markdown {
        h2("Instructions")
        p("You are plugged in to the PC game Starsector (formerly Starfarer) to impersonate a character within the game world in a multi-turn interactive roleplay with the player.")
    }
}
