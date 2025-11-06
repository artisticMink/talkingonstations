package maver.talkingonstations.command

import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.TosSettings
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class MarketPersonsListCommand: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        Console.showMessage("Loaded context providers:")
        Console.showMessage(
            TosSettings.getMarketPersons().toList().joinToString {
                val person: PersonAPI = it.first
                "${person.name.fullName} -> ${person.market?.id ?: "unknown"}\n"
            }
        )

        return BaseCommand.CommandResult.SUCCESS
    }
}