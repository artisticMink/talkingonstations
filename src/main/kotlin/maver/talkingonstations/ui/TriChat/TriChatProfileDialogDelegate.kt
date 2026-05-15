package maver.talkingonstations.ui.TriChat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TooltipMakerAPI
import maver.talkingonstations.TosInspector
import maver.talkingonstations.ui.TextArea
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class TriChatProfileDialogDelegate(
    private val dialog: InteractionDialogAPI,
    private val width: Float,
    private val height: Float,
) : CustomVisualDialogDelegate {
    private companion object {
        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)

        const val PADDING = 10f
        const val CLOSE_BUTTON_ID = "tos_profile_close"
        const val CHARACTER_INFO_WIDTH = 300f
        const val CHARACTER_INFO_HEIGHT = 400f
        const val CHARACTER_PORTRAIT_SIZE = 128f
    }

    private var callbacks: DialogCallbacks? = null
    private val panelPlugin = ProfilePanelPlugin()

    private val player: PersonAPI = Global.getSector().playerPerson

    override fun init(panel: CustomPanelAPI, cb: DialogCallbacks) {
        callbacks = cb

        renderHeader(panel, Vector2f(PADDING, PADDING))
        renderCharacterPicture(panel, Vector2f(PADDING, 120f))
        renderTextArea(panel, Vector2f(400f, 100f))
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin
    override fun getNoiseAlpha(): Float = 0f
    override fun advance(amount: Float) {}
    override fun reportDismissed(option: Int) {
        dialog.dismiss()
    }

    private fun renderHeader(panel: CustomPanelAPI, at: Vector2f = Vector2f(0f, 0f)) {
        val section: TooltipMakerAPI = panel.createUIElement(width, height, false)
        section.addTitle("TriChat Profile")
        section.addButton(
            "Close",
            CLOSE_BUTTON_ID,
            BUTTON_COLOR,
            BUTTON_TEXT_COLOR,
            Alignment.MID,
            CutStyle.ALL,
            100f, 20f, 20f,
        ).position.inTL(width - 100f - PADDING , 0f)

        section.position.inTL(at.x, at.y)
        panel.addUIElement(section)
    }

    private fun renderTextArea(panel: CustomPanelAPI, at: Vector2f = Vector2f(0f, 0f)) {
        val section: TooltipMakerAPI = panel.createUIElement(400f - PADDING, 600f - PADDING, false)
        section.addTitle("Background").position.inTL(0f, 0f)

        val textArea = TextArea(
            section,
            rows = 20,
            maxRows = 50,
            width = 400f - PADDING,
            height = 480 - PADDING,
        )

        textArea.getPosition()?.inTL(0f,20f)

        section.position.inTL(at.x, at.y)
        panel.addUIElement(section)
    }

    private fun renderCharacterPicture(panel: CustomPanelAPI, at: Vector2f = Vector2f(0f, 0f)) {
        val section: TooltipMakerAPI = panel.createUIElement(CHARACTER_INFO_WIDTH, CHARACTER_INFO_HEIGHT, false)
        section.position.inTL(at.x, at.y)

        section.addImage(player.portraitSprite, CHARACTER_PORTRAIT_SIZE, 4f)
        section.addPara(player.nameString, 6f)

        panel.addUIElement(section)
    }

    private inner class ProfilePanelPlugin : BaseCustomUIPanelPlugin() {
        override fun buttonPressed(buttonId: Any?) {
            if (buttonId == CLOSE_BUTTON_ID) {
                callbacks?.dismissDialog()
            }
        }
    }
}