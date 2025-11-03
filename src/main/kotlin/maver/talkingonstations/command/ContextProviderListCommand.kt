package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ContextProviderListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        Console.showMessage("Loaded context providers:")
        Console.showMessage(
            TosSettings.getContextMixins().joinToString { "${it.getKey()} -> ${it.enabled}\n" }
        )

        return BaseCommand.CommandResult.SUCCESS
    }
}