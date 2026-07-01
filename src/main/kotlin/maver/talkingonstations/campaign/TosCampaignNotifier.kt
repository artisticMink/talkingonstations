package maver.talkingonstations.campaign

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosEveryFrameScriptQueue

object TosCampaignNotifier {
    fun enqueue(text: String, highlight: String? = null) = TosEveryFrameScriptQueue.add {
        val ui = Global.getSector()?.campaignUI ?: return@add

        if (highlight != null)
            ui.addMessage(
                text,
                Misc.getTextColor(),
                highlight,
                highlight,
                Misc.getHighlightColor(),
                Misc.getHighlightColor())
        else
            ui.addMessage(
                text,
                Misc.getTextColor()
            )
    }

}