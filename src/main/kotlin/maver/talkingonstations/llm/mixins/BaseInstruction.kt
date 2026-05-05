package maver.talkingonstations.llm.mixins

import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Creates a list of factions and each one's standing to every other non-excluded faction.
 */
class BaseInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun canExecute(context: GameInfoInterface): Boolean {
        return true
    }

    override fun getText(gameInfo: GameInfoInterface): String = markdown {
        h2("Instructions")
        p("You are plugged in to the PC game Starsector (formerly Starfarer) to play the character of {{npc}} in the game. You are given the following instructions:")
        p("Develop the scene naturally. Stay within the lore of Starsector and the information given to you. Continue the ongoing roleplay as {{npc}}. In-Character from here on.")
    }
}
