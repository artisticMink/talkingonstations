package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.MixinUtils
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

class BarInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        val npc = gameInfo.npc ?: return null
        val market = gameInfo.market ?: return null
        if (!npc.memoryWithoutUpdate.getBoolean(TosMemoryKeys.IS_BAR_PERSON)) return null

        return markdown {
            p("The name of your character is ${npc.name.fullName}, a ${MixinUtils.gender(npc)}. You belong to the ${MixinUtils.faction(npc)} faction, currently located in a Bar on ${MixinUtils.marketName(market)}.")
        }
    }
}
