package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.GameInfoInterface

/**
 * Interface for providing contextual information to LLM interactions.
 */
interface ContextProviderInterface {

    /**
     * Controls whether this provider should be included in the LLM context.
     *
     * When set to false, this provider will be filtered out during context generation
     */
    var enabled: Boolean

    /**
     * Determines if this provider can generate context text with the given game state.
     *
     * This method should check if all required game objects are present.
     *
     * @param context The current game state information
     * @return true if [getText] can be safely called with this context, false otherwise
     */
    fun canExecute(context: GameInfoInterface): Boolean

    /**
     * Generates formatted text to be included in the LLM context.
     *
     * The returned text should be a well-formatted string.
     *
     * The following placeholders are supported:
     * - {{player}}, the full player name
     * - {{npc}}, the full npc name
     *
     * @param gameInfo The current game information. PersonAPI, MarketAPI, etc.
     * @return A formatted string containing context information
     */
    fun getText(gameInfo: GameInfoInterface): String

    /**
     * Returns a unique identifier for this context provider.
     *
     * Used for the context provider console commands.
     * Can be overwritten if needed.
     *
     * @return A unique string identifier for this provider
     */
    fun getKey(): String = this::class.java.simpleName
}
