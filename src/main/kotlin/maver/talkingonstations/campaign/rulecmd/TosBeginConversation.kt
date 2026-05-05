package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosStrings
import maver.talkingonstations.ui.TriChat.TriChatCustomVisualPanel
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import maver.talkingonstations.chat.Chat

/**
 * Hook, gets executed whenever a conversation with a market contact is started.
 * ToDo: Condition whether it should hijack the conversation or not
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
        if (person == null || !person.memory.getBoolean(TosStrings.MemoryId.CHAT_ENABLED)) return false

        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            person.market
        )
        chat.beforeContinueAsPlayer = { message -> dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor()) }
        chat.afterChatResponse = { message -> dialog.textPanel.addPara(message) }

        //val personExtensionData: PersonExtensionData = TosRegistry.getMarketPersons()[person] ?: PersonExtensionData()

        val chatUi = TriChatCustomVisualPanel(
            dialog.visualPanel,
            Global.getSector().playerPerson,
        )

        chatUi.onModelSelectClick = { modelSettings -> chat.setModelSettings(modelSettings) }
        chatUi.onRetryButtonClick = { dialog.textPanel.replaceLastParagraph(""); chat.retryLastMessage() }
        chatUi.onSendButtonClick = { message -> chat.continueChatAsPlayer(message) }
        chatUi.onPlayerQuit = { dialog.dismiss() }

        var minimal = false
        var showRel = true
        if (params.size > 1) {
            minimal = params[1]!!.getBoolean(memoryMap)
        }
        if (params.size > 2) {
            showRel = params[2]!!.getBoolean(memoryMap)
        }

        //dialog.interactionTarget.activePerson = person
        //(dialog.plugin as RuleBasedDialog).notifyActivePersonChanged()
        dialog.visualPanel.showPersonInfo(person, minimal, showRel)

        return true
    }
}