package maver.talkingonstations.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.ScrollPanelAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.util.EnumSet

/**
 * Displays a text area
 */
class TextArea (
    private val parent: TooltipMakerAPI,
    private val rows: Int = 5,
    private val maxRows: Int = 10,
    private val rowHeight: Float = 20f,
    private val fontPath: String = Fonts.ORBITRON_12,
    private val height: Float = 200f,
    private val width: Float = 500f,
): CustomUIPanelPlugin {
    private val textFields: MutableList<TextFieldAPI> = mutableListOf()
    private val state: MutableSet<TextAreaState> = EnumSet.noneOf(TextAreaState::class.java)

    private val scroller: ScrollPanelAPI
    private val tabulatorWidth: Float = stringWidth("    ")

    private var panel: CustomPanelAPI = Global.getSettings().createCustom(width, height, this)
    private var tooltip: TooltipMakerAPI = panel.createUIElement(width, height, true)
    private var activeIndex: Int = 0
    private var isCtrlPressed: Boolean = false

    enum class TextAreaState {
        FOCUS,
        EDIT,
        SELECTED,
    }

    data class ComponentBoundary(
        val topLeft: Vector2f,
        val topRight: Vector2f,
        val bottomLeft: Vector2f,
        val bottomRight: Vector2f
    )

    data class RowVisibility(val isVisible: Boolean, val position: Float, val isAbove: Boolean, val isBelow: Boolean)

    init {
        parent.addCustom(panel, 0f)

        repeat(rows) { textFields.add(appendEmptyTextField()) }
        panel.addUIElement(tooltip).inTL(0f, 0f)

        scroller = tooltip.externalScroller
        state.add(TextAreaState.EDIT)
    }

    /**
     * Returns a string containing the text area content
     * LF separates lines
     */
    fun getText(from: Int = 0, to: Int = textFields.lastIndex): String {
        val text = textFields.subList(from, to + 1).joinToString("\n") { it.text }
        return text.trimEnd('\n')
    }

    /**
     * Sets the text for the text area
     * Supports multi-line strings. Supports CRLF, LF or CR
     */
    fun setText(text: String) {
        clearText()
        insertText(text)
    }

    /**
     * Clears the entire text area
     */
    fun clearText(from: Int = 0, to: Int = textFields.lastIndex) {
        textFields.subList(from, to + 1).forEach { it.text = "" }
        activeIndex = from
        getActiveRow().grabFocus()
    }

    /**
     * Returns the active states of the component
     */
    fun getState(): Set<TextAreaState> {
        return state.toSet()
    }

    /**
     * Returns the currently selected text field
     */
    fun getActiveRow(offset: Int = 0): TextFieldAPI {
        val requestedRow = activeIndex + offset
        if (requestedRow !in 0..textFields.lastIndex) throw Exception("Active TextField index is out of bounds: $activeIndex, max: ${textFields.lastIndex}")

        return textFields[requestedRow]
    }

    /**
     * Returns the rectangular boundary of this component
     */
    fun getBounds(): ComponentBoundary {
        return ComponentBoundary(
            Vector2f(tooltip.position.x, tooltip.position.y),
            Vector2f(tooltip.position.x + tooltip.position.width, tooltip.position.y),
            Vector2f(tooltip.position.x, tooltip.position.y + tooltip.position.height),
            Vector2f(tooltip.position.x + tooltip.position.width, tooltip.position.y + tooltip.position.height),
        )
    }

    fun getPosition(): PositionAPI? {
        return panel.position
    }

    override fun positionChanged(p0: PositionAPI?) {

    }

    override fun renderBelow(p0: Float) {

    }

    override fun render(p0: Float) {

    }

    override fun advance(p0: Float) {

    }

    /**
     * Handles input events in cooperation with
     * @see TextFieldInterceptor
     */
    override fun processInput(events: MutableList<InputEventAPI>) {
        if (!state.contains(TextAreaState.EDIT)) return

        events.forEach { event ->
            if (event.isConsumed || !event.isKeyDownEvent) return@forEach

            when (event.eventValue) {
                Keyboard.KEY_UP -> {
                    moveFocusUp()
                    event.consume()
                }

                Keyboard.KEY_DOWN -> {
                    moveFocusDown()
                    event.consume()
                }

                Keyboard.KEY_TAB -> {
                    handleTabulator()
                    event.consume()
                }
            }

            if (!isCtrlPressed &&
                event.isKeyDownEvent &&
                state.contains(TextAreaState.SELECTED)
            ) clearSelection()
        }
    }

    override fun buttonPressed(p0: Any?) {

    }

    /**
     * Returns the text area scroller
     */
    fun getScroller(): ScrollPanelAPI {
        return this.scroller
    }

    /**
     * Determine the visibility of a row by calculating the upper and lower
     * boundary of the tooltip and checking if the assumed row position is in-between them.
     */
    private fun isRowVisible(rowIndex: Int): RowVisibility {
        val maxVisibleRows = height / rowHeight
        val tooltipBottomY = panel.position.height - tooltip.position.height / rowHeight
        val firstVisibleRowY = scroller.yOffset / scroller.position.height * tooltipBottomY
        val lastVisibleRowY = firstVisibleRowY + (maxVisibleRows * rowHeight)
        val rowPosition = rowHeight + (rowIndex * rowHeight)

        return RowVisibility(
            rowPosition in firstVisibleRowY..lastVisibleRowY,
            rowPosition,
            (rowPosition < firstVisibleRowY),
            (rowPosition > lastVisibleRowY)
        )
    }

    private fun updateModifierKeys(event: InputEventAPI): Boolean {
        if (event.isKeyDownEvent && !isCtrlPressed) {
            if (event.eventValue == Keyboard.KEY_LCONTROL || event.eventValue == Keyboard.KEY_RCONTROL) {
                isCtrlPressed = true
                return true
            }
        } else if (event.isKeyUpEvent && isCtrlPressed) {
            if (event.eventValue == Keyboard.KEY_LCONTROL || event.eventValue == Keyboard.KEY_RCONTROL) {
                isCtrlPressed = false
                return true
            }
        }

        return false
    }

    private fun handleLineWrap() {
        if (activeIndex >= textFields.lastIndex) return
        val currentField = getActiveRow()
        if (currentField.text.isEmpty() || !stringShouldWrap(currentField.text)) return

        val canAddRow = textFields.size < maxRows
        val lastLineHasText = textFields.last().text.isNotEmpty()
        if (lastLineHasText && !canAddRow) {
            currentField.text = currentField.text.dropLast(1)
            moveFocusDown()

        }

        if (lastLineHasText) {
            addRow()
        }

        shiftLinesDown(activeIndex + 1)
        getActiveRow(1).text = currentField.text.last().toString()
        currentField.text = currentField.text.dropLast(1)

        moveFocusDown()
    }

    private fun stringShouldWrap(text: String): Boolean {
        return stringWidth(text) > width - 1
    }

    private fun stringWidth(text: String): Float {
        return Global.getSettings().computeStringWidth(text, fontPath)
    }

    private fun handleBackspace() {
        if (activeIndex == 0) return

        val currentField = getActiveRow()

        if (currentField.text.isEmpty()) {
            val previousField = getActiveRow(-1)
            val previousText = previousField.text

            moveFocus(-1)
            shiftLinesUp(activeIndex + 1)
            previousField.text = previousText

            if (textFields.size > rows) removeRow()
        }
    }

    private fun handleDelete() {
        if (activeIndex == textFields.lastIndex) return

        if (state.contains(TextAreaState.SELECTED)) {
            clearSelection()
            clearText()
            return
        }

        val onlyEmptyFieldsBelow = onlyEmptyFieldsBelow()
        if (onlyEmptyFieldsBelow && textFields.size == rows) return

        val nextField = getActiveRow(1)
        val nextFieldEmpty = nextField.text.isEmpty()

        if (onlyEmptyFieldsBelow && textFields.size > rows) {
            removeRow()
        } else if (nextFieldEmpty) {
            shiftLinesUp(activeIndex + 1)
            if (textFields.size > rows) removeRow()
        } else {
            val currentField = getActiveRow()
            val freeSpace = width - stringWidth(currentField.text)
            if (freeSpace > stringWidth(nextField.text)) {
                currentField.text += nextField.text
                nextField.text = ""
                shiftLinesUp(activeIndex + 1)
                if (textFields.size > rows) removeRow()
            } else {
                nextField.text = nextField.text.toCharArray().drop(1).joinToString("")
            }
        }
    }

    private fun handleTabulator() {
        val currentField = getActiveRow()

        if (stringWidth(currentField.text) + tabulatorWidth < width) currentField.text += "    ";
    }

    private fun handleEnter() {
        if (activeIndex == textFields.lastIndex) {
            addRow()
            moveFocusDown()
        } else if (onlyEmptyFieldsBelow()) {
            moveFocusDown()
        } else if (activeIndex < rows && textFields.last().text.isEmpty()) {
            shiftLinesDown(activeIndex + 1)
            moveFocusDown()
        } else if (textFields.size < maxRows) {
            addRow()
            shiftLinesDown(activeIndex + 1)
            moveFocusDown()
        } else {
            getActiveRow().grabFocus()
        }
    }

    private fun onlyEmptyFieldsBelow(offset: Int = 1): Boolean {
        for (i in activeIndex + offset until textFields.size) {
            if (textFields[i].text.isNotEmpty()) {
                return false
            }
        }

        return true
    }

    private fun shiftLinesUp(from: Int) {
        if (from < 0 || from >= textFields.size) {
            return
        }

        for (i in from until textFields.size - 1) {
            textFields[i].text = textFields[i + 1].text
        }

        textFields.last().text = ""
    }

    private fun shiftLinesDown(from: Int) {
        if (from < 0 || from >= textFields.size) {
            return
        }

        for (i in textFields.lastIndex - 1 downTo from) {
            textFields[i + 1].text = textFields[i].text
        }

        textFields[from].text = ""
    }

    private fun moveFocusUp() {
        if (activeIndex > 0) {
            moveFocus(-1)
            makeActiveRowVisible()
        }
    }

    private fun moveFocusDown() {
        if (activeIndex < textFields.lastIndex) {
            moveFocus(1)
            makeActiveRowVisible()
        }
    }

    private fun moveFocus(delta: Int) {
        if (textFields.isEmpty()) return

        val newIndex = (activeIndex + delta).coerceIn(0, textFields.lastIndex)

        if (newIndex == activeIndex) return

        activeIndex = newIndex
        textFields[newIndex].grabFocus()
    }

    private fun makeActiveRowVisible() {
        val (isVisible, position, isAbove, isBelow) = isRowVisible(activeIndex)

        if (!isVisible) {
            if (isAbove) scroller.yOffset = (position - 3 * rowHeight).coerceAtLeast(0f)
            else if (isBelow) scroller.yOffset = position + 30
        }
    }

    private fun selectAll() {
        textFields.forEach { field ->
            field.textLabelAPI.setHighlightColor(Misc.getHighlightColor())
            field.textLabelAPI.setHighlight(0, field.text.length)
        }

        state.add(TextAreaState.SELECTED)
    }

    private fun clearSelection() {
        textFields.forEach { field -> field.textLabelAPI.setHighlight(-1, -1) }

        state.remove(TextAreaState.SELECTED)
    }

    private fun addRow() {
        if (textFields.size >= maxRows) return

        textFields.add(appendEmptyTextField())
    }

    private fun removeRow() {
        tooltip.removeComponent(textFields.last())
        textFields.removeLast()
    }

    private fun copyText() {
        try {
            val selection = StringSelection(getText(0, textFields.lastIndex))
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            clearSelection()
        } catch (e: Exception) {
            Global.getLogger(this::class.java).error("Failed to copy text", e)
        }
    }

    private fun pasteText() {
        try {
            val clipboardText = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
            setText(clipboardText)
        } catch (e: Exception) {
            Global.getLogger(this::class.java).error("Failed to paste text", e)
        }
    }

    private fun insertText(text: String, fromIndex: Int = 0): Int {
        //ToDo: Rework
        /**
         * var currentIndex = fromIndex
         *         for (line in text.lines()) {
         *             if (currentIndex >= maxRows) break
         *
         *             val charsPerRow = Global.getSettings().computeStringWidth(line, fontPath).toInt() - 1
         *             stringShouldWrap()
         *             for (segment in line.chunked(charsPerRow)) {
         *                 if (currentIndex > textFields.lastIndex && textFields.size < maxRows) {
         *                     appendEmptyTextField()
         *                 }
         *
         *                 textFields[currentIndex].text = segment
         *                 currentIndex++
         *             }
         *         }
         *
         *         return currentIndex
         */

        return 0
    }

    private fun appendEmptyTextField(): TextFieldAPI {
        val textField = tooltip.addTextField(width + 4f, rowHeight, fontPath, -2f)
        val uiComponent: CustomPanelAPI = Global.getSettings().createCustom(
            0f,
            0f,
            TextFieldInterceptor(this)
        )

        textField.isLimitByStringWidth = false
        textField.isUndoOnEscape = false
        textField.setBgColor(Color.BLACK)
        textField.borderColor = Color.BLACK
        textField.isHandleCtrlV = false


        // Workaround to prevent the offset from the left
        if (textFields.isEmpty()) textField.position.setXAlignOffset(-4f)

        (textField as UIPanelAPI).addComponent(uiComponent)

        return (textField as TextFieldAPI)
    }

    /**
     * Interceptor component responsible for grabbing events before the text field can consume them.
     */
    private class TextFieldInterceptor(val textArea: TextArea) : BaseCustomUIPanelPlugin() {
        override fun processInput(events: List<InputEventAPI>) {
            if (textArea.state.contains(TextAreaState.EDIT))
                events.forEach { event ->
                    if (event.isConsumed) return@forEach

                    if (event.isKeyboardEvent && textArea.updateModifierKeys(event)) {
                        event.consume()
                        return@forEach
                    }

                    when {
                        event.isKeyDownEvent -> {
                            textArea.handleLineWrap()

                            when (event.eventValue) {
                                Keyboard.KEY_ESCAPE -> {
                                    if (textArea.state.contains(TextAreaState.SELECTED))
                                        textArea.clearSelection()
                                    event.consume()
                                }

                                Keyboard.KEY_DELETE -> {
                                    textArea.handleDelete()
                                    event.consume()
                                }

                                Keyboard.KEY_RETURN -> {
                                    textArea.handleEnter()
                                    event.consume()
                                }

                                Keyboard.KEY_BACK -> {
                                    val textField = textArea.getActiveRow()
                                    if (textField.text.isEmpty()) {
                                        textArea.handleBackspace()
                                        event.consume()
                                    }
                                }

                                Keyboard.KEY_A -> {
                                    if (textArea.isCtrlPressed) {
                                        textArea.selectAll()
                                        textArea.isCtrlPressed = false
                                        event.consume()
                                    }
                                }

                                Keyboard.KEY_C -> {
                                    if (textArea.isCtrlPressed) {
                                        textArea.copyText()
                                        textArea.isCtrlPressed = false
                                        event.consume()
                                    }
                                }

                                Keyboard.KEY_V -> {
                                    if (textArea.isCtrlPressed) {
                                        textArea.pasteText()
                                        textArea.isCtrlPressed = false
                                        event.consume()
                                    }
                                }
                            }
                        }

                        event.isMouseDownEvent -> {
                            val hasFocus = textArea.state.contains(TextAreaState.FOCUS)
                            val inBounds = isInBounds(event)

                            when {
                                !hasFocus && inBounds -> {
                                    textArea.state.add(TextAreaState.FOCUS)
                                    textArea.textFields.first().grabFocus()
                                    event.consume()
                                }

                                hasFocus && inBounds -> {
                                    textArea.clearSelection()
                                    event.consume()
                                }

                                hasFocus && !inBounds -> {
                                    textArea.activeIndex = 0
                                    textArea.state.remove(TextAreaState.FOCUS)
                                    textArea.clearSelection()
                                }
                            }
                        }
                    }
                }

            super.processInput(events)
        }

        private fun isInBounds(event: InputEventAPI): Boolean {
            val bounds = textArea.getBounds()
            return event.x > bounds.topLeft.x &&
                    event.x < bounds.topRight.x &&
                    event.y > bounds.topRight.y &&
                    event.y < bounds.bottomRight.y
        }
    }


}