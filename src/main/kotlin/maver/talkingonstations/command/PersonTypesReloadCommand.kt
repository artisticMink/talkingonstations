package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class PersonTypesReloadCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        TosRegistry.reloadArchetypes()
        Console.showMessage("Person types reloaded from disk")

        return BaseCommand.CommandResult.SUCCESS
    }
}