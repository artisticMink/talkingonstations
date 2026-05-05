package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
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

    override fun canExecute(context: GameInfoInterface): Boolean {
        return true
    }

    override fun getText(gameInfo: GameInfoInterface): String = markdown {
        val factions = Global.getSector().allFactions.filter { it.id !in excluded }
        h2("Diplomatic standings between factions of the Persean Sector")
        +factions.joinToString(
            separator = "\n\n",
            transform = { faction -> getFactionBlock(faction, factions) }
        )
        line()
    }

    private fun getFactionBlock(faction: FactionAPI, others: List<FactionAPI>): String = markdown {
        h3(faction.displayName)
        list(
            others
                .filter { it.id != faction.id }
                .map { other -> "${other.displayName}: ${faction.getRelationshipLevel(other).displayName}" }
        )
    }
}
