package maver.talkingonstations.ui.trichat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.TosSettings
import maver.talkingonstations.config.TosCredentialsVault
import maver.talkingonstations.config.TosProfileConfig
import maver.talkingonstations.config.TosProfileStore
import maver.talkingonstations.mods.nexerelin.NexAllianceInfo
import maver.talkingonstations.ui.ButtonId
import maver.talkingonstations.ui.DecorativeFrame
import maver.talkingonstations.ui.ProfileCard
import maver.talkingonstations.ui.TextArea
import maver.talkingonstations.ui.dto.ButtonData
import java.awt.Color

/**
 * Delegate for [TriChatProfileInteractionDialogPlugin]
 *
 * A multi-tab profile page.
 *
 * All tabs are drawn, a row of buttons on the top pulls the active panel
 * into the foreground.
 *
 * myFrame.render(drawX, bandY, UIConstants.BAND_WIDTH, UIConstants.BAND_HEIGHT, 0f - 1f)
 *
 */
class TriChatProfileDialogDelegate(
    private val dialog: InteractionDialogAPI,
    private val width: Float,
    private val height: Float,
) : CustomVisualDialogDelegate {

    /**
     * Interface height is defined by the actual content height and
     * width is defined by summing up all width constants
     */
    private object UIConstants {
        const val PAD = 10f

        const val HEADER_H = 24f
        const val CLOSE_W = 100f
        const val CLOSE_H = 20f

        const val TAB_W = 150f
        const val TAB_H = 28f
        const val TAB_GAP = 8f

        const val SCROLLBAR_W = 20f

        // Profile
        const val PAGE_PAD = 35f
        const val CARD_W = 240f
        const val CARD_H = 168f
        const val CARD_PAD = 12f
        const val PORTRAIT_SIZE = 84f
        const val CREST_SIZE = 28f
        const val PROFILE_GAP_X = 24f

        // Faction tab: crest sits in a column max 1/3 of the section width
        const val FACTION_CREST_MAX = 128f
        const val FACTION_INFO_MIN_H = 96f

        // Faction tab: per-section background lore area
        const val FACTION_BG_AREA_H = 100f
        const val FACTION_BG_HEAD_Y = 26f

        // Faction tab: "independent captain" note shown when there is no affiliation
        const val FACTION_NOTE_H = HEADER_H + 30f

        // Sampler grid: narrow input on the right, plain-language help on the left
        const val SAMPLER_ROW_H = 54f
        const val SAMPLER_ROW_GAP = 12f
        const val SAMPLER_FIELD_W = 150f
        const val SAMPLER_COL_GAP = 24f

        val BUTTON_COLOR = Color(220, 20, 60)
        val BUTTON_TEXT_COLOR = Color(255, 255, 255)
    }

    private enum class Tab { PROFILE, API, FACTION }

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
    private var commissionBackgroundTextArea: TextArea? = null
    private var factionBackgroundTextArea: TextArea? = null

    private var profilePage: CustomPanelAPI? = null
    private var factionPage:CustomPanelAPI? = null
    private var apiPage: CustomPanelAPI? = null
    private var activePage: CustomPanelAPI? = null

    private lateinit var urlField: TextFieldAPI
    private lateinit var keyField: TextFieldAPI
    private var urlInitial: String = ""
    private var profileInitial: String = ""
    private var commissionInitial: String = ""
    private var factionInitial: String = ""
    private val knobFields = mutableListOf<KnobField>()

    private val contentX = UIConstants.PAD
    private val tabBarY = UIConstants.PAD + UIConstants.HEADER_H + 10f
    private val contentY = tabBarY + UIConstants.TAB_H + 12f
    private val contentW = width - UIConstants.PAD * 2f
    private val contentH = height - contentY - UIConstants.PAD

    // Inner viewport shared by every tab page
    private val pagePad = UIConstants.PAGE_PAD
    private val viewW = contentW - pagePad * 2f
    private val viewH = contentH - pagePad * 2f
    private val contentFrame = DecorativeFrame("graphics/ui/bgs/panel00")

    override fun init(panel: CustomPanelAPI, cb: DialogCallbacks) {
        callbacks = cb
        rootPanel = panel

        renderHeader(panel)
        renderTabBar(panel)

        profilePage = buildProfilePage()
        factionPage = buildFactionPage()
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
            .position.inTL(width - UIConstants.CLOSE_W - (UIConstants.PAD * 2f), 0f)
        section.position.inTL(UIConstants.PAD, UIConstants.PAD)
        panel.addUIElement(section)
    }

    private fun renderTabBar(panel: CustomPanelAPI) {
        val section = panel.createUIElement(width, UIConstants.TAB_H, false)
        val profileTab = createTab(section, "Personal", ButtonId.PROFILE_TAB)
        val factionTab = createTab(section, "Faction", ButtonId.FACTION_TAB)
        val apiTab = createTab(section, "Settings", ButtonId.API_SETTINGS_TAB)
        profileTab.position.inTL(0f, 0f)
        factionTab.position.inTL(UIConstants.TAB_W + UIConstants.TAB_GAP, 0f)
        apiTab.position.inTL(2f * UIConstants.TAB_W + 2 * UIConstants.TAB_GAP, 0f)
        section.position.inTL(contentX, tabBarY)
        panel.addUIElement(section)
    }

    private fun newPage(): CustomPanelAPI =
        rootPanel.createCustomPanel(contentW, contentH, BaseCustomUIPanelPlugin())

    private fun customPanel(w: Float, h: Float): CustomPanelAPI =
        Global.getSettings().createCustom(w, h, BaseCustomUIPanelPlugin())

    private fun addScroller(page: CustomPanelAPI, contentHeight: Float, fill: (CustomPanelAPI) -> Unit) {
        val scrollWrap = page.createUIElement(viewW, viewH, true)
        val content = customPanel(viewW, contentHeight)
        fill(content)
        scrollWrap.addCustom(content, 0f)
        page.addUIElement(scrollWrap).inTL(pagePad, pagePad)
    }

    /**
     * Player profile
     * 1 column 2 row layout
     */
    private fun buildProfilePage(): CustomPanelAPI {
        val page = newPage()

        val textAreaX = pagePad + UIConstants.CARD_W + UIConstants.PROFILE_GAP_X
        val textAreaWidth = viewW - textAreaX - UIConstants.SCROLLBAR_W

        addScroller(page, viewH) { content ->
            // Left column
            val leftColumn = customPanel(UIConstants.CARD_W, viewH)
            playerCard.addContent(leftColumn, 0f, 0f)
            leftColumn.position.inTL(0f, 0f)
            content.addComponent(leftColumn)

            // Right column
            val rightColumn = customPanel(textAreaWidth, viewH)
            val section = rightColumn.createUIElement(textAreaWidth, viewH, false)
            section.addSectionHeading("Your Background", Alignment.MID, 0f)

            playerBackgroundTextArea = TextArea(
                parent = section,
                rows = 16,
                maxRows = 60,
                width = textAreaWidth - 4f,
                height = viewH - 30f,
            )
            playerBackgroundTextArea.getPosition()?.inTL(0f, 26f)

            profileInitial = TosSettings.playerProfile
            if (profileInitial.isNotBlank()) playerBackgroundTextArea.setText(profileInitial)

            rightColumn.addUIElement(section)
            rightColumn.position.inTL(textAreaX, 0f)
            content.addComponent(rightColumn)
        }

        return page
    }
    /**
     * Faction tab.
     * 1 column
     */
    private fun buildFactionPage(): CustomPanelAPI {
        val page = newPage()

        val commissionFaction = Misc.getCommissionFaction()
        val ownFaction = if (Misc.isPlayerFactionSetUp()) Global.getSector().playerFaction else null

        val sectionH = factionBlockHeight(viewW) + backgroundSectionHeight()
        var totalH = 0f
        if (commissionFaction != null) totalH += sectionH
        if (ownFaction != null) totalH += sectionH
        if (commissionFaction == null && ownFaction == null) {
            totalH += UIConstants.FACTION_NOTE_H + UIConstants.PROFILE_GAP_X
        }

        addScroller(page, maxOf(viewH, totalH)) { content ->
            var y = 0f
            // Commission
            if (commissionFaction != null) {
                y += addFactionBlock(content, viewW, y, "Commission", commissionFaction, playerRankLabel(player))
                commissionInitial = TosSettings.commissionBackground
                commissionBackgroundTextArea = addTextArea(content, viewW, y, "Commission Background", commissionInitial)
                y += backgroundSectionHeight()
            }
            // Faction
            if (ownFaction != null) {
                y += addFactionBlock(content, viewW, y, "Your Faction", ownFaction, null)
                factionInitial = TosSettings.factionBackground
                factionBackgroundTextArea = addTextArea(content, viewW, y, "Faction Background", factionInitial)
                y += backgroundSectionHeight()
            }
            // No faction
            if (commissionFaction == null && ownFaction == null) {
                val none = content.createUIElement(viewW, UIConstants.FACTION_NOTE_H, false)
                none.addSectionHeading("Your Faction", Alignment.MID, 0f)
                none.addPara("You are an independent captain, with no commission or faction of your own.", 6f)
                none.position.inTL(0f, y)
                content.addUIElement(none)
            }
        }

        return page
    }

    /**
     * Faction row
     * 2 columns
     */
    private fun addFactionBlock(
        column: CustomPanelAPI,
        viewW: Float,
        topY: Float,
        heading: String,
        faction: FactionAPI,
        rankLabel: String?,
    ): Float {
        val gap = UIConstants.PROFILE_GAP_X
        val crestColW = viewW / 3f
        val infoColW = viewW - crestColW - gap
        val crestSize = minOf(crestColW, UIConstants.FACTION_CREST_MAX)
        val rowH = maxOf(crestSize, UIConstants.FACTION_INFO_MIN_H)
        val rowY = topY + UIConstants.HEADER_H + 6f

        val head = column.createUIElement(viewW, UIConstants.HEADER_H, false)
        head.addSectionHeading(heading, Alignment.MID, 0f)
        head.position.inTL(0f, topY)
        column.addUIElement(head)

        // Left column
        val infoCol = column.createUIElement(infoColW, rowH, false)
        infoCol.addTitle(faction.displayNameLong, faction.color)

        // Alliance info, nexerelin only
        val allianceInfo =
            if (Global.getSettings().modManager.isModEnabled("nexerelin"))
                NexAllianceInfo.allianceFor(faction.id)
            else null
        if (allianceInfo != null) {
            infoCol.addPara("Alliance: %s", 6f, faction.baseUIColor, allianceInfo.name)
            if (allianceInfo.memberNames.isNotEmpty()) {
                infoCol.addPara(allianceInfo.memberNames.joinToString(", "), Misc.getGrayColor(), 2f)
            }
            if (allianceInfo.alignmentName.isNotBlank()) {
                infoCol.addPara("Alignment: %s", 4f, allianceInfo.alignmentColor, allianceInfo.alignmentName)
            }
        }

        if (!rankLabel.isNullOrBlank()) infoCol.addPara(rankLabel, 6f)

        infoCol.position.inTL(0f, rowY)
        column.addUIElement(infoCol)

        // Right column
        val crest = faction.crest
        if (!crest.isNullOrBlank()) {
            val crestCol = column.createUIElement(crestColW, rowH, false)
            crestCol.addImage(crest, crestSize, 0f)
            crestCol.position.inTL(infoColW + gap, rowY)
            column.addUIElement(crestCol)
        }

        return factionBlockHeight(viewW)
    }

    /**
     * Full-width lore text area
     */
    private fun addTextArea(column: CustomPanelAPI, viewW: Float, topY: Float, heading: String, initial: String): TextArea {
        val section = column.createUIElement(viewW, backgroundSectionHeight(), false)
        section.addSectionHeading(heading, Alignment.MID, 0f)

        val area = TextArea(
            parent = section,
            rows = 16,
            maxRows = 30,
            width = viewW - 4f,
            height = UIConstants.FACTION_BG_AREA_H,
        )
        area.getPosition()?.inTL(0f, UIConstants.FACTION_BG_HEAD_Y)
        if (initial.isNotBlank()) area.setText(initial)

        section.position.inTL(0f, topY)
        column.addUIElement(section)
        return area
    }

    private fun factionBlockHeight(viewW: Float): Float {
        val crestSize = minOf(viewW / 3f, UIConstants.FACTION_CREST_MAX)
        val rowH = maxOf(crestSize, UIConstants.FACTION_INFO_MIN_H)
        return UIConstants.HEADER_H + 6f + rowH + UIConstants.PROFILE_GAP_X
    }

    private fun backgroundSectionHeight(): Float =
        UIConstants.FACTION_BG_HEAD_Y + UIConstants.FACTION_BG_AREA_H + UIConstants.PROFILE_GAP_X

    private fun playerRankLabel(person: PersonAPI): String =
        if (!person.rank.isNullOrEmpty() && !person.rank.contains("Unknown")) "${person.post} (${person.rank})"
        else (person.post ?: "")

    /**
     * API tab
     * 1 column 2 row layout
     */
    private fun buildApiPage(): CustomPanelAPI {
        val page = newPage()
        val hl = Misc.getHighlightColor()
        val colW = viewW - UIConstants.SCROLLBAR_W - UIConstants.PAGE_PAD

        val section = page.createUIElement(viewW, viewH, false)
        section.position.inTL(pagePad, pagePad)
        page.addUIElement(section)

        val host = customPanel(viewW, viewH)
        section.addCustom(host, 0f)
        val content = host.createUIElement(viewW, viewH, true)

        // Connection
        content.addSectionHeading("Connection", Alignment.MID, 0f)
        content.addPara("Provider: %s", 8f, hl, TosSettings.api.ifBlank { "N/A" })

        content.addPara("Endpoint", 8f)
        urlField = textField(content, colW, TosSettings.apiUrl, maxChars = 4000)
        urlInitial = urlField.text.trim()

        val keyStatus = if (TosCredentialsVault.hasKey() || TosSettings.apiKey.isNotBlank()) "configured" else "not set"
        content.addPara("API Key ($keyStatus (leave to keep))", 8f)
        keyField = textField(content, colW, "", maxChars = 4000)

        content.addPara("Model", 8f)
        val modelField = textField(content, colW, TosSettings.apiModel, maxChars = 400)
        knobFields += KnobField(modelField) { c, t -> c.model = t.ifBlank { null } }

        content.addPara("Reasoning effort", 8f)
        val reasoningField = textField(content, colW, TosSettings.reasoningEffort, maxChars = 40)
        knobFields += KnobField(reasoningField) { c, t -> c.reasoningEffort = t.ifBlank { null } }

        content.addSpacer(16f)

        content.addSectionHeading("Sampler", Alignment.MID, 0f)
        content.addSpacer(8f)
        content.addCustom(buildSamplerGrid(colW), 0f)

        host.addUIElement(content).inTL(0f, 0f)
        return page
    }

    private class SamplerRow(
        val name: String,
        val value: String,
        val help: String,
        val range: String,
        val apply: (TosProfileConfig, Double) -> Unit,
    )

    /**
     * Sampler settings
     * 2 columns
     */
    private fun buildSamplerGrid(width: Float): CustomPanelAPI {
        val rows = listOf(
            SamplerRow(
                "Temperature", TosSettings.temperature.toString(),
                "How random replies are. Higher is more varied, lower more focused.",
                "0.7 - 1.2",
            ) { c, v -> c.temperature = v },
            SamplerRow(
                "Top P", TosSettings.topP.toString(),
                "Trims away unlikely word choices. Lower keeps the wording predictable.",
                "0.9 - 1.0 (off)",
            ) { c, v -> c.topP = v },
            SamplerRow(
                "Top K", TosSettings.topK.toString(),
                "Limits how many word options are weighed at each step.",
                "0 (off) or 20 - 100",
            ) { c, v -> c.topK = v },
            SamplerRow(
                "Max tokens", TosSettings.maxTokens.toString(),
                "Maximum length of a single reply. Includes thinking and tool calls.",
                "500 - 5000",
            ) { c, v -> c.maxTokens = v.toInt() },
            SamplerRow(
                "Frequency penalty", TosSettings.frequencyPenalty.toString(),
                "Discourages reusing the same words too often. Higher means less repetition.",
                "0.0 (off) - 1.0",
            ) { c, v -> c.frequencyPenalty = v },
            SamplerRow(
                "Presence penalty", TosSettings.presencePenalty.toString(),
                "Nudges the model toward new topics and words. Higher adds more variety.",
                "0.0 (off) - 1.0",
            ) { c, v -> c.presencePenalty = v },
            SamplerRow(
                "Repetition penalty", TosSettings.repetitionPenalty.toString(),
                "Reduces repeated phrases and loops.",
                "1.0 (off) - 1.2",
            ) { c, v -> c.repetitionPenalty = v },
        )

        val rowH = UIConstants.SAMPLER_ROW_H
        val rightColW = UIConstants.SAMPLER_FIELD_W
        val gap = UIConstants.SAMPLER_COL_GAP
        val leftColW = width - rightColW - gap
        val stride = rowH + UIConstants.SAMPLER_ROW_GAP

        val panel = customPanel(width, stride * rows.size - UIConstants.SAMPLER_ROW_GAP)
        rows.forEachIndexed { i, row ->
            val y = i * stride

            val help = panel.createUIElement(leftColW, rowH, false)
            help.addPara(row.help, 0f)
            help.addPara(row.range, Misc.getGrayColor(), 2f)
            help.position.inTL(0f, y)
            panel.addUIElement(help)

            val input = panel.createUIElement(rightColW, rowH, false)
            input.addPara(row.name, 0f)
            val field = textField(input, rightColW, row.value, maxChars = 20)
            knobFields += KnobField(field) { c, t -> t.toDoubleOrNull()?.let { row.apply(c, it) } }
            input.position.inTL(leftColW + gap, y)
            panel.addUIElement(input)
        }

        return panel
    }

    private fun textField(el: TooltipMakerAPI, w: Float, value: String, maxChars: Int): TextFieldAPI =
        el.addTextField(w, 2f).apply {
            setMaxChars(maxChars)
            isLimitByStringWidth = false
            text = value
        }

    /**
     * Persists changes to [TosProfileStore]
     */
    private fun persistSettings() {
        val profileText = playerBackgroundTextArea.getText()
        val commissionText = commissionBackgroundTextArea?.getText()
        val factionText = factionBackgroundTextArea?.getText()

        val profileChanged = profileText != profileInitial
        val commissionChanged = commissionText != null && commissionText != commissionInitial
        val factionChanged = factionText != null && factionText != factionInitial
        val knobsChanged = knobFields.any { it.isChanged() }

        if (profileChanged || commissionChanged || factionChanged || knobsChanged) {
            TosProfileStore.current()?.let { cfg ->
                cfg.profile = profileText.ifBlank { null }
                // Only write sections that are actually shown, so a hidden one can't erase saved lore.
                commissionText?.let { cfg.commissionBackground = it.ifBlank { null } }
                factionText?.let { cfg.factionBackground = it.ifBlank { null } }
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
        buttons[ButtonId.FACTION_TAB]?.isChecked = (tab == Tab.FACTION)

        val next =
            when (tab) {
                Tab.PROFILE -> profilePage
                Tab.FACTION -> factionPage
                else -> apiPage
            }

        if (next === activePage) return

        activePage?.let { rootPanel.removeComponent(it) }
        next?.let { rootPanel.addComponent(it).inTL(contentX, contentY) }
        activePage = next
    }

    /**
     * Helper for button creation
     */
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

    /**
     * Helper for tab creation
     */
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
                ButtonId.FACTION_TAB -> selectTab(Tab.FACTION)
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
}
