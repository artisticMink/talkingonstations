package maver.talkingonstations.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import java.awt.Color

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

    private fun draw(sprite: SpriteAPI?, x: Float, y: Float, w: Float, h: Float, alpha: Float, tint: Color?) {
        if (sprite == null || w <= 0f || h <= 0f) return
        sprite.setSize(w, h)
        sprite.color = tint ?: Color.WHITE
        sprite.alphaMult = alpha
        sprite.render(x, y)
    }

    /**
     * @param x,y  bottom-left corner of the frame
     * @param w,h  frame size
     * @param alphaMult  pass through the value renderBelow receives
     */
    fun render(x: Float, y: Float, w: Float, h: Float, alphaMult: Float) {
        val cw = topLeft?.width ?: 32f
        val ch = topLeft?.height ?: 32f
        val innerW = w - cw * 2f
        val innerH = h - ch * 2f

        if (fillCenter) draw(center, x + cw, y + ch, innerW, innerH, alphaMult, null)

        // edges (stretched along their run)
        draw(bot, x + cw, y, innerW, ch, alphaMult, borderTint)
        draw(top, x + cw, y + h - ch, innerW, ch, alphaMult, borderTint)
        draw(left, x, y + ch, cw, innerH, alphaMult, borderTint)
        draw(right, x + w - cw, y + ch, cw, innerH, alphaMult, borderTint)

        // corners (native size)
        draw(botLeft, x, y, cw, ch, alphaMult, borderTint)
        draw(botRight, x + w - cw, y, cw, ch, alphaMult, borderTint)
        draw(topLeft, x, y + h - ch, cw, ch, alphaMult, borderTint)
        draw(topRight, x + w - cw, y + h - ch, cw, ch, alphaMult, borderTint)
    }
}
