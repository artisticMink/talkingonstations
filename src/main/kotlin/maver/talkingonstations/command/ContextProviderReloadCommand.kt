package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ContextProviderReloadCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        TosSettings.reloadContextProvider()
        Console.showMessage("Context provider reloaded from disk")

        return BaseCommand.CommandResult.SUCCESS
    }
}