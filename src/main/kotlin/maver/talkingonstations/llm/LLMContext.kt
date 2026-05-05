package maver.talkingonstations.llm

import maver.talkingonstations.TosRegistry
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.enum.Section

/**
 * Represents a generic LLM interaction context that manages system instructions,
 * conversation messages, and context mixins. Extend to specialize for a
 * particular interaction type (e.g., NPC chat).
 */

open class LLMContext(private val gameInformation: GameInfoInterface) {
    private val mixins: List<ContextMixinInterface> = TosRegistry.getContextMixins()

    protected val publicMessages: MutableList<Message> = mutableListOf()

    /**
     * Merges all system instructions into a single string.
     * @param withMixins If true, appends context mixin output after the instructions.
     */
    fun getSystemInstructionsMerged(withMixins: Boolean = true): String {
        return markdown {
            h1("SYSTEM INSTRUCTION")
            +mixins.filter { mixin -> mixin.section == Section.INSTRUCTION && mixin.canExecute(gameInformation) }.joinToString("\n\n")
            h1("CHARACTER SHEETS")
            +mixins.filter { mixin -> mixin.section == Section.PERSON && mixin.canExecute(gameInformation) }.joinToString("\n\n")
            h1("STATE OF THE PERSEAN SECTOR")
            +mixins.filter { mixin -> mixin.section == Section.SECTOR && mixin.canExecute(gameInformation) }.joinToString("\n\n")
            h1("CURRENT LOCATION")
            +mixins.filter { mixin -> mixin.section == Section.MARKET && mixin.canExecute(gameInformation) }.joinToString("\n\n")
        }
    }

    fun getPublicMessageCopy() = publicMessages.toList()
}