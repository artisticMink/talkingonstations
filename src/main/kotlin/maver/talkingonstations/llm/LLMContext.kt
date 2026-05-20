package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.dto.ConversationUi
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.enum.Section

/**
 * Represents a generic LLM interaction context that manages system instructions,
 * conversation messages, and context mixins. Extend to specialize for a
 * particular interaction type.
 */

open class LLMContext(override val gameInfo: GameInfoInterface, override val conversationUi: ConversationUi? = null): LLMContextInterface {
    private val mixins: List<ContextMixinInterface> = TosRegistry.getContextMixins()

    protected val publicMessages: MutableList<Message> = mutableListOf()

    /**
     * Template variables substituted into system instructions and public
     * message content.
     */
    protected fun getTemplateVariables(): Map<String, String> = buildMap {
        gameInfo.player?.let { put("{{player}}", it.name.fullName) }
        gameInfo.npc?.let { put("{{npc}}", it.name.fullName) }
        put("{{summaryBudget}}", TosSettings.characterMemoryBudget.toString())
        put("{{sectorDate}}", Global.getSector().clock.cycleString)
    }

    private fun applyTemplateVariables(text: String, vars: Map<String, String>): String =
        vars.entries.fold(text) { acc, (key, value) -> acc.replace(key, value) }

    /**
     * Merges all system instructions into a single string.
     */
    override fun getSystemInstructionsMerged(): String {
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
            .mapNotNull { it.render(gameInfo) }
            .joinToString("\n")

    fun getPublicMessageCopy(): List<Message> {
        val vars = getTemplateVariables()
        return publicMessages.map { it.copy(content = applyTemplateVariables(it.content, vars)) }
    }
}