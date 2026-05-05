package maver.talkingonstations.command

import com.fs.starfarer.api.Global
import maver.talkingonstations.ui.debug.ProbeScratchpadPlugin
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class UiProbeCommand : BaseCommand {
    override fun runCommand(
        args: String,
        context: BaseCommand.CommandContext,
    ): BaseCommand.CommandResult {
        val sector = Global.getSector()
        val fleet = sector.playerFleet
        if (fleet == null) {
            Console.showMessage("tos.ui.probe: run from the campaign map (no player fleet available).")
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }
        sector.campaignUI.showInteractionDialog(ProbeScratchpadPlugin(), fleet)
        return BaseCommand.CommandResult.SUCCESS
    }
}
