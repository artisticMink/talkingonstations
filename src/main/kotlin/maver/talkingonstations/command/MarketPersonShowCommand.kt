package maver.talkingonstations.command

import com.fs.starfarer.api.Global
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.llm.LLMContext
import maver.talkingonstations.llm.dto.GameInfo
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class MarketPersonShowCommand : BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX

        val args = p0.trim().split(Regex("\\s+"))
        val id = args.first()
        val toLog = args.getOrNull(1)

        val entry = TosRegistry.getMarketPersons().entries
            .find { (_, data) -> data.id == id }
        if (entry == null) {
            Console.showMessage("Market person $id not found")
            return BaseCommand.CommandResult.SUCCESS
        }

        val data = entry.value

        val market = Global.getSector().economy.getMarket(data.market)
        if (market == null) {
            Console.showMessage("Market ${data.market} not found")
            return BaseCommand.CommandResult.SUCCESS
        }

        val person = market.peopleCopy.find { it.id == entry.key }
        if (person == null) {
            Console.showMessage("Person ${entry.key} not registered at ${market.name}")
            return BaseCommand.CommandResult.SUCCESS
        }

        val context = LLMContext(
            GameInfo(
                Global.getSector().playerPerson,
                person,
                market,
            )
        )

        if (toLog != null) {
            Global.getLogger(javaClass).info("Context for ${person.name.fullName} at ${market.name}:\n${context.getSystemBlock()}")
            Console.showMessage("Dumped context for ${person.name.fullName} to starsector.log")
        } else {
            Console.showMessage("Dumping context for ${person.name.fullName} at ${market.name}")
            Console.showMessage(context.getSystemBlock())
            Console.showMessage("\n")
        }

        return BaseCommand.CommandResult.SUCCESS
    }
}
