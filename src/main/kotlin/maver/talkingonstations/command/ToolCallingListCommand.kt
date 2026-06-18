package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ToolCallingListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        // Every loaded tool is enabled by definition: disabled CSV rows are
        // filtered out before instantiation.
        Console.showMessage("Loaded tools:")
        Console.showMessage(
            TosRegistry.getTools().joinToString("\n") { tool ->
                "${tool.getKey()} (${tool.name}) params: [${tool.parameters.joinToString { it.name }}]"
            }
        )

        return BaseCommand.CommandResult.SUCCESS
    }
}
