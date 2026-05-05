package maver.talkingonstations.command

import maver.talkingonstations.InspectableInterface
import maver.talkingonstations.TosInspector
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class InspectCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX

        val segments: List<String> = p0.split(" ")
        val className: String = segments[0]
        val item: String? = segments.getOrNull(1)

        val instances: List<Any> = TosInspector.get(className)
        if (instances.isEmpty()) {
            Console.showMessage("No instances of $className are available in this context.")
            return BaseCommand.CommandResult.WRONG_CONTEXT
        } else if (instances.size > 1) {
            Console.showMessage("More than one instance of $className. Abort.")
            return BaseCommand.CommandResult.ERROR
        }

        try {
            val instance = instances.first() as InspectableInterface
            if (item == null) {
                Console.showMessage("Available inspections: \n${instance.canInspect().joinToString("\n")}")
                return BaseCommand.CommandResult.SUCCESS
            }

            val inspection = instance.inspect(item)
            Console.showMessage("$item:\n\n${inspection.ifEmpty { "No content" }}")

        } catch (exception: Exception) {
            Console.showMessage(exception.message)
            return BaseCommand.CommandResult.ERROR
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}