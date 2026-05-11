package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section

/**
 * Interface for providing contextual information to LLM interactions.
 */
interface ContextMixinInterface {

    /**
     * Controls whether this provider should be included in the LLM context.
     *
     * When set to false, this provider will be filtered out during context generation.
     */
    var enabled: Boolean

    /**
     * Determines which section inside the context this mixin will be appended to.
     *
     * If multiple mixins append to the same section, they are concatenated in load order.
     */
    var section: Section

    /**
     * Produces formatted markdown for the LLM context, or null to opt out.
     *
     * @param gameInfo The current game information (player, npc, market, ...).
     * @return Formatted markdown, or null if this mixin has nothing to contribute.
     */
    fun render(gameInfo: GameInfoInterface): String?

    /**
     * Returns a unique identifier for this context provider.
     *
     * Used for the context provider console commands.
     * For example "TestMixin" for the class "TestMixin"
     */
    fun getKey(): String = this::class.java.simpleName
}
