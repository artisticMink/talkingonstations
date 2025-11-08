package maver.talkingonstations.llm

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Instruction
import maver.talkingonstations.llm.dto.Message

/**
 * Represents a LLM interaction in the most generic sense and can be extended
 * to represent a more specific context.
 *
 * @property gameInformation Starsector objects depending on the current game state
 * @property systemInstructions System-level instructions meant to steer LLM behavior
 * @property publicMessages The ongoing conversation
 * @property mixins Generators that add information regarding player, NPCs and markets, etc.
 * @property overrideMixins Manual set of mixins decoupled from settings; when not empty,
 * only these mixins will be used context for composition.
 */

open class LLMContext(private val gameInformation: GameInfoInterface) {
    private val mixins: List<ContextMixinInterface> = TosSettings.getContextMixins()
    private val overrideMixins: MutableList<ContextMixinInterface> = mutableListOf()

    protected val publicMessages: MutableList<Message> = mutableListOf()
    protected var systemInstructions: MutableList<Instruction> = mutableListOf()

    fun getSystemInstructionsMerged(withProvider: Boolean = true): String {
        val instructionsBlock = systemInstructions.joinToString("\n\n")
        val mixinBlock = when(overrideMixins.isEmpty()) {
            true -> mixinBlock(mixins)
            false -> mixinBlock(overrideMixins)
        }

        return when (withProvider) {
            true -> instructionsBlock + mixinBlock
            false -> instructionsBlock
        }
    }

    private fun mixinBlock(mixins: Iterable<ContextMixinInterface>): String {
        return mixins.mapNotNull { mixin ->
            mixin.takeIf { it.canExecute(gameInformation) }?.getText(gameInformation)
        }.joinToString("\n\n")
    }

    fun getPublicMessageCopy() = publicMessages.toList()

    fun addOverrideMixin(mixin: ContextMixinInterface) {
        overrideMixins.add(mixin)
    }

    fun addOverrideMixins(mixins: List<ContextMixinInterface>) {
        overrideMixins.addAll(mixins)
    }

    fun removeOverrideMixin(mixin: ContextMixinInterface) {
        overrideMixins.remove(mixin)
    }

    fun clearOverrideMixins() {
        overrideMixins.clear()
    }

    fun getOverrideMixinsCopy() = overrideMixins.toList()
}