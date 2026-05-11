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
     * Template variables substituted into system instructions and public
     * message content.
     */
    protected open fun getTemplateVariables(): Map<String, String> = buildMap {
        gameInformation.player?.let { put("{{player}}", it.name.fullName) }
        gameInformation.npc?.let { put("{{npc}}", it.name.fullName) }
    }

    private fun applyTemplateVariables(text: String, vars: Map<String, String>): String =
        vars.entries.fold(text) { acc, (key, value) -> acc.replace(key, value) }

    /**
     * Merges all system instructions into a single string.
     * @param withMixins If true, appends context mixin output after the instructions.
     */
    fun getSystemInstructionsMerged(withMixins: Boolean = true): String {
        val merged = markdown {
            h1("BASE INSTRUCTIONS")
            +renderSection(Section.INSTRUCTION)
            h1("CHARACTER SHEETS")
            +renderSection(Section.PERSON)
            h1("CURRENT LOCATION")
            +renderSection(Section.MARKET)
            h1("STATE OF THE PERSEAN SECTOR")
            +renderSection(Section.SECTOR)
        }

        return applyTemplateVariables(merged, getTemplateVariables())
    }

    private fun renderSection(section: Section): String =
        mixins
            .filter { it.section == section }
            .mapNotNull { it.render(gameInformation) }
            .joinToString("\n")

    fun getPublicMessageCopy(): List<Message> {
        val vars = getTemplateVariables()
        return publicMessages.map { it.copy(content = applyTemplateVariables(it.content, vars)) }
    }
}