package maver.talkingonstations.llm.tools

import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ParamType
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolParameter
import maver.talkingonstations.llm.dto.ToolResult

/**
 * Let's the model end the conversation.
 */
class EndConversation : ToolInterface {
    override val name = "end_conversation"
    override val description = "End the conversation. Equivalent to hanging up or leaving the room."
    override val isTransient = true

    // Shouldn't bounce back to the model just because it forgot the goodbye line.
    override val parameters = listOf(
        ToolParameter(
            name = "last_message",
            type = ParamType.STRING,
            description = "The last message the player will see before the scene ends.",
            required = false,
        )
    )

    override fun execute(args: ToolArguments, gameInfo: GameInfoInterface): ToolResult =
        ToolResult(
            text = args.optString("last_message") ?: "Only static remains...",
            forceEnd = true,
        )
}
