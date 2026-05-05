package maver.talkingonstations.ui.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Opens a full-screen modal with a single DebugProbe you can drag around.
 * Entered via the `tos.ui.probe` console command; dismissed with the
 * "Close" button.
 */
class ProbeScratchpadPlugin : InteractionDialogPlugin {

    override fun init(dialog: InteractionDialogAPI) {
        val settings = Global.getSettings()
        val width = (settings.screenWidth - 100f).coerceAtLeast(800f)
        val height = (settings.screenHeight - 120f).coerceAtLeast(600f)

        dialog.showCustomDialog(width, height, object : BaseCustomDialogDelegate() {
            private var probe: DebugProbe? = null

            override fun createCustomDialog(
                panel: CustomPanelAPI,
                callback: CustomDialogDelegate.CustomDialogCallback,
            ) {
                val p = DebugProbe(panel).also { probe = it }
                p.placeInTL(panel, 100f, 100f)
            }

            override fun hasCancelButton(): Boolean = false
            override fun getConfirmText(): String = "Close"
            override fun getCancelText(): String? = null
            override fun customDialogConfirm() {}
            override fun customDialogCancel() {}
            override fun getCustomPanelPlugin(): CustomUIPanelPlugin? = null
        })
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {}
    override fun optionMousedOver(optionText: String?, optionData: Any?) {}
    override fun advance(amount: Float) {}
    override fun backFromEngagement(battleResult: EngagementResultAPI?) {}
    override fun getContext(): Any? = null
    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}
