package maver.talkingonstations.campaign

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.*
import kotlinx.coroutines.*
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.ui.ButtonId
import maver.talkingonstations.ui.dto.ButtonData
import maver.talkingonstations.ui.TextArea
import java.awt.Color

class BarChatCustomUiPanel(val dialog: InteractionDialogAPI, val player: PersonAPI, val person: PersonAPI) : BaseCustomUIPanelPlugin() {
    private object UIConstants {
        const val CHAT_PANEL_WIDTH = 500f
        const val CHAT_PANEL_HEIGHT = 100f
        const val PORTRAIT_BOX_WIDTH = 500f
        const val PORTRAIT_BOX_HEIGHT = 100f
        const val MODEL_SETTINGS_WIDTH = 100f
        const val MODEL_SETTINGS_HEIGHT = 300f
        const val BUTTON_WIDTH = 100f
        const val BUTTON_HEIGHT = 20f
        const val SMALL_BUTTON_WIDTH = 100f
        const val SMALL_BUTTON_HEIGHT = 20f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var onPlayerQuit: (() -> Unit)? = null
    var onSendButtonClick: (suspend (message: String) -> Unit)? = null
    var onRetryButtonClick: (suspend () -> Unit)? = null
    var onModelSelectClick: ((modelSettings: ModelSettings) -> Unit)? = null

    val mainPanel: CustomPanelAPI = dialog.visualPanel.showCustomPanel(1000f, 1000f, this)
    val buttons: MutableMap<ButtonId, ButtonAPI> = mutableMapOf()

    init {
        mainPanel.position?.inTL(30f, 600f)
        drawUi()
    }

    private fun drawUi() {
        renderChatBoxBottomLeft(mainPanel)
        renderSidebarBoxRight(mainPanel)
        renderPortraitBoxBottomRight(mainPanel)
    }

    fun renderChatBoxBottomLeft(panel: CustomPanelAPI) {
        val bottomLeftSection: TooltipMakerAPI =
            panel.createUIElement(UIConstants.CHAT_PANEL_WIDTH, UIConstants.CHAT_PANEL_HEIGHT, false)
        bottomLeftSection.position.inTL(0f, 0f)

        val bottomLeftSectionLabel = bottomLeftSection.addTitle("Tri-Chat (TM)")
        bottomLeftSectionLabel.position.setSize(100f, 20f)

        val textArea = TextArea(bottomLeftSection)
        textArea.getPosition()?.inTL(20f,30f)

        createButton(
            bottomLeftSection,
            "Send",
            ButtonId.CHAT_SEND_BUTTON,
            textArea,
        ).position.inTL(550f, 70f)

        createButton(
            bottomLeftSection,
            "Retry",
            ButtonId.CHAT_RETRY_BUTTON,
            null,
            UIConstants.SMALL_BUTTON_WIDTH,
            UIConstants.SMALL_BUTTON_HEIGHT,
        ).position.inTL(550f, 95f)

        createButton(
            bottomLeftSection,
            "Quit",
            ButtonId.CHAT_QUIT_BUTTON,
            null,
            UIConstants.SMALL_BUTTON_WIDTH,
            UIConstants.SMALL_BUTTON_HEIGHT,
        ).position.inTL(550f, 120f)

        panel.addUIElement(bottomLeftSection)
    }

    fun renderPortraitBoxBottomRight(panel: CustomPanelAPI) {
        val bottomRightSection: TooltipMakerAPI =
            panel.createUIElement(UIConstants.PORTRAIT_BOX_WIDTH, UIConstants.PORTRAIT_BOX_HEIGHT, false)
        bottomRightSection.position.inTL(600f, -20f)

        val bottomRightSectionLabel = bottomRightSection.addTitle("HegMon Citizen Database (unlicensed)")
        bottomRightSectionLabel.position.setSize(350f, 20f)
        bottomRightSection.addImage(player.portraitSprite.toString(), 0f)

        panel.addUIElement(bottomRightSection)
    }

    fun renderSidebarBoxRight(panel: CustomPanelAPI) {
        val npcSection: TooltipMakerAPI = panel.createUIElement(100f, 100f, false)
        npcSection.position.inTL(550f, -500f)
        npcSection.addTitle(person.name?.fullName ?: "Unknown")
        npcSection.addImage(person.portraitSprite.toString(), 0f)

        panel.addUIElement(npcSection)

        val modelSettingsSection: TooltipMakerAPI =
            panel.createUIElement(UIConstants.MODEL_SETTINGS_WIDTH, UIConstants.MODEL_SETTINGS_HEIGHT, false)
        modelSettingsSection.position.inTL(550f, -350f)
        modelSettingsSection.addTitle("Models")
        /**
        val modelRadioButtons: LunaUIRadioButton = LunaUIRadioButton(
            availableModels[0],
            availableModels,
            250f,
            300f,
            "",
            "",
            panel,
            modelSettingsSection
        )

        modelRadioButtons.onClick { model ->
            val modelSettings = OpenrouterModelService.getModelSettings(modelRadioButtons.value ?: "")
            onModelSelectClick?.invoke(modelSettings)
        }
*/
        panel.addUIElement(modelSettingsSection)
    }

    override fun buttonPressed(buttonId: Any?) {
        super.buttonPressed(buttonId)

        if (buttonId !is ButtonData) return

        when (buttonId.buttonId) {
            ButtonId.CHAT_QUIT_BUTTON -> {
                onPlayerQuit?.invoke()
            }

            ButtonId.CHAT_SEND_BUTTON -> {
                val chatInputTextArea: TextArea = buttonId.customData as TextArea
                scope.launch {
                    onSendButtonClick?.invoke(chatInputTextArea.getText())
                    chatInputTextArea.clearText()
                }
            }

            ButtonId.CHAT_RETRY_BUTTON -> {
                scope.launch {
                    onRetryButtonClick?.invoke()
                }
            }
        }
    }

    private fun createButton(
        tooltip: TooltipMakerAPI,
        text: String,
        buttonId: ButtonId,
        customData: Any? = null,
        width: Float = UIConstants.BUTTON_WIDTH,
        height: Float = UIConstants.BUTTON_HEIGHT
    ): ButtonAPI {
        return tooltip.addButton(
            text,
            buttonId,
            UIConstants.BUTTON_COLOR,
            UIConstants.BUTTON_TEXT_COLOR,
            Alignment.MID,
            CutStyle.ALL,
            width,
            height,
            0f
        ).also {
            it.customData = ButtonData(buttonId, customData)
            buttons[buttonId] = it
        }
    }

    fun cancelScope() {
        scope.cancel()
    }

}