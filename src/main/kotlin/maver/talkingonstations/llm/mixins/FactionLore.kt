package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.loading.Description.Type
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Creates a list of factions, their lore (text1), currently illegal commodities and their relation to the player.
 */
class FactionLore : ContextMixinInterface {
    companion object {
        private const val PLACEHOLDER_DESCRIPTION = "No description... yet"
    }

    val excluded = TosSettings.ignoredFactions

    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String = markdown {
        h2("Factions of the Persean Sector")
        +Global.getSector().allFactions
            .filter { it.id !in excluded }
            .filter { hasLore(it) }
            .joinToString(separator = "\n\n", transform = ::getFactionBlock)
        line()
    }

    private fun hasLore(faction: FactionAPI): Boolean {
        val description = Global.getSettings().getDescription(faction.id, Type.FACTION).text1
        return !description.isNullOrBlank() && description != PLACEHOLDER_DESCRIPTION
    }

    private fun getFactionBlock(faction: FactionAPI): String = markdown {
        val description = Global.getSettings().getDescription(faction.id, Type.FACTION).text1
        h3(faction.displayName)
        h4("Background")
        p(description)

        if (!faction.illegalCommodities.isEmpty()) {
            h3("Illegal commodities in ${faction.displayName} space")
            list(faction.illegalCommodities)
        }

        h3("Relationship with {{player}}")

        when (faction.relToPlayer.isHostile) {
            true -> p("${faction.displayName} is openly hostile towards {{player}}")
            false -> p("${faction.displayName} is not openly hostile towards {{player}}")
        }

        when(faction.relToPlayer.rel) {
            in 0.5f..1f -> p("{{player}} is in good standing with ${faction.displayName}")
            in -0.49f..0.49f -> p("${faction.displayName} has a neutral standing towards {{player}}")
            in -1f..-0.5f -> p("{{player}} has a terrible standing with ${faction.displayName}")
        }

        val factions = Global.getSector().allFactions.filter { it.id !in excluded }
        +getStandingsBlock(faction, factions)
    }

    private fun getStandingsBlock(faction: FactionAPI, others: List<FactionAPI>): String = markdown {
        val standings = others
            .filter { it.id != faction.id }
            .mapNotNull { other ->
                val level = faction.getRelationshipLevel(other)
                if (level.displayName == RepLevel.NEUTRAL.displayName) null
                else "${other.displayName}: ${level.displayName}"
            }

        h3("Standings")
        list(standings)
    }
}