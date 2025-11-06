package maver.talkingonstations.campaign.intel.bar.events

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.chat.Chat
import maver.talkingonstations.campaign.BarChatCustomUiPanel
import maver.talkingonstations.characters.random.Trader

class DebugChatBarEventWithPerson : BaseBarEventWithPerson() {

    // Prevent serializer from complaining when saving the game
    @Transient
    private lateinit var barChatUi: BarChatCustomUiPanel

    override fun isAlwaysShow(): Boolean {
        return Global.getSettings().isDevMode
    }

    override fun init(dialog: InteractionDialogAPI, memoryMap: Map<String, MemoryAPI>) {
        person = Trader().getPerson(market.faction.id)

        super.init(dialog, memoryMap)

        options.clearOptions()

        val chat = Chat(
            Global.getSector().playerPerson,
            person,
            market
        )

        chat.beforeContinueAsPlayer = ::addPlayerDialogOption
        chat.afterChatResponse = ::addNpcDialogOption

        // Setup custom visual panel
        barChatUi = BarChatCustomUiPanel(
            dialog,
            Global.getSector().playerPerson,
            person
        )

        barChatUi.onModelSelectClick = { modelSettings -> chat.setModelSettings(modelSettings) }
        barChatUi.onRetryButtonClick = { dialog.textPanel.replaceLastParagraph(""); chat.retryLastMessage() }
        barChatUi.onSendButtonClick = { message -> chat.continueChatAsPlayer(message) }
        barChatUi.onPlayerQuit = ::onPlayerQuit
    }

    override fun addPromptAndOption(dialog: InteractionDialogAPI, memoryMap: MutableMap<String?, MemoryAPI?>?) {
        super.addPromptAndOption(dialog, memoryMap)

        regen(dialog.interactionTarget.market)
        dialog.optionPanel.addOption("(Talking On Stations) Start Debug Chat", this)
    }

    fun addPlayerDialogOption(message: String) {
        dialog.textPanel.addParagraph(message, Misc.getBasePlayerColor())
    }

    fun addNpcDialogOption(message: String) {
        dialog.textPanel.addPara(message)
    }

    fun onPlayerQuit() {
        done = true
        noContinue = true
        barChatUi.cancelScope()
        this.optionSelected("Quit", {} )
    }

    override fun getPersonFaction(): String {
        return Factions.TRITACHYON
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