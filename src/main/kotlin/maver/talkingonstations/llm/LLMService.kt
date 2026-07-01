package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import kotlinx.coroutines.CancellationException
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosSettings
import maver.talkingonstations.TosStrings
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.HttpApiInterface
import maver.talkingonstations.httpapi.exception.HttpApiRequestException
import maver.talkingonstations.llm.dto.ApiSettings
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.llm.dto.ToolArgumentException
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolCall
import maver.talkingonstations.llm.dto.ToolResult
import maver.talkingonstations.llm.dto.TurnResult

/**
 * Runs one conversational turn against an [HttpApiInterface] client.
 * Originally a wrapper for the client, now also holds tool calling. Oh well.
 *
 * request -> response -> wants tool?  no  -> Reply
 *                        yes -> run tool -> ends conversation? yes -> end
 *                                           no -> append result -> repeat
 */
class LLMService(
    private val client: HttpApiInterface,
    private val apiSettings: ApiSettings,
) {
    private companion object {
        const val MAX_TOOL_ITERATIONS = 5
    }

    /**
     * Passes the given context to the HTTP client.
     * Makes sure messages are sanitized.
     *
     * [ChatRoles.INFO] are not passed to the client.
     * Tool calls and result will not be returned.
     */
    suspend fun send(
        context: LLMContextInterface,
        model: ModelSettings,
        onProgress: (Message) -> Unit = {},
        tools: List<ToolInterface> = TosRegistry.getTools(),
    ): TurnResult {
        // Info messages are never passed down
        val history = context.getMessagesCopy().filter { message -> message.role !== ChatRoles.INFO }
        val receivedMessages = mutableListOf<Message>()

        val effectiveTools = when {
            !TosSettings.isToolCallingEnabled || !client.supportsToolCalling -> emptyList()
            else -> tools.filter { !it.isDangerous || TosSettings.isDangerousToolCallingEnabled }
        }

        val instructions = context.getSystemBlock()

        try {
            val toolLoopLimit = if (effectiveTools.isNotEmpty()) MAX_TOOL_ITERATIONS else 1
            repeat(toolLoopLimit) { iteration ->
                // Most cases will be faulty tool calls.
                // ToDo: Perhaps lock tool calling for session after x faulty attempts. Incapable model etc.
                val toolBudgetExhausted = iteration == toolLoopLimit - 1

                val response = client.send(
                    apiSettings = apiSettings,
                    instructions = instructions,
                    messages = history + receivedMessages,
                    model = model,
                    tools = if (toolBudgetExhausted) emptyList() else effectiveTools,
                )

                // This is more of a convention than an actual error state
                if (response.role == ChatRoles.INFO) return TurnResult.Failed(response)

                receivedMessages += response

                if (response.toolCalls.isEmpty() || toolBudgetExhausted)
                    return TurnResult.Reply(stripTransient(receivedMessages))

                for (call in response.toolCalls) {
                    if (Global.getSettings().isDevMode) TosInspector.debug(
                        "Model requested tool: ${call.name}",
                        this::class
                    )

                    // Propagate tool progress back to ui
                    if (TosSettings.showToolCallingIndicator)
                        onProgress(
                            Message(
                                role = ChatRoles.INFO,
                                content = "{{npc}} is requesting ${call.name}",
                                toolCallId = call.id,
                            )
                        )

                    val tool = effectiveTools.find { it.name == call.name }
                    if (tool == null) {
                        receivedMessages += Message(
                            role = ChatRoles.TOOL,
                            content = "Tool not found: ${call.name}",
                            toolCallId = call.id,
                        )
                        continue
                    }

                    val result = runTool(tool, call, context)

                    // Edge-Case should the model hang up via tool use
                    if (result.forceEnd) return TurnResult.Ended(
                        messages = stripTransient(receivedMessages),
                        farewell = result.text,
                    )

                    receivedMessages += Message(
                        role = ChatRoles.TOOL,
                        content = result.text,
                        toolCallId = call.id,
                        isTransient = tool.isTransient,
                    )
                }
            }

            // Should never be hit
            return TurnResult.Failed(Message(ChatRoles.INFO, "No reply was produced. Please retry."))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpApiRequestException) {
            // ToDo: maybe skip and do a more lore-friendly disconnect
            TosInspector.error("Request failed with status code ${exception.statusCode}", this::class)
            TosInspector.error("Response body: ${exception.responseBody}", this::class)

            return TurnResult.Failed(
                Message(
                    ChatRoles.INFO,
                    "Request failed with status code ${exception.statusCode}. Please retry or consult starsector.log",
                )
            )
        } catch (exception: Exception) {
            TosInspector.error(exception.message, this::class, exception)

            return TurnResult.Failed(
                Message(
                    ChatRoles.INFO,
                    "Request failed with unknown exception. Please consult starsector.log",
                )
            )
        }
    }

    /**
     * Sanitizes the message list by stripping tool calls
     */
    private fun stripTransient(messages: List<Message>): List<Message> {
        val transientIds = messages
            .filter { it.role == ChatRoles.TOOL && it.isTransient }
            .mapNotNull { it.toolCallId }
            .toSet()

        val persistentIds = messages
            .filter { it.role == ChatRoles.TOOL && !it.isTransient }
            .mapNotNull { it.toolCallId }
            .toSet()

        return messages.mapNotNull { msg ->
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
     * Runs a single tool.
     */
    private fun runTool(
        tool: ToolInterface,
        call: ToolCall,
        context: LLMContextInterface,
    ): ToolResult {
        val arguments = try {
            ToolArguments.parse(call.arguments, tool.parameters)
        } catch (exception: ToolArgumentException) {
            TosInspector.error(
                "Tool '${tool.name}' received bad arguments ${call.arguments}: ${exception.message}",
                this::class
            )
            return ToolResult("Invalid arguments for ${tool.name}: ${exception.message}")
        }

        return try {
            tool.execute(arguments, context.gameInfo)
        } catch (exception: Exception) {
            val text = "'${tool.name}' failed with : ${exception.message}"
            TosInspector.error(text, this::class)
            ToolResult(text)
        }
    }
}
