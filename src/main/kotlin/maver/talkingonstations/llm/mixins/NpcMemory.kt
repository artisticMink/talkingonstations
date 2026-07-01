package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Includes the summary of previous character interactions from npc memory
 */
class NpcMemory: ContextMixinInterface {
    override var enabled: Boolean = true
    override var section: Section = Section.SCENE

    override fun render(gameInfo: GameInfoInterface): String? {
        val summary = gameInfo.npc?.memory?.get(TosMemoryKeys.MEMORY_STORAGE)
        if (summary != null &&
            summary is String &&
            summary.isNotEmpty()
        ) {
            return markdown {
                h2("History")
                p("{{npc}} has an established history with {{player}}. A summary of their most recent interaction follows.")
                p(summary)
            }
        }

        return null
    }
}