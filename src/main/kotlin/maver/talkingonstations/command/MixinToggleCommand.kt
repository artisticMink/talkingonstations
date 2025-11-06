package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand

class MixinToggleCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty() ) return BaseCommand.CommandResult.BAD_SYNTAX
        TosSettings.enableContextMixin(p0)

        when (TosSettings.getPersonTypes().find { it.getKey() == p0 }?.enabled) {
            null -> return BaseCommand.CommandResult.ERROR
            true -> TosSettings.disableContextMixin(p0)
            false -> TosSettings.enableContextMixin(p0)
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}