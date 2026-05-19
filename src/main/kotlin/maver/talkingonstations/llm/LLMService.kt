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
    ): Message {
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
                    if (response.role == ChatRoles.INFO) return response

                    // If there are no tool calls, the loop ends here in its first iteration
                    if (response.toolCalls.isEmpty() || toolBudgetExhausted) return finalReply(response)

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
                        val result = runTool(tools, call.name, call.arguments, context)
                        receivedMessages += Message(
                            role = ChatRoles.TOOL,
                            content = result.text,
                            toolCallId = call.id,
                        )
                    }
                }

            return Message(ChatRoles.INFO, "Please retry.")
        } catch (exception: HttpApiRequestException) {
            TosInspector.error("Request failed with status code ${exception.statusCode}", this::class)
            TosInspector.error("Response body: ${exception.responseBody}", this::class)

            return Message(
                ChatRoles.INFO,
                "Request failed with status code ${exception.statusCode}. Please retry or consult starsector.log",
            )
        } catch (exception: Exception) {
            TosInspector.error(exception.message, this::class, exception)

            return Message(
                ChatRoles.INFO,
                "Request failed with unknown exception. Please consult starsector.log",
            )
        }
    }

    private fun finalReply(message: Message): Message {
        if (message.content.isBlank()) return Message(ChatRoles.INFO, "The model returned nothing. Please retry.")
        return message.copy(content = message.content, toolCalls = emptyList())
    }


    private fun runTool(
        tools: List<ToolInterface>,
        name: String,
        arguments: Map<String, String>,
        context: LLMContext,
    ): ToolResult {
        val tool = tools.find { it.getName() == name }
            ?: return ToolResult("Unknown tool: $name")

        return try {
            tool.execute(arguments, context.gameInfo)
        } catch (exception: Exception) {
            TosInspector.error("Tool '$name' threw: ${exception.message}", this::class)
            ToolResult("Tool '$name' failed: ${exception.message}")
        }
    }
}
