package maver.talkingonstations.ui.trichat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosSettings
import maver.talkingonstations.config.TosCredentialsVault
import maver.talkingonstations.config.TosProfileConfig
import maver.talkingonstations.config.TosProfileStore
import maver.talkingonstations.ui.ButtonId
import maver.talkingonstations.ui.DecorativeFrame
import maver.talkingonstations.ui.ProfileCard
import maver.talkingonstations.ui.TextArea
import maver.talkingonstations.ui.dto.ButtonData
import java.awt.Color

/**
 * Delegate for [TriChatProfileInteractionDialogPlugin] called by
 * [maver.talkingonstations.campaign.abilities.TriChatAbility]
 *
 * A multi-tab profile page that holds player profile information and
 * save-scoped settings.
 */
class TriChatProfileDialogDelegate(
    private val dialog: InteractionDialogAPI,
    private val width: Float,
    private val height: Float,
) : CustomVisualDialogDelegate {

    private object UIConstants {
        const val PAD = 10f

        const val HEADER_H = 24f
        const val CLOSE_W = 100f
        const val CLOSE_H = 20f

        const val TAB_W = 180f
        const val TAB_H = 28f
        const val TAB_GAP = 8f

        // Profile
        const val PAGE_PAD = 12f
        const val CARD_W = 240f
        const val CARD_H = 168f
        const val CARD_PAD = 12f
        const val PORTRAIT_SIZE = 84f
        const val CREST_SIZE = 28f
        const val PROFILE_GAP_X = 24f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private enum class Tab { PROFILE, API }

    private var callbacks: DialogCallbacks? = null
    private val panelPlugin = ProfilePanelPlugin()

    private val player: PersonAPI = Global.getSector().playerPerson
    private val playerCard = ProfileCard(
        player,
        UIConstants.CARD_W, UIConstants.CARD_H, UIConstants.CARD_PAD,
        UIConstants.PORTRAIT_SIZE, UIConstants.CREST_SIZE,
    )

    private val buttons: MutableMap<ButtonId, ButtonAPI> = mutableMapOf()

    private lateinit var rootPanel: CustomPanelAPI
    private lateinit var playerBackgroundTextArea: TextArea

    private var profilePage: CustomPanelAPI? = null
    private var apiPage: CustomPanelAPI? = null
    private var activePage: CustomPanelAPI? = null

    private lateinit var urlField: TextFieldAPI
    private lateinit var keyField: TextFieldAPI
    private var urlInitial: String = ""
    private var profileInitial: String = ""
    private val knobFields = mutableListOf<KnobField>()

    private val contentX = UIConstants.PAD
    private val tabBarY = UIConstants.PAD + UIConstants.HEADER_H + 10f
    private val contentY = tabBarY + UIConstants.TAB_H + 12f
    private val contentW = width - UIConstants.PAD * 2f
    private val contentH = height - contentY - UIConstants.PAD

    private val contentFrame = DecorativeFrame("graphics/ui/bgs/panel00")

    override fun init(panel: CustomPanelAPI, cb: DialogCallbacks) {
        callbacks = cb
        rootPanel = panel

        renderHeader(panel)
        renderTabBar(panel)

        profilePage = buildProfilePage()
        apiPage = buildApiPage()
        selectTab(Tab.PROFILE)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin
    override fun getNoiseAlpha(): Float = 0f
    override fun advance(amount: Float) {}
    override fun reportDismissed(option: Int) {
        dialog.dismiss()
    }

    private fun renderHeader(panel: CustomPanelAPI) {
        val section = panel.createUIElement(width, UIConstants.HEADER_H, false)
        section.addTitle("TriChat Profile")
        createButton(section, "Close", ButtonId.PROFILE_CLOSE, UIConstants.CLOSE_W, UIConstants.CLOSE_H)
            .position.inTL(width - UIConstants.CLOSE_W - UIConstants.PAD, 0f)
        section.position.inTL(UIConstants.PAD, UIConstants.PAD)
        panel.addUIElement(section)
    }

    private fun renderTabBar(panel: CustomPanelAPI) {
        val section = panel.createUIElement(width, UIConstants.TAB_H, false)
        val profileTab = createTab(section, "Player Profile", ButtonId.PROFILE_TAB)
        val apiTab = createTab(section, "API Settings", ButtonId.API_SETTINGS_TAB)
        profileTab.position.inTL(0f, 0f)
        apiTab.position.inTL(UIConstants.TAB_W + UIConstants.TAB_GAP, 0f)
        section.position.inTL(contentX, tabBarY)
        panel.addUIElement(section)
    }

    private fun buildProfilePage(): CustomPanelAPI {
        val page = rootPanel.createCustomPanel(contentW, contentH, ProfilePagePlugin())

        // Player card on the left
        playerCard.addContent(page, UIConstants.PAGE_PAD, UIConstants.PAGE_PAD)

        val bgX = UIConstants.PAGE_PAD + UIConstants.CARD_W + UIConstants.PROFILE_GAP_X
        val bgW = contentW - bgX - UIConstants.PAGE_PAD
        val bgH = contentH - UIConstants.PAGE_PAD * 2f
        val section = page.createUIElement(bgW, bgH, false)
        section.addSectionHeading("Background", Alignment.MID, 0f)

        playerBackgroundTextArea = TextArea(
            parent = section,
            rows = 16,
            maxRows = 60,
            width = bgW - 4f,
            height = bgH - 30f,
        )
        playerBackgroundTextArea.getPosition()?.inTL(0f, 26f)

        profileInitial = TosSettings.playerProfile
        if (profileInitial.isNotBlank()) playerBackgroundTextArea.setText(profileInitial)

        section.position.inTL(bgX, UIConstants.PAGE_PAD)
        page.addUIElement(section)
        return page
    }

    /**
     * The API page
     *
     * A player can configure a save-scoped url, api key and model settings
     */
    private fun buildApiPage(): CustomPanelAPI {
        val page = rootPanel.createCustomPanel(contentW, contentH, BaseCustomUIPanelPlugin())
        val hl = Misc.getHighlightColor()
        val colGap = 24f
        val colW = (contentW - UIConstants.PAGE_PAD * 2f - colGap) / 2f
        val colH = contentH - UIConstants.PAGE_PAD * 2f

        // Left column
        val left = page.createUIElement(colW, colH, false)
        left.addSectionHeading("Connection", Alignment.MID, 0f)
        left.addPara("Provider: %s", 8f, hl, TosSettings.api.ifBlank { "N/A" })

        left.addPara("Endpoint", 8f)
        urlField = textField(left, colW, TosSettings.apiUrl, maxChars = 4000)
        urlInitial = urlField.text.trim()

        val keyStatus = if (TosCredentialsVault.hasKey() || TosSettings.apiKey.isNotBlank()) "configured" else "not set"
        left.addPara("API Key ($keyStatus (leave to keep))", 8f)
        keyField = textField(left, colW, "", maxChars = 4000)

        left.addPara("Model", 8f)
        val modelField = textField(left, colW, TosSettings.apiModel, maxChars = 400)
        knobFields += KnobField(modelField) { c, t -> c.model = t.ifBlank { null } }

        left.addPara("Reasoning effort", 8f)
        val reasoningField = textField(left, colW, TosSettings.reasoningEffort, maxChars = 40)
        knobFields += KnobField(reasoningField) { c, t -> c.reasoningEffort = t.ifBlank { null } }

        left.position.inTL(UIConstants.PAGE_PAD, UIConstants.PAGE_PAD)
        page.addUIElement(left)

        // Right column
        val right = page.createUIElement(colW, colH, false)
        right.addSectionHeading("Sampler", Alignment.MID, 0f)
        numberField(right, colW, "Temperature", TosSettings.temperature.toString()) { c, v -> c.temperature = v }
        numberField(right, colW, "Top P", TosSettings.topP.toString()) { c, v -> c.topP = v }
        numberField(right, colW, "Top K", TosSettings.topK.toString()) { c, v -> c.topK = v }
        numberField(right, colW, "Max tokens", TosSettings.maxTokens.toString()) { c, v -> c.maxTokens = v.toInt() }
        numberField(right, colW, "Frequency penalty", TosSettings.frequencyPenalty.toString()) { c, v -> c.frequencyPenalty = v }
        numberField(right, colW, "Presence penalty", TosSettings.presencePenalty.toString()) { c, v -> c.presencePenalty = v }
        numberField(right, colW, "Repetition penalty", TosSettings.repetitionPenalty.toString()) { c, v -> c.repetitionPenalty = v }
        right.position.inTL(UIConstants.PAGE_PAD + colW + colGap, UIConstants.PAGE_PAD)
        page.addUIElement(right)

        return page
    }

    private fun textField(el: TooltipMakerAPI, w: Float, value: String, maxChars: Int): TextFieldAPI =
        el.addTextField(w, 2f).apply {
            setMaxChars(maxChars)
            setLimitByStringWidth(false)
            text = value
        }

    private fun numberField(
        el: TooltipMakerAPI,
        w: Float,
        label: String,
        value: String,
        apply: (TosProfileConfig, Double) -> Unit,
    ) {
        el.addPara(label, 8f)
        val field = textField(el, w, value, maxChars = 20)
        knobFields += KnobField(field) { c, t -> t.toDoubleOrNull()?.let { apply(c, it) } }
    }

    /**
     * Persists changes to [TosProfileStore]
     */
    private fun persistSettings() {
        val profileText = playerBackgroundTextArea.getText()
        val profileChanged = profileText != profileInitial
        val knobsChanged = knobFields.any { it.isChanged() }

        if (profileChanged || knobsChanged) {
            TosProfileStore.current()?.let { cfg ->
                cfg.profile = profileText.ifBlank { null }
                knobFields.forEach { it.run(cfg) }
                TosProfileStore.save()
            }
        }

        val url = urlField.text.trim()
        val newKey = keyField.text.trim()
        if (url != urlInitial || newKey.isNotEmpty()) {
            val key = newKey.ifEmpty { TosCredentialsVault.key().orEmpty() }
            TosCredentialsVault.update(url, key)
        }
    }

    /**
     * Switches tabs by swapping page panels
     */
    private fun selectTab(tab: Tab) {
        buttons[ButtonId.PROFILE_TAB]?.isChecked = (tab == Tab.PROFILE)
        buttons[ButtonId.API_SETTINGS_TAB]?.isChecked = (tab == Tab.API)

        val next = if (tab == Tab.PROFILE) profilePage else apiPage
        if (next === activePage) return

        activePage?.let { rootPanel.removeComponent(it) }
        next?.let { rootPanel.addComponent(it).inTL(contentX, contentY) }
        activePage = next
    }

    private fun createButton(
        tooltip: TooltipMakerAPI,
        text: String,
        id: ButtonId,
        w: Float,
        h: Float,
    ): ButtonAPI {
        val payload = ButtonData(id, null)
        return tooltip.addButton(
            text, payload,
            UIConstants.BUTTON_COLOR, UIConstants.BUTTON_TEXT_COLOR,
            Alignment.MID, CutStyle.ALL, w, h, 0f,
        ).also {
            it.customData = payload
            buttons[id] = it
        }
    }

    private fun createTab(tooltip: TooltipMakerAPI, text: String, id: ButtonId): ButtonAPI {
        val base = Misc.getBasePlayerColor()
        val bg = Misc.getDarkPlayerColor()
        val payload = ButtonData(id, null)
        return tooltip.addAreaCheckbox(
            text, payload, base, bg, base,
            UIConstants.TAB_W, UIConstants.TAB_H, 0f,
        ).also {
            it.customData = payload
            buttons[id] = it
        }
    }

    private inner class ProfilePanelPlugin : BaseCustomUIPanelPlugin() {
        override fun renderBelow(alphaMult: Float) {
            val p = rootPanel.position ?: return
            val frameY = (p.y + p.height) - contentY - contentH
            contentFrame.render(p.x + contentX, frameY, contentW, contentH, alphaMult)
        }

        override fun buttonPressed(buttonId: Any?) {
            val data = buttonId as? ButtonData ?: return
            when (data.buttonId) {
                ButtonId.PROFILE_CLOSE -> {
                    persistSettings()
                    callbacks?.dismissDialog()
                }

                ButtonId.PROFILE_TAB -> selectTab(Tab.PROFILE)
                ButtonId.API_SETTINGS_TAB -> selectTab(Tab.API)
                else -> {}
            }
        }
    }

    private inner class KnobField(
        private val field: TextFieldAPI,
        private val initial: String = field.text.trim(),
        private val apply: (TosProfileConfig, String) -> Unit,
    ) {
        fun isChanged(): Boolean = field.text.trim() != initial
        fun run(cfg: TosProfileConfig) {
            val text = field.text.trim()
            if (text != initial) apply(cfg, text)
        }
    }

    private inner class ProfilePagePlugin : BaseCustomUIPanelPlugin() {
        override fun renderBelow(alphaMult: Float) {
            val p = profilePage?.position ?: return
            playerCard.renderFrame(p, UIConstants.PAGE_PAD, UIConstants.PAGE_PAD, alphaMult)
        }
    }
}
