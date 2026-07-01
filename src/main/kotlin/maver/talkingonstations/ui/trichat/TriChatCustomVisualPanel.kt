package maver.talkingonstations.ui.trichat

import com.fs.starfarer.api.Global
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
import maver.talkingonstations.ui.DecorativeFrame
import maver.talkingonstations.ui.ProfileCard
import maver.talkingonstations.ui.TextArea
import maver.talkingonstations.ui.dto.ButtonData
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.coroutines.cancellation.CancellationException

/**
 * Control panel to interact with [maver.talkingonstations.chat.Chat]
 *
 * Shows conversation participants, controls and input methods.
 */
class TriChatCustomVisualPanel(
    visualPanelAPI: VisualPanelAPI,
    val playerPerson: PersonAPI,
    val npcPerson: PersonAPI,
) : BaseCustomUIPanelPlugin() {
    lateinit var textArea: TextArea private set

    var onPlayerQuit: (() -> Unit)? = null
    var onSendButtonClick: (suspend (message: String) -> Unit)? = null
    var onRetryButtonClick: (suspend () -> Unit)? = null
    var onModelSelectClick: ((modelSettings: ModelSettings) -> Unit)? = null
    var onDebugButtonClick: (() -> Unit)? = null

    val buttons: MutableMap<ButtonId, ButtonAPI> = mutableMapOf()

    private var activeJob: Job? = null
    private var conversationEnded = false

    private val screenW = Global.getSettings().screenWidth
    private val screenH = Global.getSettings().screenHeight

    val mainPanel: CustomPanelAPI = visualPanelAPI.showCustomPanel(screenW, screenH, this)

    // Top left band anchor
    private val bandX = (screenW - UIConstants.BAND_WIDTH) / 2f

    private val coroutineErrorHandler = CoroutineExceptionHandler { _, exception ->
        if (exception is CancellationException) return@CoroutineExceptionHandler
        TosInspector.error(
            "A TriChatCustomVisualPanel coroutine failed unexpectedly.\n${exception.stackTraceToString()}",
            this::class,
        )
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob() + coroutineErrorHandler)

    // Content
    private val contentLeftX = bandX + UIConstants.CONTENT_PAD
    private val bandTopY = screenH * UIConstants.ANCHOR_FRACTION + UIConstants.BAND_TOP_OFFSET_FROM_CENTER
    private val contentTopY = bandTopY + UIConstants.CONTENT_PAD
    private val inputColTopY = contentTopY + (UIConstants.CONTENT_HEIGHT - UIConstants.INPUT_COL_HEIGHT) / 2f
    private val npcCardLeftX = contentLeftX + UIConstants.INPUT_W + UIConstants.PROFILE_GAP_X
    private val playerCardLeftX = npcCardLeftX + UIConstants.CARD_W + UIConstants.CARD_GAP

    private val outerFrame = DecorativeFrame("graphics/ui/bgs/panel00")

    private val npcCard = ProfileCard(
        npcPerson,
        UIConstants.CARD_W, UIConstants.CARD_H, UIConstants.CARD_PAD,
        UIConstants.PORTRAIT_SIZE, UIConstants.CREST_SIZE,
    )

    private val playerCard = ProfileCard(
        playerPerson,
        UIConstants.CARD_W, UIConstants.CARD_H, UIConstants.CARD_PAD,
        UIConstants.PORTRAIT_SIZE, UIConstants.CREST_SIZE,
    )

    init {
        mainPanel.position.inTL(0f, 0f)
        drawUi()
    }

    /**
     * Draws everything in the foreground
     */
    private fun drawUi() {
        renderInput(contentLeftX + 10f, inputColTopY)
        renderButtons(contentLeftX + 10f, inputColTopY + UIConstants.INPUT_H + UIConstants.BTN_GAP_V)
        npcCard.addContent(mainPanel, npcCardLeftX, contentTopY)
        playerCard.addContent(mainPanel, playerCardLeftX, contentTopY)
    }

    /**
     * Draws the textarea
     */
    private fun renderInput(x: Float, y: Float) {
        val section: TooltipMakerAPI =
            mainPanel.createUIElement(UIConstants.INPUT_W, UIConstants.INPUT_H, false)
        section.position.inTL(x, y)

        textArea = TextArea(
            parent = section,
            rows = 4,
            maxRows = 12,
            height = UIConstants.INPUT_H,
            width = UIConstants.INPUT_W,
        )
        textArea.getPosition()?.inTL(0f, 0f)

        mainPanel.addUIElement(section)
    }

    /**
     * Draws action buttons on the bottom left.
     */
    private fun renderButtons(x: Float, y: Float) {
        val section: TooltipMakerAPI =
            mainPanel.createUIElement(UIConstants.INPUT_W, UIConstants.BTN_HEIGHT, false)
        section.position.inTL(x, y)

        val specs: List<Triple<String, ButtonId, Any?>> = listOf(
            Triple("Send", ButtonId.CHAT_SEND_BUTTON, textArea),
            Triple("Retry", ButtonId.CHAT_RETRY_BUTTON, null),
            Triple("Quit", ButtonId.CHAT_QUIT_BUTTON, null),
        )

        val offset = (UIConstants.BTN_WIDTH + UIConstants.BTN_GAP)
        specs.forEachIndexed { i, (label, id, data) ->
            val btnX = i * offset
            createButton(section, label, id, data).position.inTL(btnX, 0f)
        }

        val lastButtonPosition = offset + (specs.lastIndex * offset)
        val statusButton = createStatusButton(section, "Idle", ButtonId.STATUS, null)
        statusButton.position.inTL(lastButtonPosition, 0f)
        statusButton.isEnabled = false

        mainPanel.addUIElement(section)
    }

    /**
     * Draws background and inner frames, card frames
     */
    override fun renderBelow(alphaMult: Float) {
        val position = mainPanel.position ?: return

        val drawX = position.x + bandX
        val bandY = position.y + (screenH - bandTopY - UIConstants.BAND_HEIGHT)

        drawSolidRect(
            drawX,
            bandY,
            UIConstants.BAND_WIDTH,
            UIConstants.BAND_HEIGHT,
            UIConstants.BACKING_COLOR,
            alphaMult
        )

        // Frame around the whole band.
        outerFrame.render(drawX, bandY, UIConstants.BAND_WIDTH, UIConstants.BAND_HEIGHT, alphaMult)

        // Inner frame around each profile card
        npcCard.renderFrame(position, npcCardLeftX, contentTopY, alphaMult)
        playerCard.renderFrame(position, playerCardLeftX, contentTopY, alphaMult)
    }

    private fun drawSolidRect(x: Float, y: Float, w: Float, h: Float, c: Color, alphaMult: Float) {
        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f * alphaMult)
        GL11.glRectf(x, y, x + w, y + h)
        GL11.glPopMatrix()
    }

    fun getSendButton(): ButtonAPI? = buttons[ButtonId.CHAT_SEND_BUTTON]
    fun getRetryButton(): ButtonAPI? = buttons[ButtonId.CHAT_RETRY_BUTTON]
    fun getStatusButton(): ButtonAPI? = buttons[ButtonId.STATUS]

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

            ButtonId.CHAT_RETRY_BUTTON ->
                launch { onRetryButtonClick?.invoke() }

            // Aborts the request
            ButtonId.STATUS ->
                activeJob?.cancel(CancellationException("Aborted by player"))

            else -> {}
        }
    }

    fun markEnded() {
        conversationEnded = true
        TosEveryFrameScriptQueue.add {
            getSendButton()?.isEnabled = false
            getRetryButton()?.isEnabled = false
        }
    }

    /**
     * Executes the given block in the ui scope
     */
    fun launch(block: suspend () -> Unit) {
        activeJob = scope.launch {
            try {
                TosEveryFrameScriptQueue.add {
                    getSendButton()?.isEnabled = false
                    getRetryButton()?.isEnabled = false
                    getStatusButton()?.apply { text = "In progress..."; isEnabled = true }
                }
                block()
            } finally {
                TosEveryFrameScriptQueue.add {
                    getStatusButton()?.apply { text = ""; isEnabled = false }
                    if (!conversationEnded) {
                        getSendButton()?.isEnabled = true
                        getRetryButton()?.isEnabled = true
                    }
                }
            }
        }

    }

    private fun createButton(
        tooltip: TooltipMakerAPI,
        text: String,
        buttonId: ButtonId,
        customData: Any? = null,
        width: Float = UIConstants.BTN_WIDTH,
        height: Float = UIConstants.BTN_HEIGHT
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

    private fun createStatusButton(
        tooltip: TooltipMakerAPI,
        text: String,
        buttonId: ButtonId,
        customData: Any? = null,
        width: Float = UIConstants.BTN_WIDTH,
        height: Float = UIConstants.BTN_HEIGHT
    ): ButtonAPI {
        return tooltip.addButton(
            text,
            buttonId,
            Color.BLACK,
            Color.BLUE,
            Alignment.MID,
            CutStyle.NONE,
            width,
            height,
            0f
        ).also {
            it.customData = ButtonData(buttonId, customData)
            buttons[buttonId] = it
        }
    }

    private object UIConstants {
        // 1f = right, 0.5f = center, 0f = left
        const val ANCHOR_FRACTION = 0.5f
        const val BAND_TOP_OFFSET_FROM_CENTER = 125f
        const val CONTENT_PAD = 22f

        const val INPUT_W = 490f
        const val INPUT_H = 120f

        const val BTN_WIDTH = 110f
        const val BTN_HEIGHT = 26f
        const val BTN_GAP = 10f
        const val BTN_GAP_V = 10f

        const val PROFILE_GAP_X = 30f
        const val CARD_W = 240f
        const val CARD_H = 168f
        const val CARD_GAP = 14f
        const val CARD_PAD = 12f
        const val PORTRAIT_SIZE = 84f
        const val CREST_SIZE = 28f

        // Band height = content + padding, raise CARD_H/INPUT_H to grow
        const val INPUT_COL_HEIGHT = INPUT_H + BTN_GAP_V + BTN_HEIGHT
        val CONTENT_HEIGHT = maxOf(INPUT_COL_HEIGHT, CARD_H)
        val BAND_HEIGHT = CONTENT_HEIGHT + CONTENT_PAD * 2f

        // Band width = content + padding
        const val CONTENT_WIDTH = INPUT_W + PROFILE_GAP_X + CARD_W + CARD_GAP + CARD_W
        const val BAND_WIDTH = CONTENT_WIDTH + CONTENT_PAD * 2f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
        val BACKING_COLOR = Color(6, 8, 12, 250)
    }
}
