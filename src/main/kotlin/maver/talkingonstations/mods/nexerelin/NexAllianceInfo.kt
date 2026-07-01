package maver.talkingonstations.mods.nexerelin

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.AllianceManager
import java.awt.Color

data class AllianceInfo(
    val name: String,
    val memberNames: List<String>,
    val alignmentName: String,
    val alignmentColor: Color,
)

object NexAllianceInfo {
    fun allianceFor(factionId: String): AllianceInfo? {
        val alliance = AllianceManager.getFactionAlliance(factionId) ?: return null

        val memberNames = alliance.membersSorted.mapNotNull { factionId ->
            Global.getSector().getFaction(factionId)?.displayName
        }
        val alignment = alliance.alignment

        return AllianceInfo(
            name = alliance.name,
            memberNames = memberNames,
            alignmentName = alignment?.getName() ?: "",
            alignmentColor = alignment?.color ?: Misc.getBasePlayerColor(),
        )
    }
}
