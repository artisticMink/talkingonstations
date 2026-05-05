package maver.talkingonstations.events

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import maver.talkingonstations.campaign.intel.bar.events.ChatBarEventWithPerson

class ChatBarEventWithPersonCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent {
        return ChatBarEventWithPerson()
    }
}