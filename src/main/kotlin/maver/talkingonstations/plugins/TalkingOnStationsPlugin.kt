package maver.talkingonstations.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import maver.talkingonstations.TosSettings
import maver.talkingonstations.events.ChatBarEventWithPersonCreator

internal class TalkingOnStationsPlugin : BaseModPlugin() {
    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        TosSettings.initialize()

        val barEventManager = BarEventManager.getInstance()
        if (!barEventManager.hasEventCreator(ChatBarEventWithPersonCreator::class.java)) {
            barEventManager.addEventCreator(ChatBarEventWithPersonCreator())
        }
    }
}