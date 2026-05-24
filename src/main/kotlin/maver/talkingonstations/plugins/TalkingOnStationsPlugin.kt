package maver.talkingonstations.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.events.ChatBarEventWithPersonCreator

internal class TalkingOnStationsPlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        TosEveryFrameScriptQueue.setup()

    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        // Load csv, json, etc.
        if (!TosRegistry.isInitialized()) {
            TosRegistry.initialize()
        }

        val barEventManager = BarEventManager.getInstance()
        if (!barEventManager.hasEventCreator(ChatBarEventWithPersonCreator::class.java)) {
            // The default random person bar event, available whenever a player visits a market.
            barEventManager.addEventCreator(ChatBarEventWithPersonCreator())
        }
    }
}