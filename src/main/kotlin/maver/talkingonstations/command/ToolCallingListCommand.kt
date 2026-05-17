package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ToolCallingListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        Console.showMessage("Loaded tools:")
        Console.showMessage(
            TosRegistry.getTools().joinToString { "${it.getKey()} (${it.getName()}) -> ${it.enabled}\n" }
        )

        return BaseCommand.CommandResult.SUCCESS
    }
}