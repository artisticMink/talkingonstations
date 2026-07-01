package maver.talkingonstations.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import java.awt.Color

/**
 * Border running around a panel.
 *
 * 8 + 1 sprites for border and fill.
 */
class DecorativeFrame(private val tileSet: String = "graphics/ui/bgs/panel00") {

    private fun load(suffix: String): SpriteAPI? {
        val path = "${tileSet}_$suffix.png"
        return try {
            Global.getSettings().loadTexture(path)
            Global.getSettings().getSprite(path)
        } catch (e: Exception) {
            null
        }
    }

    private val topLeft = load("top_left")
    private val top = load("top")
    private val topRight = load("top_right")
    private val left = load("left")
    private val center = load("center")
    private val right = load("right")
    private val botLeft = load("bot_left")
    private val bot = load("bot")
    private val botRight = load("bot_right")

    var borderTint: Color? = null

    var fillCenter: Boolean = true

    /**
     * Draws the sprite at x,y
     */
    private fun draw(sprite: SpriteAPI?, x: Float, y: Float, width: Float, height: Float, alpha: Float, tint: Color?) {
        if (sprite == null || width <= 0f || height <= 0f) return

        sprite.setSize(width, height)
        sprite.color = tint ?: Color.WHITE
        sprite.alphaMult = alpha
        sprite.render(x, y)
    }

    /**
     * Render the border of a rectangle.
     */
    fun render(startX: Float, startY: Float, width: Float, height: Float, alphaMult: Float) {
        val cw = topLeft?.width ?: 32f
        val ch = topLeft?.height ?: 32f
        val innerWidth = width - cw * 2f
        val innerHeight = height - ch * 2f

        if (fillCenter) draw(center, startX + cw, startY + ch, innerWidth, innerHeight, alphaMult, null)

        // Edges
        draw(bot, startX + cw, startY, innerWidth, ch, alphaMult, borderTint)
        draw(top, startX + cw, startY + height - ch, innerWidth, ch, alphaMult, borderTint)
        draw(left, startX, startY + ch, cw, innerHeight, alphaMult, borderTint)
        draw(right, startX + width - cw, startY + ch, cw, innerHeight, alphaMult, borderTint)

        // Corners
        draw(botLeft, startX, startY, cw, ch, alphaMult, borderTint)
        draw(botRight, startX + width - cw, startY, cw, ch, alphaMult, borderTint)
        draw(topLeft, startX, startY + height - ch, cw, ch, alphaMult, borderTint)
        draw(topRight, startX + width - cw, startY + height - ch, cw, ch, alphaMult, borderTint)
    }
}
