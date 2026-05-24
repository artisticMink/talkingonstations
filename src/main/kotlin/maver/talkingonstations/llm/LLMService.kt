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
 * request -> response -> want tool?    yes -> run tool         -> force end?  no  -> append result -> repeat
 *                                      no  -> return message                  yes -> return message
 *
 * Tools might either be persistent or transient. Transient call-result pairs are not returned to the context.
 * Both persistent and transient tools may exist in the same temporary chain.
 */
class LLMService(
    private val client: HttpApiInterface
) {
    private companion object {
        const val MAX_TOOL_ITERATIONS = 5
    }

    suspend fun send(
        context: LLMContextInterface,
        model: ModelSettings,
        onProgress: (Message) -> Unit = {},
        tools: List<ToolInterface> = TosRegistry.getTools().filter { it.enabled }
    ): List<Message> {
        val history = context.getMessagesCopy().filter { message -> message.role !== ChatRoles.INFO }
        val receivedMessages = mutableListOf<Message>()

        try {
            val toolLoopLimit = if (tools.isNotEmpty()) MAX_TOOL_ITERATIONS else 1
            repeat(toolLoopLimit) { iteration ->
                val toolBudgetExhausted = iteration == toolLoopLimit -1

                if (Global.getSettings().isDevMode) TosInspector.debug(
                    "LLMService.send - iteration ${iteration + 1}",
                    this::class
                )

                val response = client.send(
                    instructions = context.getSystemBlock(),
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

                    val tool = tools.find { it.getName() == call.name }
                    if (tool == null) {
                        receivedMessages += Message(
                            role = ChatRoles.TOOL,
                            content = "Tool not found: ${call.name}",
                            toolCallId = call.id,
                        )
                        continue
                    }

                    // Actual hook into Starsector code
                    val result = runTool(tool, call.arguments, context)

                    // Tools can be one-off actions that end a conversation.
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

    /**
     * Returns the given message chain without transient tool calls or their response.
     */
    private fun returnNonTransient(messageChain: List<Message>): List<Message> {
        val transientIds = messageChain
            .filter { it.role == ChatRoles.TOOL && it.isTransient }
            .mapNotNull { it.toolCallId }
            .toSet()

        val persistentIds = messageChain
            .filter { it.role == ChatRoles.TOOL && !it.isTransient }
            .mapNotNull { it.toolCallId }
            .toSet()

        return messageChain.mapNotNull { msg ->
            when {
                msg.role == ChatRoles.TOOL && msg.toolCallId in transientIds -> null
                msg.toolCalls.isNotEmpty() -> {
                    val kept = msg.toolCalls.filter { it.id in persistentIds }

                    if (kept.isEmpty() && msg.content.isBlank()) null
                    else msg.copy(toolCalls = kept)
                }

                else -> msg
            }
        }
    }

    /**
     * Run a single tool with the given context
     *
     * @param tool The tool to be run
     * @param arguments A map of arguments for the provided tool
     * @param context The current [LLMContext]
     */
    private fun runTool(
        tool: ToolInterface,
        arguments: Map<String, String>,
        context: LLMContextInterface,
    ): ToolResult {
        return try {
            tool.execute(arguments, context.gameInfo, context.conversationUi)
        } catch (exception: Exception) {
            TosInspector.error("Tool '${tool.getName()}' threw: ${exception.message}", this::class)
            ToolResult("Tool '${tool.getName()}' failed with: ${exception.message}")
        }
    }
}
