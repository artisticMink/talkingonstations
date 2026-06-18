package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolParameter
import maver.talkingonstations.llm.dto.ToolResult

/**
 * @see [maver.talkingonstations.llm.tools.CheckLocalMarket]
 * @see /data/config/tos/Tools.csv
 * @see [maver.talkingonstations.httpapi.body.ToolCallDefinition.fromTool]
 */
interface ToolInterface {
    // Must be snake_case
    val name: String

    // Brief, one to two sentences max.
    val description: String

    val parameters: List<ToolParameter>

    /**
     *  Transient call/result pairs are not persisted in the chat history.
     *
     *  When in doubt, a tool should be transient.
     */
    val isTransient: Boolean

    /**
     * Dangerous tools directly affect the player
     *
     * When a tool places a bounty or deducts credits it is 'dangerous'
     */
    val isDangerous: Boolean get() = false

    fun execute(args: ToolArguments, gameInfo: GameInfoInterface): ToolResult

    fun getKey(): String = this::class.java.simpleName
}
