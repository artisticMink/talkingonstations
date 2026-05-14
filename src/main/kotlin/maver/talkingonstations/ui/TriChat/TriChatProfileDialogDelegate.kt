package maver.talkingonstations.ui.TriChat

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color

class TriChatProfileDialogDelegate(
    private val dialog: InteractionDialogAPI,
    private val width: Float,
    private val height: Float,
) : CustomVisualDialogDelegate {
    private companion object {
        const val CLOSE_BUTTON_ID = "tos_profile_close"
        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private var callbacks: DialogCallbacks? = null
    private val panelPlugin = ProfilePanelPlugin()

    override fun init(panel: CustomPanelAPI, cb: DialogCallbacks) {
        callbacks = cb

        val section: TooltipMakerAPI = panel.createUIElement(width, height, false)
        section.addTitle("TriChat Profile")
        section.addButton(
            "Close",
            CLOSE_BUTTON_ID,
            BUTTON_COLOR,
            BUTTON_TEXT_COLOR,
            Alignment.MID,
            CutStyle.ALL,
            100f, 20f, 10f,
        )

        section.position.inTL(0f, 0f)
        panel.addUIElement(section)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin
    override fun getNoiseAlpha(): Float = 0f
    override fun advance(amount: Float) {}
    override fun reportDismissed(option: Int) {
        dialog.dismiss()
    }

    private inner class ProfilePanelPlugin : BaseCustomUIPanelPlugin() {
        override fun buttonPressed(buttonId: Any?) {
            if (buttonId == CLOSE_BUTTON_ID) {
                callbacks?.dismissDialog()
            }
        }
    }
}