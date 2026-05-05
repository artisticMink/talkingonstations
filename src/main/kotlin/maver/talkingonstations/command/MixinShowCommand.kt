package maver.talkingonstations.command

import com.fs.starfarer.api.Global
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfo
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class MixinShowCommand : BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        if (p0.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX

        val parts = p0.trim().split(Regex("\\s+"))
        val mixinName = parts[0]
        val marketId = parts.getOrNull(1) ?: DEFAULT_MARKET_ID

        val mixin: ContextMixinInterface? = TosRegistry.getContextMixins().find { it.getKey() == mixinName }
        if (mixin == null) {
            Console.showMessage("Mixin $mixinName not found")
            return BaseCommand.CommandResult.SUCCESS
        }

        val market = Global.getSector().economy.getMarket(marketId)
        if (market == null) {
            Console.showMessage("Market $marketId not found")
            return BaseCommand.CommandResult.SUCCESS
        }

        // ToDo: Add random npc for testing NPC-dependent mixins
        Console.showMessage("----- $mixinName @ ${market.name} -----")
        Console.showMessage(
            mixin.getText(
                GameInfo(
                    Global.getSector().playerPerson,
                    null,
                    market,
                )
            )
        )

        Console.showMessage("\n")
        return BaseCommand.CommandResult.SUCCESS
    }

    companion object {
        private const val DEFAULT_MARKET_ID = "chicomoztoc"
    }
}
