package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class PersonTypesListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        Console.showMessage("Loaded person types:")
        Console.showMessage(
            TosRegistry.getArchetypes().joinToString { "${it.getKey()} -> ${it.enabled}\n" }
        )

        return BaseCommand.CommandResult.SUCCESS
    }
}