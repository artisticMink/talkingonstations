package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosSettings
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.ui.TriChat.TriChatCustomVisualPanel

/**
 * Hook, gets executed whenever a conversation with a market contact is started.
 * @see /data/campaign/rules.csv
 */
class TosBeginConversation : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token?>,
        memoryMap: MutableMap<String?, MemoryAPI?>?
    ): Boolean {
        if (dialog == null) return false

        val person = dialog.interactionTarget.activePerson

        // Hijack if this is a ToS person
        if (person == null || !person.memory.getBoolean(TosMemoryKeys.CHAT_ENABLED)) return false

        // We need at least one HTTP API to make use of the UI
        if (HttpApiRegistry.getDefaultApi() == null) return false

        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            person.market
        )
        chat.beforeContinueAsPlayer = { message -> dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor()) }
        chat.afterChatResponse = { message -> dialog.textPanel.addPara(message) }

        val chatUi = TriChatCustomVisualPanel(
            dialog.visualPanel,
            Global.getSector().playerPerson,
            person,
        )

        chatUi.onModelSelectClick = { modelSettings -> chat.modelSettings = modelSettings }
        chatUi.onRetryButtonClick = { dialog.textPanel.replaceLastParagraph(""); chat.retryLastMessage() }
        chatUi.onSendButtonClick = { message -> chat.continueChatAsPlayer(message) }
        chatUi.onPlayerQuit = {
            // Persistent character memory
            // Assumes the summary mixin is active, so
            // we have the previous summary somewhere within the context
            if (TosSettings.enableCharacterMemory) {
                chat.summarizeToNpcMemory()
            }

            dialog.dismiss()
        }

        dialog.interactionTarget.activePerson = person

        return true
    }
}