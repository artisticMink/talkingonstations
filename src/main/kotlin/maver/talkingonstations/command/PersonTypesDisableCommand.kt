package maver.talkingonstations.command

import maver.talkingonstations.TosSettings
import org.lazywizard.console.BaseCommand

class PersonTypesDisableCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty() ) return BaseCommand.CommandResult.BAD_SYNTAX
        TosSettings.disablePersonType(p0)

        return BaseCommand.CommandResult.SUCCESS
    }
}