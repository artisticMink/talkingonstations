package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosSettings
import maver.talkingonstations.TosStrings
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.enum.Section

/**
 * A generic LLM interaction context that manages system instructions,
 * conversation messages, and context mixins.
 */
open class LLMContext(override val gameInfo: GameInfoInterface): LLMContextInterface {
    override val messages: MutableList<Message> = mutableListOf()

    private val mixins: MutableList<ContextMixinInterface> = TosRegistry.getContextMixins().toMutableList()

    /**
     * Mixins should always be loaded through the [ContextMixinLoader] if possible,
     * to take advantage of mod load order.
     */
    fun addMixin(mixin: ContextMixinInterface) = mixins.add(mixin)

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

    override fun getMessagesCopy(): List<Message> {
        val vars = getTemplateVariables()
        return messages.map { it.copy(content = applyTemplateVariables(it.content, vars)) }
    }

    override fun getSystemBlock(): String {
        val merged = markdown {
            p(TosStrings.Prompt.PREAMBLE)
            h1("Instructions")
            +renderSection(Section.INSTRUCTION)
            if (TosSettings.isToolCallingEnabled) p(TosStrings.Prompt.TOOL_CALLING)
            h1("Reference Dossier")
            +renderSection(Section.CHARACTERS)
            +renderSection(Section.PLAYER)
            +renderSection(Section.MARKET)
            +renderSection(Section.SECTOR)
            +renderSection(Section.SCENE)
        }

        return applyTemplateVariables(merged, getTemplateVariables())
    }

    private fun renderSection(section: Section): String =
        mixins
            .filter { it.section == section }
            .mapNotNull { it.render(gameInfo) }
            .joinToString("\n")
}