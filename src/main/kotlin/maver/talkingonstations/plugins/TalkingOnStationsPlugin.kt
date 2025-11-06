package maver.talkingonstations.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import maver.talkingonstations.events.ChatBarEventWithPersonCreator

internal class TalkingOnStationsPlugin : BaseModPlugin() {
    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)



        val barEventManager = BarEventManager.getInstance()
        if (!barEventManager.hasEventCreator(ChatBarEventWithPersonCreator::class.java)) {
            //barEventManager.addEventCreator(DebugChatBarEventWithPersonCreator())
            barEventManager.addEventCreator(ChatBarEventWithPersonCreator())
        }
    }
}