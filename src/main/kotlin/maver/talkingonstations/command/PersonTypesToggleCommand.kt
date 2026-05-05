package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand

class PersonTypesToggleCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult {
        if (p0.isEmpty() ) return BaseCommand.CommandResult.BAD_SYNTAX

        when (TosRegistry.getArchetypes().find { it.getKey() == p0 }?.enabled) {
            null -> return BaseCommand.CommandResult.ERROR
            true -> TosRegistry.disableArchetype(p0)
            false -> TosRegistry.enableArchetype(p0)
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}