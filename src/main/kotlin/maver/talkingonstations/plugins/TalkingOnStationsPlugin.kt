package maver.talkingonstations.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.events.ChatBarEventWithPersonCreator
import maver.talkingonstations.mods.lunasettings.LunaSettingsRegistration

internal class TalkingOnStationsPlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        LunaSettingsRegistration.register()
        TosEveryFrameScriptQueue.setup()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        TosEveryFrameScriptQueue.setup()
        TosRegistry.initialize()

        val barEventManager = BarEventManager.getInstance()
        if (!barEventManager.hasEventCreator(ChatBarEventWithPersonCreator::class.java)) {
            // The default random person bar event, available whenever a player visits a market.
            barEventManager.addEventCreator(ChatBarEventWithPersonCreator())
        }

        // Add ability to view the player profile
        val characterData = Global.getSector().characterData
        if (!characterData.abilities.contains("tos_opentrichat")) {
            characterData.addAbility("tos_opentrichat")
        }

    }
}