package maver.talkingonstations.llm

import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.Message
import java.util.*

/**
 * LLMContext can be extended to represent a more specific context.
 *
 * Represents a LLM interaction in the most generic sense.
 *
 * @property gameInformation Starsector objects depending on the current game state
 * @property systemInstructions System-level instructions meant to steer LLM behavior
 * @property publicMessages The ongoing conversation
 * @property mixins Generators that add information regarding player, NPCs and markets, etc.
 */

open class LLMContext(private val gameInformation: GameInfoInterface) {
    private val mixins: List<ContextMixinInterface> = TosSettings.getContextMixins()

    protected var systemInstructions: SortedMap<String, String> = sortedMapOf()
    protected val publicMessages: MutableList<Message> = mutableListOf()

    fun getSystemInstructionsMerged(withProvider: Boolean = true): String {
        val instructionsBlock = systemInstructions.values.joinToString("\n\n")
        val providerBlock = mixins
            .filter { it.enabled }
            .joinToString("\n\n") { it.takeIf { it.canExecute(gameInformation) }?.getText(gameInformation) as String }

        return when (withProvider) {
            true -> instructionsBlock + providerBlock
            false -> instructionsBlock
        }
    }

    fun getPublicMessageCopy() = publicMessages.map { it.copy() }
}