package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class MixinListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        Console.showMessage("Currently loaded context providers:")
        Console.showMessage(
            TosRegistry.getContextMixins().joinToString { "${it.getKey()} -> ${it.enabled}\n" }
        )

        Console.showMessage("\n")
        return BaseCommand.CommandResult.SUCCESS
    }
}