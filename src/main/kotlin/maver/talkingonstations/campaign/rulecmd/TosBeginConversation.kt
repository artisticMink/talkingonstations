package maver.talkingonstations.campaign.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.RuleBasedDialog
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import maver.talkingonstations.campaign.BarChatCustomUiPanel

/**
 * Person must have been added to SectorAPI.getImportantPeople().
 *
 * BeginConversation <person id> <minimal mode (no faction shown), optional> <show relationship bar>
 *
 * <person id> can also be POST:<post id> which will find the first person in the comm directory with that post.
</post></person></show></minimal></person> */
class TosBeginConversation : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token?>,
        memoryMap: MutableMap<String?, MemoryAPI?>?
    ): Boolean {
        if (dialog == null) return false

        val barChatUi = BarChatCustomUiPanel(
            dialog,
            Global.getSector().playerPerson,
            dialog.interactionTarget.activePerson
        )

        var id: String? = null
        var person: PersonAPI? = null

        val o = params.get(0)!!.getObject(memoryMap)
        if (o is PersonAPI) {
            person = o
        } else {
            id = params.get(0)!!.getStringWithTokenReplacement(ruleId, dialog, memoryMap)
        }

        var minimal = false
        var showRel = true
        if (params.size > 1) {
            minimal = params.get(1)!!.getBoolean(memoryMap)
        }
        if (params.size > 2) {
            showRel = params.get(2)!!.getBoolean(memoryMap)
        }

        if (person == null) {
            val data = Global.getSector().getImportantPeople().getData(id)

            if (data == null) {
                if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
                    if (id!!.startsWith("POST:")) {
                        val postId = id.substring(id.indexOf(":") + 1)
                        for (entry in dialog.getInteractionTarget().getMarket().getCommDirectory().getEntriesCopy()) {
                            if (entry.getType() == CommDirectoryEntryAPI.EntryType.PERSON && entry.getEntryData() is PersonAPI) {
                                val curr = entry.getEntryData() as PersonAPI
                                if (postId == curr.getPostId()) {
                                    person = curr
                                    break
                                }
                            }
                        }
                    } else {
                        for (curr in dialog.getInteractionTarget().getMarket().getPeopleCopy()) {
                            if (curr.getId() == id) {
                                person = curr
                                break
                            }
                        }
                        if (person == null) {
                            val entry =
                                dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson(id)
                            if (entry != null) {
                                person = entry.getEntryData() as PersonAPI?
                            }
                        }
                    }
                }
            } else {
                person = data.getPerson()
            }
        }

        if (person == null) return false

        dialog.getInteractionTarget().setActivePerson(person)
        (dialog.getPlugin() as RuleBasedDialog).notifyActivePersonChanged()
        dialog.getVisualPanel().showPersonInfo(person, minimal, showRel)

        return true
    }
}