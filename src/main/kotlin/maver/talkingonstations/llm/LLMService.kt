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
 * Only returns the last message of a message chain that involved tool calling.
 */
class LLMService(
    private val client: HttpApiInterface
) {
    private companion object {
        const val MAX_TOOL_ITERATIONS = 5
    }

    suspend fun send(context: LLMContext, model: ModelSettings, onProgress: (Message) -> Unit = {}): Message {
        val tools = TosRegistry.getTools().filter { it.enabled }
        val history = context.getPublicMessageCopy().filter { message -> message.role !== ChatRoles.INFO }
        val receivedMessages = mutableListOf<Message>()

        try {
            repeat(MAX_TOOL_ITERATIONS) { iteration ->
                if (Global.getSettings().isDevMode) TosInspector.debug("Starting send round-triop - iteration $iteration", this::class)
                val response = client.send(
                    instructions = context.getSystemInstructionsMerged(),
                    messages = history + receivedMessages,
                    model = model,
                    tools = if (!TosSettings.isToolCallingEnabled || iteration == MAX_TOOL_ITERATIONS - 1) emptyList() else tools,
                )

                receivedMessages += response

                if (response.role == ChatRoles.INFO) return receivedMessages.last()

                // No requested tool calls, treat it as regular request
                if (response.toolCalls.isEmpty()) return receivedMessages.last()

                for (call in response.toolCalls) {
                    if (Global.getSettings().isDevMode) TosInspector.debug("Model requested tool: ${call.name}", this::class)
                    // Propagate tool progress back to user
                    if (TosSettings.showToolCallingIndicator)
                        onProgress(Message(
                            role = ChatRoles.INFO,
                            content = "{{npc}} is using ${call.name}",
                            toolCallId = call.id
                        ))

                    // Actual hook into Starsector code
                    val result = runTool(tools, call.name, call.arguments, context)
                    receivedMessages += Message(
                        role = ChatRoles.TOOL,
                        content = result.text,
                        toolCallId = call.id,
                    )

                    // A tool call that results in the ended the message chain forcefully.
                    // For example ending the conversation.
                    if (result.terminal) return receivedMessages.last()
                }
            }

            return receivedMessages.last()
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
