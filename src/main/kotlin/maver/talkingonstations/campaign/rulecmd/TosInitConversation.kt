package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class TosInitConversation : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI,
        params: MutableList<Misc.Token?>,
        memoryMap: MutableMap<String?, MemoryAPI?>?
    ): Boolean {
        dialog.optionPanel.addOption("Connect to Tri-Chat (unlicensed).", "foo")
        return true
    }
}