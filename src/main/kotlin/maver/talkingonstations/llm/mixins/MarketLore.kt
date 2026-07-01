package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.loading.Description.Type
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

class MarketLore : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        val market = gameInfo.market ?: return null
        if (market.id in TosSettings.ignoredMarkets) return null

        return markdown {
            h2(market.name)
            p(intro(market))
            p("${market.name} is ${sizeDescriptor(market.size)} and its day-to-day rule is ${stabilityDescriptor(market.stabilityValue)}.")

            planetLore(market)?.let {
                h3("Canonical lore")
                p(it)
            }

            val flavorConditions = market.conditions
                .filter { it.id !in TosSettings.ignoredConditions }
                .filter { it.id !in ECONOMY_CONDITIONS && it.id !in MILITARY_CONDITIONS }
                .map(MarketConditionAPI::getName)
                .filter { it.isNotBlank() }

            if (flavorConditions.isNotEmpty()) {
                h3("Local conditions and character")
                list(flavorConditions)
            }

            market.admin?.let { admin ->
                if (!market.isPlayerOwned) {
                    p("The colony is administered by ${admin.name.fullName}.")
                }
            }

            line()
        }
    }

    private fun intro(market: MarketAPI): String {
        val typeName = market.planetEntity?.typeNameWithLowerCaseWorld ?: "station"
        val systemClause = market.starSystem?.baseName?.let { " in the $it system" } ?: ""
        return "${market.name} is a $typeName$systemClause, belonging to ${market.faction.displayNameLongWithArticle}."
    }

    private fun planetLore(market: MarketAPI): String? {
        val planetId = market.planetEntity?.id ?: return null
        val description = Global.getSettings().getDescription(planetId, Type.PLANET) ?: return null
        return if (description.hasText1()) description.text1 else null
    }

    private fun sizeDescriptor(size: Int): String = when {
        size <= 1 -> "barely more than a semi-permanent camp"
        size == 2 -> "a lone outpost"
        size == 3 -> "a small colony"
        size <= 5 -> "an established settlement"
        size <= 7 -> "a major world with multiple urban centers"
        else -> "a sprawling core world with multiple cities"
    }

    private fun stabilityDescriptor(stability: Float): String = when {
        stability < 3f -> "teetering on collapse"
        stability < 5f -> "restive"
        stability < 7f -> "stable"
        stability < 9f -> "orderly"
        else -> "thoroughly orderly"
    }

    companion object {
        val ECONOMY_CONDITIONS = setOf(
            Conditions.FREE_PORT,
            Conditions.RECENT_UNREST,
            Conditions.EVENT_FOOD_SHORTAGE,
            Conditions.ORGANIZED_CRIME,
            Conditions.VICE_DEMAND,
        )

        val MILITARY_CONDITIONS = setOf(
            Conditions.HOSTILE_ACTIVITY,
            Conditions.BLOCKADED,
            Conditions.PIRATE_ACTIVITY,
            Conditions.PATHER_CELLS,
            Conditions.PIRACY_RESPITE,
        )
    }
}
