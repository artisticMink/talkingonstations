package maver.talkingonstations.llm

import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult

interface ToolInterface {
    var enabled: Boolean
    var description: String
    var parameters: ToolParamInterface

    fun getName(): String

    /**
     * Runs the tool.
     *
     * @param params Call arguments parsed from the model — v1 keeps every value a string.
     * @param game Live game state, so a tool can read/act on the player, npc or market.
     * @return text the model reads as the tool result, plus whether the loop should stop.
     */
    fun execute(params: Map<String, String>, game: GameInfoInterface): ToolResult

    fun getKey(): String = this::class.java.simpleName
}
