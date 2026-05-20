package maver.talkingonstations.llm.dto

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosEveryFrameScriptQueue
import maver.talkingonstations.ui.TriChat.TriChatCustomVisualPanel

class ConversationUi(private val dialog: InteractionDialogAPI, private val triChat: TriChatCustomVisualPanel) :
    ConversationUiInterface {
    override fun forceEnd(
        lastMessage: String,
        notice: String?
    ) = TosEveryFrameScriptQueue.add {
        triChat.getSendButton()?.isEnabled = false
        triChat.getRetryButton()?.isEnabled = false
        dialog.textPanel.addPara(lastMessage)
        dialog.textPanel.addPara(
            notice ?: "Your comms officer informs you that the connection has been severed.",
            Misc.getHighlightColor()
        )
    }
}