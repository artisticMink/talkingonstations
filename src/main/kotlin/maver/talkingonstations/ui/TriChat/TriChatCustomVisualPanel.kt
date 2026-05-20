package maver.talkingonstations.ui.TriChat

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.VisualPanelAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TooltipMakerAPI
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.ui.ButtonId
import maver.talkingonstations.ui.TextArea
import maver.talkingonstations.ui.dto.ButtonData
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.coroutines.cancellation.CancellationException

/**
 * UI Panel Plugin
 * 
 */
class TriChatCustomVisualPanel(
    visualPanelAPI: VisualPanelAPI,
    val playerPerson: PersonAPI,
    val npcPerson: PersonAPI,
) : BaseCustomUIPanelPlugin() {
    private object UIConstants {
        const val CHAT_PANEL_WIDTH = 500f
        const val CHAT_PANEL_HEIGHT = 100f
        const val PORTRAIT_BOX_WIDTH = 500f
        const val PORTRAIT_BOX_HEIGHT = 100f
        const val NPC_INFO_WIDTH = 300f
        const val NPC_INFO_HEIGHT = 400f
        const val NPC_PORTRAIT_SIZE = 128f
        const val FACTION_CREST_SIZE = 32f
        const val MODEL_SETTINGS_WIDTH = 100f
        const val MODEL_SETTINGS_HEIGHT = 300f
        const val BUTTON_WIDTH = 100f
        const val BUTTON_HEIGHT = 20f
        const val SMALL_BUTTON_WIDTH = 100f
        const val SMALL_BUTTON_HEIGHT = 20f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private var activeJob: Job? = null
    private val coroutineErrorHandler = CoroutineExceptionHandler { _, exception ->
        if (exception is CancellationException) return@CoroutineExceptionHandler
        TosInspector.error(
            "A TriChatCustomVisualPanel coroutine failed unexpectedly.\n${exception.stackTraceToString()}",
            this::class,
        )
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob() + coroutineErrorHandler)

    lateinit var textArea: TextArea private set

    var onPlayerQuit: (() -> Unit)? = null
    var onSendButtonClick: (suspend (message: String) -> Unit)? = null
    var onRetryButtonClick: (suspend () -> Unit)? = null
    var onModelSelectClick: ((modelSettings: ModelSettings) -> Unit)? = null

    val mainPanel: CustomPanelAPI = visualPanelAPI.showCustomPanel(0f,0f, this)
    val buttons: MutableMap<ButtonId, ButtonAPI> = mutableMapOf()

    init {
        drawUi()

        //ToDo: Remove debug
        TosInspector.addVisualProbe(mainPanel)
    }

    private fun drawUi() {
        renderChatBoxBottomLeft(Vector2f(-515f, 410f))
        renderNpcInfoTopRight(Vector2f(00f, 0f))
        renderPortraitBoxBottomRight(Vector2f(200f, 400f))
    }

    fun renderNpcInfoTopRight(at: Vector2f = Vector2f(0f, 0f)) {
        val section: TooltipMakerAPI =
            mainPanel.createUIElement(UIConstants.NPC_INFO_WIDTH, UIConstants.NPC_INFO_HEIGHT, false)
        section.position.inTL(at.x, at.y)

        section.addImage(npcPerson.portraitSprite, UIConstants.NPC_PORTRAIT_SIZE, 4f)
        section.addPara(npcPerson.nameString, 6f)

        val rank = if (!npcPerson.rank.contains("Unknown")) "${npcPerson.rank} — ${npcPerson.post}" else npcPerson.post
        section.addPara(rank, 0f)

        val crest = npcPerson.faction.crest
        if (!crest.isNullOrEmpty()) {
            section.addImage(crest, UIConstants.FACTION_CREST_SIZE, 6f)
        }
        section.addPara(npcPerson.faction.displayName, npcPerson.faction.baseUIColor, 0f)
        section.addRelationshipBar(npcPerson, UIConstants.NPC_INFO_WIDTH - 20f, 8f)

        mainPanel.addUIElement(section)
    }

    fun renderChatBoxBottomLeft(at: Vector2f = Vector2f(0f, 0f)) {
        val bottomLeftSection: TooltipMakerAPI =
            mainPanel.createUIElement(UIConstants.CHAT_PANEL_WIDTH, UIConstants.CHAT_PANEL_HEIGHT, false)
        bottomLeftSection.position.inTL(at.x, at.y)

        //val bottomLeftSectionLabel = bottomLeftSection.addTitle("Tri-Chat (TM)")
        //bottomLeftSectionLabel.position.setSize(100f, 20f)

        textArea = TextArea(bottomLeftSection)
        textArea.getPosition()?.inTL(0f,0f)

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

        mainPanel.addUIElement(bottomLeftSection)
    }

    fun renderPortraitBoxBottomRight(at: Vector2f = Vector2f(0f, 0f)) {
        val bottomRightSection: TooltipMakerAPI =
            mainPanel.createUIElement(UIConstants.PORTRAIT_BOX_WIDTH, UIConstants.PORTRAIT_BOX_HEIGHT, false)
        bottomRightSection.position.inTL(at.x, at.y)

        val bottomRightSectionLabel = bottomRightSection.addTitle("HegMon Citizen Database (unlicensed)")
        bottomRightSectionLabel.position.setSize(350f, 20f)
        bottomRightSection.addImage(playerPerson.portraitSprite.toString(), 0f)

        mainPanel.addUIElement(bottomRightSection)
    }

    fun getSendButton(): ButtonAPI? =  buttons[ButtonId.CHAT_SEND_BUTTON]
    fun getRetryButton(): ButtonAPI? =  buttons[ButtonId.CHAT_RETRY_BUTTON]

    override fun buttonPressed(buttonId: Any?) {
        super.buttonPressed(buttonId)

        if (buttonId !is ButtonData) return

        when (buttonId.buttonId) {
            ButtonId.CHAT_QUIT_BUTTON -> {
                onPlayerQuit?.invoke()
            }

            ButtonId.CHAT_SEND_BUTTON -> {
                val chatInputTextArea: TextArea = buttonId.customData as TextArea
                val input = chatInputTextArea.getText()
                chatInputTextArea.clearText()

                launch { onSendButtonClick?.invoke(input) }
            }

            ButtonId.CHAT_RETRY_BUTTON -> launch { onRetryButtonClick?.invoke() }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        activeJob = scope.launch {
            try {
                TosEveryFrameScriptQueue.add {
                    getSendButton()?.isEnabled = false
                    getRetryButton()?.isEnabled = false
                }
                block()
            } finally {
                TosEveryFrameScriptQueue.add {
                    getSendButton()?.isEnabled = true
                    getRetryButton()?.isEnabled = true
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
}