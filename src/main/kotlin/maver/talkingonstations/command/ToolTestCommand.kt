package maver.talkingonstations.command

import com.fs.starfarer.api.Global
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.dto.GameInfo
import maver.talkingonstations.llm.dto.ToolArgumentException
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolResult
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ToolTestCommand : BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        if (p0.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX

        // ToDo: Breaks param strings with whitespaces. Oh well.
        val parts = p0.trim().split(Regex("\\s+"))
        if (parts.size < 2) return BaseCommand.CommandResult.BAD_SYNTAX

        val toolName = parts[0]
        val parameterString = parts[1]
        val marketId = parts.getOrNull(2) ?: DEFAULT_MARKET_ID

        val tool: ToolInterface? = TosRegistry.getTools()
            .find { it.name == toolName || it.getKey() == toolName }
        if (tool == null) {
            Console.showMessage("Tool '$toolName' not found. Use tos.tools.list to see loaded tools.")
            return BaseCommand.CommandResult.SUCCESS
        }

        val market = Global.getSector().economy.getMarket(marketId)
        if (market == null) {
            Console.showMessage("Market $marketId not found")
            return BaseCommand.CommandResult.SUCCESS
        }

        val game = GameInfo(
            player = Global.getSector().playerPerson,
            npc = null,
            market = market,
        )

        val result: ToolResult = try {
            // Same parse + validate path as production (LLMService.runTool), so
            // a passing console test means a real call would pass too.
            val args = ToolArguments.parse(parameterString, tool.parameters)
            tool.execute(args, game)
        } catch (exception: ToolArgumentException) {
            Console.showMessage("Invalid arguments: ${exception.message}")
            return BaseCommand.CommandResult.SUCCESS
        } catch (exception: Exception) {
            Console.showMessage("${exception.javaClass.simpleName}: ${exception.message}")
            return BaseCommand.CommandResult.SUCCESS
        }

        Console.showMessage(result.text)
        Console.showMessage("\n")
        return BaseCommand.CommandResult.SUCCESS
    }

    companion object {
        private const val DEFAULT_MARKET_ID = "chicomoztoc"
    }
}
