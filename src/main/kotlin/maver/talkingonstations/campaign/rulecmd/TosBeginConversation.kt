package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosSettings
import maver.talkingonstations.campaign.BarChatCustomUiPanel
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.llm.dto.Instruction

class TosBeginConversation : BaseCommandPlugin() {

    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token?>,
        memoryMap: MutableMap<String?, MemoryAPI?>?
    ): Boolean {
        if (dialog == null) return false

        val person = dialog.interactionTarget.activePerson
        //val person: PersonAPI = params[0]!!.getObject(memoryMap) as PersonAPI? ?: return false

        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            person.market
        )
        chat.beforeContinueAsPlayer = { message -> dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor()) }
        chat.afterChatResponse = { message -> dialog.textPanel.addPara(message) }

        val personExtensionData: PersonExtensionData = TosSettings.getMarketPersons()[person] ?: PersonExtensionData()

        // Overwrite instructions with character-specific ones
        if (personExtensionData.instructions.isNotEmpty()) {
            chat.updateInstruction(Instruction("main", personExtensionData.instructions))
        }

        // Build our own context when applicable.
        if (personExtensionData.knowledgeWhitelist.isNotEmpty()) {
            chat.addOverrideMixins(personExtensionData.knowledgeWhitelist)
        } else if (personExtensionData.knowledgeBlacklist.isNotEmpty()) {
            val filteredMixins = TosSettings.getContextMixins() - personExtensionData.knowledgeBlacklist.toSet()
            chat.addOverrideMixins(filteredMixins)
        }

        val chatUi = BarChatCustomUiPanel(
            dialog,
            Global.getSector().playerPerson,
            dialog.interactionTarget.activePerson
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