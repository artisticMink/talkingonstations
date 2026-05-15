package maver.talkingonstations.campaign.abilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.ui.TooltipMakerAPI
import maver.talkingonstations.ui.TriChat.TriChatProfileInteractionDialogPlugin

class TriChatAbility: BaseDurationAbility() {
    override fun setCooldownLeft(p0: Float) { }

    override fun getCooldownLeft(): Float {
        return 0f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        tooltip?.addTitle("Open TriChat")
        tooltip?.addPara("View and edit your character profile.", 10f)
    }

    override fun activateImpl() {
        Global.getSector().campaignUI.showInteractionDialog(
            TriChatProfileInteractionDialogPlugin(),
            Global.getSector().playerFleet
        )

    }

    override fun applyEffect(p0: Float, p1: Float) {

    }

    override fun deactivateImpl() {

    }

    override fun cleanupImpl() {

    }

    override fun hasTooltip(): Boolean {
        return true
    }
}