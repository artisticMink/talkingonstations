package maver.talkingonstations.ui.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Draggable red-outlined rectangle that shows its live coordinates.
 *
 * Usage:
 *     val probe = DebugProbe(mainPanel, w = 200f, h = 100f)
 *     probe.placeInTL(mainPanel, 50f, 50f)
 *
 * Controls:
 *  - LMB-drag (interior): move with the mouse
 *  - LMB-drag (edge/corner): resize along that edge/corner (8px grab zone)
 */
class DebugProbe(
    host: CustomPanelAPI,
    w: Float = 200f,
    h: Float = 100f,
    private val color: Color = Color(220, 20, 60),
) : BaseCustomUIPanelPlugin() {

    val panel: CustomPanelAPI = host.createCustomPanel(w, h, this)

    private val label: LabelAPI

    private enum class Handle { NONE, MOVE, LO_X, HI_X, LO_Y, HI_Y, LO_X_LO_Y, HI_X_LO_Y, LO_X_HI_Y, HI_X_HI_Y }

    private var activeHandle: Handle = Handle.NONE
    private var anchorMouseX = 0f
    private var anchorMouseY = 0f
    private var anchorTlX = 0f
    private var anchorTlY = 0f
    private var anchorW = 0f
    private var anchorH = 0f

    private var tlX = 0f
    private var tlY = 0f

    init {
        val tooltip = panel.createUIElement(w, h, false)
        label = tooltip.addPara("-", 0f)
        tooltip.position.inTL(4f, 4f)
        panel.addUIElement(tooltip)
    }

    fun placeInTL(host: CustomPanelAPI, tlX: Float, tlY: Float) {
        this.tlX = tlX
        this.tlY = tlY
        host.addComponent(panel).inTL(tlX, tlY)
    }

    private fun moveTo(newTlX: Float, newTlY: Float) {
        tlX = newTlX
        tlY = newTlY
        panel.position.inTL(newTlX, newTlY)
    }

    override fun render(alphaMult: Float) {
        val p = panel.position
        drawOutline(p.x, p.y, p.width, p.height, alphaMult)
    }

    override fun advance(amount: Float) {
        // Display host-relative top-down coords (inTL-compatible), not OpenGL screen-space.
        val p = panel.position
        val w = p.width
        val h = p.height
        label.text = "TL=(${fmt(tlX)}, ${fmt(tlY)})\n" +
                "TR=(${fmt(tlX + w)}, ${fmt(tlY)})\n" +
                "BL=(${fmt(tlX)}, ${fmt(tlY + h)})\n" +
                "BR=(${fmt(tlX + w)}, ${fmt(tlY + h)})\n" +
                "w=${fmt(w)}  h=${fmt(h)}"
    }

    override fun processInput(events: List<InputEventAPI>) {
        events.forEach { evt ->
            if (evt.isConsumed) return@forEach

            when {
                evt.isLMBDownEvent && contains(evt.x.toFloat(), evt.y.toFloat()) -> {
                    activeHandle = handleAt(evt.x.toFloat(), evt.y.toFloat())
                    anchorMouseX = evt.x.toFloat()
                    anchorMouseY = evt.y.toFloat()
                    anchorTlX = tlX
                    anchorTlY = tlY
                    anchorW = panel.position.width
                    anchorH = panel.position.height
                    evt.consume()
                }
                evt.isLMBUpEvent && activeHandle != Handle.NONE -> {
                    activeHandle = Handle.NONE
                    evt.consume()
                }
                evt.isMouseMoveEvent && activeHandle != Handle.NONE -> {
                    // evt.y is OpenGL Y-up; flip to the top-down convention used by inTL / tlY.
                    applyHandle(activeHandle, evt.x - anchorMouseX, -(evt.y - anchorMouseY))
                }
                evt.isKeyDownEvent -> handleKey(evt)
            }
        }
    }

    private fun handleKey(evt: InputEventAPI) {
        val step = if (evt.isShiftDown) 10f else 1f
        when (evt.eventValue) {
            Keyboard.KEY_LEFT -> { moveTo(tlX - step, tlY); evt.consume() }
            Keyboard.KEY_RIGHT -> { moveTo(tlX + step, tlY); evt.consume() }
            Keyboard.KEY_UP -> { moveTo(tlX, tlY - step); evt.consume() }
            Keyboard.KEY_DOWN -> { moveTo(tlX, tlY + step); evt.consume() }
            Keyboard.KEY_ADD, Keyboard.KEY_EQUALS -> { resizeUniform(+25f); evt.consume() }
            Keyboard.KEY_SUBTRACT, Keyboard.KEY_MINUS -> { resizeUniform(-25f); evt.consume() }
        }
    }

    private fun resizeUniform(delta: Float) {
        val p = panel.position
        val newW = (p.width + delta).coerceAtLeast(MIN_W)
        val newH = (p.height + delta).coerceAtLeast(MIN_H)
        p.setSize(newW, newH)
    }

    private fun handleAt(mouseX: Float, mouseY: Float): Handle {
        val p = panel.position
        val margin = minOf(8f, minOf(p.width, p.height) / 3f)
        val nearLoX = mouseX <= p.x + margin
        val nearHiX = mouseX >= p.x + p.width - margin
        val nearLoY = mouseY <= p.y + margin
        val nearHiY = mouseY >= p.y + p.height - margin
        return when {
            nearLoX && nearLoY -> Handle.LO_X_LO_Y
            nearLoX && nearHiY -> Handle.LO_X_HI_Y
            nearHiX && nearLoY -> Handle.HI_X_LO_Y
            nearHiX && nearHiY -> Handle.HI_X_HI_Y
            nearLoX -> Handle.LO_X
            nearHiX -> Handle.HI_X
            nearLoY -> Handle.LO_Y
            nearHiY -> Handle.HI_Y
            else -> Handle.MOVE
        }
    }

    private fun applyHandle(h: Handle, dx: Float, dy: Float) {
        var newTlX = anchorTlX
        var newTlY = anchorTlY
        var w = anchorW
        var hgt = anchorH
        val touchLoX = h == Handle.LO_X || h == Handle.LO_X_LO_Y || h == Handle.LO_X_HI_Y
        val touchHiX = h == Handle.HI_X || h == Handle.HI_X_LO_Y || h == Handle.HI_X_HI_Y
        val touchLoY = h == Handle.LO_Y || h == Handle.LO_X_LO_Y || h == Handle.HI_X_LO_Y
        val touchHiY = h == Handle.HI_Y || h == Handle.LO_X_HI_Y || h == Handle.HI_X_HI_Y
        if (h == Handle.MOVE) {
            newTlX = anchorTlX + dx
            newTlY = anchorTlY + dy
        }
        if (touchLoX) {
            w = (anchorW - dx).coerceAtLeast(MIN_W)
            newTlX = anchorTlX + (anchorW - w)
        } else if (touchHiX) {
            w = (anchorW + dx).coerceAtLeast(MIN_W)
        }
        if (touchLoY) {
            // Handle.LO_Y = near low OpenGL Y = visual BOTTOM edge. Top stays put, height tracks the drag.
            hgt = (anchorH + dy).coerceAtLeast(MIN_H)
        } else if (touchHiY) {
            // Handle.HI_Y = near high OpenGL Y = visual TOP edge. Top moves with the cursor, height inverts.
            hgt = (anchorH - dy).coerceAtLeast(MIN_H)
            newTlY = anchorTlY + (anchorH - hgt)
        }
        if (h == Handle.NONE) return
        moveTo(newTlX, newTlY)
        panel.position.setSize(w, hgt)
    }

    private fun contains(x: Float, y: Float): Boolean {
        val p = panel.position
        return x >= p.x && x <= p.x + p.width && y >= p.y && y <= p.y + p.height
    }

    private fun drawOutline(x: Float, y: Float, w: Float, h: Float, alphaMult: Float) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_CURRENT_BIT or GL11.GL_LINE_BIT)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            alphaMult.coerceIn(0f, 1f),
        )
        GL11.glLineWidth(2f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x + w, y)
        GL11.glVertex2f(x + w, y + h)
        GL11.glVertex2f(x, y + h)
        GL11.glEnd()
        GL11.glPopAttrib()
    }

    private fun fmt(v: Float): String = "%.0f".format(v)

    private companion object {
        const val MIN_W = 60f
        const val MIN_H = 60f
    }
}
