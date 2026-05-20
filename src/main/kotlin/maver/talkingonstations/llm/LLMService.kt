package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosSettings
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.httpapi.exception.HttpApiRequestException
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.llm.dto.ToolResult

/**
 * Initially a wrapper class for a client implementing [HttpApiInterface].
 * Now also contains the tool-calling loop, oh well.
 *
 * request -> response -> want tool?    yes -> run tool -> append result -> repeat
 *                                      no  -> return message
 *
 */
class LLMService(
    private val client: HttpApiInterface
) {
    private companion object {
        const val MAX_TOOL_ITERATIONS = 5
    }

    suspend fun send(
        context: LLMContext,
        model: ModelSettings,
        onProgress: (Message) -> Unit = {}
    ): List<Message> {
        val tools = TosRegistry.getTools().filter { it.enabled }
        val history = context.getPublicMessageCopy().filter { message -> message.role !== ChatRoles.INFO }
        val receivedMessages = mutableListOf<Message>()

        try {
            repeat(MAX_TOOL_ITERATIONS) { iteration ->
                val toolBudgetExhausted = iteration == MAX_TOOL_ITERATIONS - 1

                if (Global.getSettings().isDevMode) TosInspector.debug(
                    "Answering - iteration ${iteration + 1}",
                    this::class
                )

                val response = client.send(
                    instructions = context.getSystemInstructionsMerged(),
                    messages = history + receivedMessages,
                    model = model,
                    tools = if (!TosSettings.isToolCallingEnabled || toolBudgetExhausted) emptyList() else tools,
                )

                receivedMessages += response

                // Soft error from HTTP API
                if (response.role == ChatRoles.INFO) return returnNonTransient(receivedMessages)

                // If there are no tool calls, the loop ends here in its first iteration
                if (response.toolCalls.isEmpty() || toolBudgetExhausted) return returnNonTransient(receivedMessages)

                for (call in response.toolCalls) {
                    if (Global.getSettings().isDevMode) TosInspector.debug(
                        "Model requested tool: ${call.name}",
                        this::class
                    )

                    // Propagate tool progress back to user
                    if (TosSettings.showToolCallingIndicator)
                        onProgress(
                            Message(
                                role = ChatRoles.INFO,
                                content = "{{npc}} is requesting ${call.name}",
                                toolCallId = call.id,
                            )
                        )

                    // Actual hook into Starsector code
                    val tool = tools.find { it.getName() == call.name }
                    if (tool == null) {
                        receivedMessages += Message(
                            role = ChatRoles.TOOL,
                            content = "Tool not found: ${call.name}",
                            toolCallId = call.id,
                        )
                        continue
                    }

                    val result = runTool(tool, call.name, call.arguments, context)

                    // Tools can be one-off actions that end a conversation.
                    //if (result.forceEnd) return returnNonTransient(receivedMessages)
                    if (result.forceEnd) return emptyList()

                    receivedMessages += Message(
                        role = ChatRoles.TOOL,
                        content = result.text,
                        toolCallId = call.id,
                        isTransient = tool.isTransient
                    )
                }
            }

            return listOf(Message(ChatRoles.INFO, "Please retry."))
        } catch (exception: HttpApiRequestException) {
            TosInspector.error("Request failed with status code ${exception.statusCode}", this::class)
            TosInspector.error("Response body: ${exception.responseBody}", this::class)

            return listOf(
                Message(
                    ChatRoles.INFO,
                    "Request failed with status code ${exception.statusCode}. Please retry or consult starsector.log",
                )
            )
        } catch (exception: Exception) {
            TosInspector.error(exception.message, this::class, exception)

            return listOf(
                Message(
                    ChatRoles.INFO,
                    "Request failed with unknown exception. Please consult starsector.log",
                )
            )
        }
    }

    private fun returnNonTransient(receivedMessages: List<Message>): List<Message> {
        val transientIds = receivedMessages
            .filter { it.role == ChatRoles.TOOL && it.isTransient }
            .map { it.toolCallId }
            .toSet()

        return receivedMessages.mapNotNull { msg ->
            when {
                msg.role == ChatRoles.TOOL && msg.toolCallId in transientIds -> null
                msg.toolCalls.isNotEmpty() -> {
                    val answeredIds = receivedMessages
                        .filter { it.role == ChatRoles.TOOL && !it.isTransient }
                        .mapNotNull { it.toolCallId }
                        .toSet()

                    val kept = msg.toolCalls.filter { it.id in answeredIds }

                    if (kept.isEmpty() && msg.content.isBlank()) null
                    else msg.copy(toolCalls = kept)
                }

                else -> msg
            }
        }
    }


    private fun runTool(
        tool: ToolInterface,
        name: String,
        arguments: Map<String, String>,
        context: LLMContext,
    ): ToolResult {
        return try {
            tool.execute(arguments, context.gameInfo, context.conversationUi)
        } catch (exception: Exception) {
            TosInspector.error("Tool '$name' threw: ${exception.message}", this::class)
            ToolResult("Tool '$name' failed: ${exception.message}")
        }
    }
}
