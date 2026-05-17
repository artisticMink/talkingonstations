package maver.talkingonstations.characters.market
import com.fs.starfarer.api.Global

class LobsterFlipper() : MarketPersonInterface {
    override lateinit var id: String

    override fun canUse(): Boolean = Global.getSettings().modManager.isModEnabled("uaf")

    override fun getInstructions(): String {
        return ""
    }

    override fun getBackground(): String {
        return ""
    }
}