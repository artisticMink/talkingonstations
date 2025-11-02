package maver.talkingonstations.campaign.intel.bar.events

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosSettings
import maver.talkingonstations.campaign.BarChatCustomUiPanel
import maver.talkingonstations.chat.Chat

class ChatBarEventWithPerson : BaseBarEventWithPerson() {

    @Transient
    private lateinit var barChatUi: BarChatCustomUiPanel

    override fun isAlwaysShow(): Boolean {
        return Global.getSettings().isDevMode
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

    override fun regen(market: MarketAPI) {
        person = TosSettings.getPersonTypes().random().getNewPerson(market.faction.id)
        super.regen(market)
    }

    override fun addPromptAndOption(dialog: InteractionDialogAPI, memoryMap: MutableMap<String?, MemoryAPI?>?) {
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
        done = true
        noContinue = true
        barChatUi.cancelScope()
        this.optionSelected("Quit", {} )
    }

    override fun getPersonFaction(): String {
        return person.faction.id
    }

    override fun getPersonRank(): String? {
        return person.rankId
    }

    override fun getPersonPost(): String? {
        return person.postId
    }

    override fun getPersonPortrait(): String? {
        return person.portraitSprite
    }

    override fun getPersonGender(): Gender {
        return person.gender
    }

}