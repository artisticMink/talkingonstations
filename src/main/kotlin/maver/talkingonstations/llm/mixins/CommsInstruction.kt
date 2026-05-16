package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

class CommsInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        val npc = gameInfo.npc ?: return null
        if (!npc.memoryWithoutUpdate.getBoolean(TosMemoryKeys.IS_MARKET_PERSON)) return null

        return markdown {
            p("In this scene your character, {{npc}}, is connected with the {{player}}'s fleet currently in orbit via a comms relay. A sophisticated sci-fi zoom call. ")
        }
    }
}
