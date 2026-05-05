package maver.talkingonstations.command

import maver.talkingonstations.TosRegistry
import org.lazywizard.console.BaseCommand

class MixinToggleCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty() ) return BaseCommand.CommandResult.BAD_SYNTAX

        when (TosRegistry.getContextMixins().find { it.getKey() == p0 }?.enabled) {
            null -> return BaseCommand.CommandResult.ERROR
            true -> TosRegistry.disableContextMixin(p0)
            false -> TosRegistry.enableContextMixin(p0)
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}