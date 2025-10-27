package maver.talkingonstations.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import maver.talkingonstations.events.DebugChatBarEventWithPersonCreator

internal class TalkingOnStationsPlugin : BaseModPlugin() {
    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        val barEventManager = BarEventManager.getInstance()
        if (Global.getSettings().isDevMode) {
            if (!barEventManager.hasEventCreator(DebugChatBarEventWithPersonCreator::class.java)) {
                barEventManager.addEventCreator(DebugChatBarEventWithPersonCreator())
            }
        }
    }
}