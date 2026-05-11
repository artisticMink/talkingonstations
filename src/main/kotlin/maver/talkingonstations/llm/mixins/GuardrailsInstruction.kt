package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Basic Guardrails to prevent M rated content
 */
class GuardrailsInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        if (!TosSettings.guardrailsEnabled) return null

        return markdown {
            h2("Guardrails")
            p("ToDo: Basic safety guardrails that allow for grimdark Starsector content within limits.")
        }
    }
}
