package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand

class PersonToggleCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        if (p0.isEmpty() ) return BaseCommand.CommandResult.BAD_SYNTAX

        when (TosSettings.getPersonTypes().find { it.getKey() == p0 }?.enabled) {
            null -> return BaseCommand.CommandResult.ERROR
            true -> TosSettings.disablePersonType(p0)
            false -> TosSettings.enablePersonType(p0)
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}