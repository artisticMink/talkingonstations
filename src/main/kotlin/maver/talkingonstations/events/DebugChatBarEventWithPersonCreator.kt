package maver.talkingonstations.events

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import maver.talkingonstations.campaign.intel.bar.events.DebugChatBarEventWithPerson

class DebugChatBarEventWithPersonCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent {
        return DebugChatBarEventWithPerson()
    }
}