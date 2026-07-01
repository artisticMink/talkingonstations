package maver.talkingonstations.llm.mixins

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section

/**
 * Basic Guardrails to prevent M rated content
 */
class GuardrailsInstruction : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        if (!TosSettings.guardrailsEnabled) return null

        // ToDo: Basic safety guardrails that allow for grimdark Starsector content within limits.
        // Emit nothing until real guardrail text exists - shipping a "ToDo" note to the model
        // is worse than staying silent, so this opts out via null until then.
        return null
    }
}
