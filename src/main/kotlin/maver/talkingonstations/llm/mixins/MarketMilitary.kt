package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI
import com.fs.starfarer.api.impl.campaign.ids.Industries
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Describes the military posture of the current market: garrisoned forces, orbital
 * defenses, and active threats (blockades, pirate and pather activity). Self-silences
 * via [canExecute] when the market has nothing notable to report.
 */
class MarketMilitary : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun canExecute(context: GameInfoInterface): Boolean {
        val market = context.market ?: return false
        if (market.id in TosSettings.ignoredMarkets) return false
        return threatConditions(market).isNotEmpty() || defensiveIndustries(market).isNotEmpty()
    }

    override fun getText(gameInfo: GameInfoInterface): String = markdown {
        val market = requireNotNull(gameInfo.market)

        h2("Military posture of ${market.name}")

        val defenses = defensiveIndustries(market)
        if (defenses.isNotEmpty()) {
            h3("Garrison and defenses")
            list(defenses.map(::industryLine))
        }

        val threats = threatConditions(market)
            .map(MarketConditionAPI::getName)
            .filter { it.isNotBlank() }
        if (threats.isNotEmpty()) {
            h3("Active threats and security events")
            list(threats)
        }

        line()
    }

    private fun defensiveIndustries(market: MarketAPI): List<Industry> =
        market.industries.filter { industry ->
            if (industry.isHidden) return@filter false
            val spec = industry.spec ?: return@filter false
            DEFENSIVE_TAGS.any(spec::hasTag) || industry.id == Industries.PLANETARYSHIELD
        }

    private fun threatConditions(market: MarketAPI): List<MarketConditionAPI> =
        market.conditions
            .filter { it.id in MarketLore.MILITARY_CONDITIONS }
            .filter { it.id !in TosSettings.ignoredConditions }

    private fun industryLine(industry: Industry): String {
        val name = industry.currentName
        return if (industry.isDisrupted) "$name (disrupted)" else name
    }

    companion object {
        private val DEFENSIVE_TAGS = setOf(
            Industries.TAG_MILITARY,
            Industries.TAG_COMMAND,
            Industries.TAG_PATROL,
            Industries.TAG_STATION,
            Industries.TAG_BATTLESTATION,
            Industries.TAG_STARFORTRESS,
            Industries.TAG_GROUNDDEFENSES,
        )
    }
}
