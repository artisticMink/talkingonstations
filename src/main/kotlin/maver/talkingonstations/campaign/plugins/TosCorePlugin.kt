package maver.talkingonstations.campaign.plugins

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import maver.talkingonstations.TosStrings
import maver.talkingonstations.campaign.ChatInteractionDialogPlugin

class TosCorePlugin : BaseCampaignPlugin() {
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget != null) {
            if (interactionTarget.hasTag(TosStrings.Tags.COMM_CHAT) && interactionTarget.market != null) {
                return PluginPick<InteractionDialogPlugin>(
                    ChatInteractionDialogPlugin(),
                    PickPriority.CORE_GENERAL
                )
            }
        }

        return super.pickInteractionDialogPlugin(interactionTarget)
    }
}