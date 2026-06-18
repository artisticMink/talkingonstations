package maver.talkingonstations.ui

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

/**
 * A card showing a persons portrait and details
 * Two columns.
 */
class ProfileCard(
    private val person: PersonAPI,
    val width: Float = 260f,
    val height: Float = 168f,
    private val pad: Float = 12f,
    private val portraitSize: Float = 84f,
    private val crestSize: Float = 28f,
    frameTileSet: String = "graphics/ui/bgs/panel01",
) {
    private val frame = DecorativeFrame(frameTileSet)

    fun addContent(panel: CustomPanelAPI, leftX: Float, topY: Float) {
        val contentW = width - pad * 2f
        val contentH = height - pad * 2f
        val columnWidth = contentW / 2
        val leftColumn: TooltipMakerAPI = panel.createUIElement(columnWidth, contentH, false)
        leftColumn.position.inTL(leftX + pad, topY + pad)

        leftColumn.addImage(person.portraitSprite, portraitSize, 4f)
        leftColumn.addPara(person.nameString, 6f)

        val rightColumn: TooltipMakerAPI = panel.createUIElement(columnWidth, contentH, false)
        rightColumn.position.inTL(leftX + columnWidth + pad, topY + pad)

        rightColumn.addPara(getRankLabel(), 0f)

        if (person.faction.displayName !== "Your")
            rightColumn.addPara(person.faction.displayName, person.faction.baseUIColor, 0f)

        //val crest = person.faction.crest
        //if (!crest.isNullOrEmpty()) rightColumn.addImage(crest, crestSize, 6f)

        panel.addUIElement(leftColumn)
        panel.addUIElement(rightColumn)
    }

    fun renderFrame(hostPos: PositionAPI, leftX: Float, topY: Float, alphaMult: Float) {
        val frameY = (hostPos.y + hostPos.height) - topY - height
        frame.render(hostPos.x + leftX, frameY, width, height, alphaMult)
    }

    fun getRankLabel() =
        if (!person.rank.isNullOrEmpty() && !person.rank.contains("Unknown")) "${person.post} (${person.rank})"
        else (person.post ?: "")
}
