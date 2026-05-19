package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.util.Misc
import java.util.Locale

object ToolUtils {

    /**
     * Resolves a commodity by its display name.
     */
    fun findCommodity(name: String): CommoditySpecAPI? {
        val economy = Global.getSector().economy
        return economy.allCommodityIds
            .map { economy.getCommoditySpec(it) }
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * The most lucrative markets to sell a commodity at.
     */
    fun bestSellPrices(
        commodityId: String,
        origin: SectorEntityToken,
        quantity: Double = 1.0,
        limit: Int = 3,
    ): List<Pair<String, String>> {
        // All markets with a com relay
        return Global.getSector().economy.marketsCopy
            .asSequence()
            .filter { it.hasCondition(Conditions.COMM_RELAY) }
            // Best price
            .mapNotNull { market ->
                val com = market.getCommodityData(commodityId)
                if (com == null || com.isNonEcon || com.maxDemand <= 0) return@mapNotNull null
                market to market.getDemandPrice(commodityId, quantity, true)
            }
            .sortedByDescending { (_, price) -> price }
            .take(limit)
            // Distance and market name
            .map { (market, price) ->
                val ly = Misc.getDistanceLY(origin, market.primaryEntity)
                price.toInt().toString() to String.format(Locale.US, "%.1f ly away at ${market.name}", ly)
            }
            .toList()
    }
}