package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Describes the commerce side of the current market: industries, trade posture,
 * local shortages and surpluses, and economic unrest. Silent on factional lore and
 * military posture (MarketLore and MarketMilitary cover those).
 */
class MarketEconomy : ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        val market = gameInfo.market ?: return null
        if (market.id in TosSettings.ignoredMarkets) return null

        return markdown {
            h2("Economy of ${market.name}")

            val industries = market.industries.filter { !it.isHidden }
            if (industries.isNotEmpty()) {
                h3("Industries and structures")
                list(industries.map(::industryLine))
            }

            h3("Trade posture")
            list(tradePosture(market))

            shortages(market).takeIf { it.isNotEmpty() }?.let {
                h3("Current shortages")
                list(it)
            }

            surpluses(market).takeIf { it.isNotEmpty() }?.let {
                h3("Current surpluses")
                list(it)
            }

            val events = market.conditions
                .filter { it.id in MarketLore.ECONOMY_CONDITIONS && it.id != Conditions.FREE_PORT }
                .filter { it.id !in TosSettings.ignoredConditions }
                .map(MarketConditionAPI::getName)
                .filter { it.isNotBlank() }

            if (events.isNotEmpty()) {
                h3("Ongoing economic events")
                list(events)
            }

            line()
        }
    }

    private fun industryLine(industry: Industry): String {
        val name = industry.currentName
        val suffix = buildList {
            if (industry.isDisrupted) add("disrupted")
            industry.visibleInstalledItems
                .mapNotNull { Global.getSettings().getSpecialItemSpec(it.id)?.name }
                .forEach { add("equipped with $it") }
        }
        return if (suffix.isEmpty()) name else "$name (${suffix.joinToString(", ")})"
    }

    private fun tradePosture(market: MarketAPI): List<String> = buildList {
        if (market.isFreePort) {
            add("Registered as a Free Port, customs is nominal and galactic trade law is ignored locally.")
        } else {
            add("Tariffs are ${tariffDescriptor(market.tariff.modifiedValue)}.")
            val factionIllegal = market.faction.illegalCommodities
            if (factionIllegal.isNotEmpty()) {
                add("Locally enforced prohibitions follow ${market.faction.displayNameWithArticle} law.")
            }
        }
        if (market.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
            add("A black market operates here, openly enough to be known to regulars.")
        }
        if (market.isImmigrationClosed) {
            add("Immigration is closed. The colony is not accepting new arrivals - at least by legal means.")
        }
    }

    private fun tariffDescriptor(tariff: Float): String = when {
        tariff < 0.05f -> "negligible"
        tariff < 0.2f -> "modest"
        tariff < 0.35f -> "standard for the sector"
        else -> "steep"
    }

    private fun shortages(market: MarketAPI): List<String> =
        market.allCommodities
            .filter { !it.commodity.isMeta && !it.commodity.isNonEcon }
            .filter { it.deficitQuantity > 0 }
            .sortedByDescending { it.deficitQuantity }
            .take(3)
            .map { commodityLine(it, it.deficitQuantity) }

    private fun surpluses(market: MarketAPI): List<String> =
        market.allCommodities
            .filter { !it.commodity.isMeta && !it.commodity.isNonEcon }
            .filter { it.excessQuantity > 0 }
            .sortedByDescending { it.excessQuantity }
            .take(3)
            .map { commodityLine(it, it.excessQuantity) }

    private fun commodityLine(commodity: CommodityOnMarketAPI, magnitude: Int): String {
        val name = commodity.commodity.name
        val intensity = when {
            magnitude >= 8 -> "severe"
            magnitude >= 4 -> "notable"
            else -> "mild"
        }
        return "$name ($intensity)"
    }
}
