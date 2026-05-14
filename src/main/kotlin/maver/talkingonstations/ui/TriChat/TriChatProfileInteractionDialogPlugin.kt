package maver.talkingonstations.ui.TriChat

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI

class TriChatProfileInteractionDialogPlugin : InteractionDialogPlugin {
    private companion object {
        const val WIDTH = 600f
        const val HEIGHT = 400f
    }

    override fun init(dialog: InteractionDialogAPI?) {
        if (dialog == null) return
        dialog.showCustomVisualDialog(WIDTH, HEIGHT, TriChatProfileDialogDelegate(dialog, WIDTH, HEIGHT))
    }

    override fun optionSelected(text: String?, data: Any?) {}
    override fun optionMousedOver(text: String?, data: Any?) {}
    override fun advance(amount: Float) {}
    override fun backFromEngagement(result: EngagementResultAPI?) {}
    override fun getContext(): Any? = null
    override fun getMemoryMap(): Map<String?, MemoryAPI?>? = null
}
