package maver.talkingonstations.ui.trichat

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.ui.ButtonId
import maver.talkingonstations.ui.dto.ButtonData
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Delegate of [TriChatCustomVisualPanel] dialog
 *
 * This will render a context viewer for an [maver.talkingonstations.llm.LLMContext]
 */
class TriChatContextDialogDelegate(
    private val chat: Chat,
    private val width: Float = WIDTH,
    private val height: Float = HEIGHT,
) : CustomVisualDialogDelegate {

    companion object {
        const val WIDTH = 800f
        const val HEIGHT = 600f
        private const val SYSTEM_BLOCK = -1
    }

    /**
     * Full width of parent minus padding.
     * Height defined by content.
     */
    private object UIConstants {
        const val PAD = 10f

        const val HEADER_H = 24f
        const val CLOSE_W = 100f
        const val CLOSE_H = 20f

        const val SCROLLBAR_W = 20f
        const val ROW_H = 24f
        const val BLOCK_GAP = 8f
        const val CONTENT_PAD = 6f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private val contentY = UIConstants.PAD + UIConstants.HEADER_H + 10f
    private val viewW = width - UIConstants.PAD * 2f
    private val viewH = height - contentY - UIConstants.PAD
    private val rowW = viewW - UIConstants.SCROLLBAR_W

    private var callbacks: DialogCallbacks? = null
    private val panelPlugin = ContextPanelPlugin()
    private var rootPanel: CustomPanelAPI? = null
    private var transcript: TooltipMakerAPI? = null
    private var expandedIndex: Int? = null

    override fun init(panel: CustomPanelAPI, dialogCallbacks: DialogCallbacks) {
        callbacks = dialogCallbacks
        rootPanel = panel

        renderHeader(panel)
        renderTranscript(panel)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin
    override fun getNoiseAlpha(): Float = 0f
    override fun advance(amount: Float) {}

    // This delegate is a popover, keep background ui
    override fun reportDismissed(option: Int) {}

    /**
     * Contains whole context stats.
     */
    private fun renderHeader(panel: CustomPanelAPI) {
        val section = panel.createUIElement(width, UIConstants.HEADER_H, false)
        section.addTitle("Context Inspector")

        val payload = ButtonData(ButtonId.DEBUG_CLOSE, null)
        section.addButton(
            "Close", payload,
            UIConstants.BUTTON_COLOR, UIConstants.BUTTON_TEXT_COLOR,
            Alignment.MID, CutStyle.ALL, UIConstants.CLOSE_W, UIConstants.CLOSE_H, 0f,
        ).also {
            it.customData = payload
            it.position.inTL(width - UIConstants.CLOSE_W - (UIConstants.PAD * 2f), 0f)
        }

        section.position.inTL(UIConstants.PAD, UIConstants.PAD)
        panel.addUIElement(section)
    }

    /**
     * Context transcript with collapsible messages.
     */
    private fun renderTranscript(panel: CustomPanelAPI) {
        val scroller = panel.createUIElement(viewW, viewH, true)
        scroller.textWidthOverride = rowW

        val systemBlock = chat.getSystemBlock()
        val history = chat.getMessagesCopy().filter { it.role != ChatRoles.INFO }

        history.lastOrNull { it.usage != null }?.usage?.let {
            scroller.addPara(
                "Context size: %s tokens total. Last completion: %s prompt tokens with %s response tokens",
                4f, Misc.getHighlightColor(),
                "${it.totalTokens}", "${it.promptTokens}", "${it.completionTokens}",
            )
        }

        addBlock(scroller, SYSTEM_BLOCK, "SYSTEM (Mixins)", roleColor(ChatRoles.SYSTEM)) {
            addParagraphs(scroller, systemBlock)
        }

        history.forEachIndexed { index, message ->
            addBlock(scroller, index, rowTitle(index, message), roleColor(message.role)) {
                addMessageBody(scroller, message)
            }
        }

        transcript = scroller
        panel.addUIElement(scroller).inTL(UIConstants.PAD, contentY)
    }

    /**
     * Add a message block.
     */
    private fun addBlock(scroller: TooltipMakerAPI, index: Int, title: String, color: Color, body: () -> Unit) {
        val payload = ButtonData(ButtonId.DEBUG_TOGGLE_BLOCK, index)
        scroller.addAreaCheckbox(
            title, payload, color, Misc.getDarkPlayerColor(), color,
            rowW, UIConstants.ROW_H, UIConstants.BLOCK_GAP, true,
        ).also {
            it.customData = payload
            it.isChecked = index == expandedIndex
        }
        if (index == expandedIndex) body()
    }

    private fun rowTitle(index: Int, message: Message): String = buildString {
        append("#$index ${message.role}")
        message.toolCallId?.let { append(it) }
        if (message.toolCalls.isNotEmpty())
            append(" - calls ${message.toolCalls.joinToString { it.name }}")
    }

    private fun addMessageBody(scroller: TooltipMakerAPI, message: Message) {
        message.reasoning?.takeIf { it.isNotBlank() }?.let { reasoning ->
            addParagraphs(scroller, reasoning, Misc.getGrayColor())
        }

        addParagraphs(scroller, message.content)

        message.toolCalls.forEach { call ->
            scroller.addPara("tool call ${call.name} (${call.id})", roleColor(ChatRoles.TOOL), UIConstants.CONTENT_PAD)
            scroller.addPara(call.arguments, Misc.getGrayColor(), 2f)
        }
    }

    private fun addParagraphs(scroller: TooltipMakerAPI, content: String, color: Color? = null) {
        content.split("\n\n")
            .filter { it.isNotBlank() }
            .forEach {
                if (color != null) scroller.addPara(it, color, UIConstants.CONTENT_PAD)
                else scroller.addPara(it, UIConstants.CONTENT_PAD)
            }
    }

    /**
     * Toggles the block visibility at index and
     * re-renders the context transcript.
     */
    private fun toggleBlock(index: Int) {
        expandedIndex = if (expandedIndex == index) null else index
        rebuildTranscript()
    }

    /**
     * Re-renders the transcript
     *
     * Needs to be called to adjust the scroller after
     * collapsing/expanding a block.
     */
    private fun rebuildTranscript() {
        val panel = rootPanel ?: return
        val offset = transcript?.externalScroller?.yOffset ?: 0f

        transcript?.let { panel.removeComponent(it.externalScroller ?: it) }
        renderTranscript(panel)

        transcript?.let {
            val maxOffset = (it.heightSoFar - viewH).coerceAtLeast(0f)
            it.externalScroller.yOffset = offset.coerceIn(0f, maxOffset)
        }
    }

    private fun roleColor(role: ChatRoles): Color = when (role) {
        ChatRoles.SYSTEM -> Misc.getGrayColor()
        ChatRoles.USER -> Misc.getBasePlayerColor()
        ChatRoles.ASSISTANT -> Misc.getHighlightColor()
        ChatRoles.TOOL -> Misc.getStoryOptionColor()
        ChatRoles.INFO -> Misc.getGrayColor()
    }

    /**
     * Translucent background for the modal popup effect.
     */
    private inner class ContextPanelPlugin : BaseCustomUIPanelPlugin() {

        override fun renderBelow(alphaMult: Float) {
            val p = rootPanel?.position ?: return

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_CURRENT_BIT)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glColor4f(0f, 0f, 0f, alphaMult)
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glVertex2f(p.x, p.y)
            GL11.glVertex2f(p.x + p.width, p.y)
            GL11.glVertex2f(p.x + p.width, p.y + p.height)
            GL11.glVertex2f(p.x, p.y + p.height)
            GL11.glEnd()

            GL11.glPopAttrib()
        }

        override fun buttonPressed(buttonId: Any?) {
            val data = buttonId as? ButtonData ?: return
            when (data.buttonId) {
                ButtonId.DEBUG_CLOSE -> callbacks?.dismissDialog()
                ButtonId.DEBUG_TOGGLE_BLOCK -> (data.customData as? Int)?.let(::toggleBlock)
                else -> {}
            }
        }
    }
}
