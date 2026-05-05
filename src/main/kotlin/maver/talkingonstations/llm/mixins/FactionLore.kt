package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
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
    val excluded = TosSettings.ignoredFactions

    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun canExecute(context: GameInfoInterface): Boolean {
        return true;
    }

    override fun getText(gameInfo: GameInfoInterface): String = markdown {
        h2("Factions of the Persean Sector and their lore")
        +Global.getSector().allFactions.filter{ faction -> !excluded.contains(faction.id) }.joinToString(
            separator = "\n\n",
            transform = { faction -> getFactionBlock(faction) }
        )
        line()
    }

    private fun getFactionBlock(faction: FactionAPI): String = markdown {
        h3(faction.displayName)
        h4("Background")
        p(Global.getSettings().getDescription(faction.id, Type.FACTION).text1)

        if (!faction.illegalCommodities.isEmpty()) {
            h3("Illegal commodities in ${faction.displayName} space")
            list(faction.illegalCommodities)
        }

        h3("Relationship with player")

        when (faction.relToPlayer.isHostile) {
            true -> p("${faction.displayName} is openly hostile towards the player")
            false -> p("${faction.displayName} is not openly hostile towards the player")
        }

        when(faction.relToPlayer.rel) {
            in 0.5f..1f -> p("The player has a good standing towards ${faction.displayName}")
            in -0.49f..0.49f -> p("${faction.displayName} has a neutral standing towards the player")
            in -1f..-0.5f -> p("The player has a terrible standing towards ${faction.displayName}")
        }
    }
}