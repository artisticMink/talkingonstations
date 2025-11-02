package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class PersonTypesReloadCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        TosSettings.reloadPersonTypes()
        Console.showMessage("Person types reloaded from disk")

        return BaseCommand.CommandResult.SUCCESS
    }
}