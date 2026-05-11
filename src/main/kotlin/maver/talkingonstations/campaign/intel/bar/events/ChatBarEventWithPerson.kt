package maver.talkingonstations.campaign.intel.bar.events

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.ui.TriChat.TriChatCustomVisualPanel
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.httpapi.HttpApiRegistry

/**
 * A 1:1 conversation that appears when visitng a market and bringing up the bar interface
 */
class ChatBarEventWithPerson : BaseBarEventWithPerson() {

    @Transient
    private lateinit var barChatUi: TriChatCustomVisualPanel

    override fun isAlwaysShow(): Boolean {
        return when(Global.getSettings().isDevMode) {
            true -> TosRegistry.getArchetypes().isNotEmpty()
            false -> false
        }
    }

    override fun init(dialog: InteractionDialogAPI, memoryMap: Map<String, MemoryAPI>) {
        super.init(dialog, memoryMap)

        options.clearOptions()

        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            market
        )

        chat.beforeContinueAsPlayer = ::addPlayerDialogOption
        chat.afterChatResponse = ::addNpcDialogOption

        dialog.showVisualPanel()
        dialog.visualPanel.showPersonInfo(person, true)

        barChatUi = TriChatCustomVisualPanel(
            dialog.visualPanel,
            Global.getSector().playerPerson
        )

        barChatUi.onModelSelectClick = { modelSettings -> chat.modelSettings = modelSettings }
        barChatUi.onRetryButtonClick = { dialog.textPanel.replaceLastParagraph(""); chat.retryLastMessage() }
        barChatUi.onSendButtonClick = { message -> chat.continueChatAsPlayer(message) }
        barChatUi.onPlayerQuit = ::onPlayerQuit
    }

    override fun regen(market: MarketAPI) {
        person = TosRegistry.getArchetypes().random().getPerson(market)
        super.regen(market)
    }

    override fun addPromptAndOption(dialog: InteractionDialogAPI, memoryMap: MutableMap<String?, MemoryAPI?>?) {
        // We need at least one HTTP API to initialize the UI
        if (HttpApiRegistry.getDefaultApi() == null) return

        regen(dialog.interactionTarget.market)
        dialog.optionPanel.addOption("Dive into the crowd and see who you can find.", this)
        super.addPromptAndOption(dialog, memoryMap)
    }

    fun addPlayerDialogOption(message: String) {
        dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor())
    }

    fun addNpcDialogOption(message: String) {
        dialog.textPanel.addPara(message)
    }

    fun onPlayerQuit() {
        barChatUi.textArea.clearText()
        dialog.dismissAsCancel()
    }

    override fun getPersonRank(): String? {
        return Ranks.EXECUTIVE
    }

    override fun getPersonPost(): String? {
        return Ranks.EXECUTIVE
    }

    override fun getPersonPortrait(): String? {
        return null
    }

    override fun getPersonGender(): Gender {
        return Gender.FEMALE
    }

}