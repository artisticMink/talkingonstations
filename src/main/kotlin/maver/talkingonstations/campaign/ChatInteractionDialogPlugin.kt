package maver.talkingonstations.campaign

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.impl.campaign.DevMenuOptions
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.rulecmd.DismissDialog
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.util.Misc

class ChatInteractionDialogPlugin: InteractionDialogPlugin {
    private val memoryMap: MutableMap<String, MemoryAPI> = mutableMapOf()

    private var dialog: InteractionDialogAPI? = null
    private var memory: MemoryAPI? = null
    private var textPanel: TextPanelAPI? = null
    private var options: OptionPanelAPI? = null
    private var visual: VisualPanelAPI? = null

    override fun init(dialog: InteractionDialogAPI) {
        this.dialog = dialog

        textPanel = dialog.textPanel
        options = dialog.optionPanel
        visual = dialog.visualPanel

        visual?.setVisualFade(0.25f, 0.25f)

        dialog.optionPanel.addOption("Foo", "Bar")

        options?.hasOptions()?.let {
            if (!it) {
                options!!.clearOptions()
                options!!.addOption("Leave", RuleBasedInteractionDialogPluginImpl.FAILSAFE_LEAVE)
                if (Global.getSettings().isDevMode) {
                    DevMenuOptions.addOptions(dialog)
                }
            }
        }
    }

    fun fireBest(trigger: String): Boolean {
        return FireBest.fire(null, dialog, memoryMap, trigger)
    }

    override fun optionSelected(optionData: String, p1: Any) {
        dialog!!.addOptionSelectedText(optionData)

        if (optionData === RuleBasedInteractionDialogPluginImpl.FAILSAFE_LEAVE) {
            DismissDialog().execute(null, dialog, null, memoryMap)
            return
        }

        if (optionData === DumpMemory.OPTION_ID) {
            DumpMemory().execute(null, dialog, null, memoryMap)
            return
        } else if (DevMenuOptions.isDevOption(optionData)) {
            DevMenuOptions.execute(dialog, optionData as String)
            return
        }

        memory?.set("\$option", optionData)
        memory?.expire("\$option", 0f)

        val foundRule: Boolean = fireBest("DialogOptionSelected")
        if (!foundRule && !dialog!!.isCurrentOptionHadAConfirm()) {
            textPanel!!.addPara(
                "ERROR: no rule found for option " + optionData +
                        ", adding a failsafe option to exit dialog.", Misc.getNegativeHighlightColor()
            )
            textPanel!!.addPara(
                "Note: this may break any mission interaction in the current dialog, "
                        + "it's recommended that you reload an earlier save if you use this option."
            )
            textPanel!!.highlightInLastPara(
                Misc.getNegativeHighlightColor(),
                "recommended that you reload an earlier save"
            )
            options!!.addOption("Exit dialog", RuleBasedInteractionDialogPluginImpl.FAILSAFE_LEAVE)
        }
    }

    override fun optionMousedOver(p0: String?, p1: Any?) {

    }

    override fun advance(p0: Float) {

    }

    override fun backFromEngagement(p0: EngagementResultAPI?) {

    }

    override fun getContext(): Any? {
        return null
    }

    override fun getMemoryMap(): Map<String, MemoryAPI> {
        return memoryMap
    }
}