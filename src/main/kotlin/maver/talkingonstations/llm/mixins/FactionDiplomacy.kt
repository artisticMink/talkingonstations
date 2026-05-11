package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.RepLevel
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Creates a list of factions and each one's standing to every other non-excluded faction.
 */
class FactionDiplomacy : ContextMixinInterface {
    val excluded = TosSettings.ignoredFactions

    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String = markdown {
        val factions = Global.getSector().allFactions.filter { it.id !in excluded }
        h2("Diplomatic standings between factions")
        p("Relations from worst to best are: vengeful, hostile, inhospitable, suspicious, neutral, favorable, welcoming, friendly, cooperativ.")
        +factions.joinToString(
            separator = "\n\n",
            transform = { faction -> getFactionBlock(faction, factions) }
        )
        line()
    }

    private fun getFactionBlock(faction: FactionAPI, others: List<FactionAPI>): String = markdown {
        val standings = others
            .filter { it.id != faction.id }
            .mapNotNull { other ->
                val level = faction.getRelationshipLevel(other)
                if (level.displayName == RepLevel.NEUTRAL.displayName) null
                else "${other.displayName}: ${level.displayName}"
            }

        if (standings.isNotEmpty()) {
            h3(faction.displayName)
            list(standings)
        }
    }
}
