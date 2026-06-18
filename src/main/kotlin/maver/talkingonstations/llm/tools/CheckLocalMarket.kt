package maver.talkingonstations.llm.tools

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolParameter
import maver.talkingonstations.llm.dto.ToolResult
import maver.talkingonstations.llm.markdown

/**
 * Gives the model the capability to check the current market that's associated
 * with the comms directory where the npc is listed at.
 */
class CheckLocalMarket : ToolInterface {
    override val name = "check_local_market"
    override val description = "Returns details for every commodity on the local market. No parameters needed."
    override val isTransient = false
    override val parameters = emptyList<ToolParameter>()

    override fun execute(args: ToolArguments, gameInfo: GameInfoInterface): ToolResult {
        val market = gameInfo.market ?: return ToolResult("There is no market at this location. Market overview impossible.")

        // Fetch all commodities that are at the very least tradeable
        val rows = market.commoditiesCopy
            .filter { !it.commodity.isMeta && !it.commodity.isNonEcon }
            .filter { it.maxDemand > 0 || it.available > 0 }
            .sortedBy { it.commodity.name }
            .map { row(market, it) }

        if (rows.isEmpty()) {
            return ToolResult("No commodities available at ${market.name}.")
        }

        return ToolResult(markdown {
            h2("Market overview for ${market.name}")
            list(rows)
        })
    }

    private fun row(market: MarketAPI, c: CommodityOnMarketAPI): String {
        val id = c.commodity.id
        val buy = market.getSupplyPrice(id, 1.0, true).toInt()
        val sell = market.getDemandPrice(id, 1.0, true).toInt()
        val status = statusFor(c)
        return "${c.commodity.name}, buy $buy / sell $sell credits, ${c.stockpile} in stock, $status"
    }

    private fun statusFor(c: CommodityOnMarketAPI): String = when {
        c.deficitQuantity > 0 -> "shortage (${intensity(c.deficitQuantity)})"
        c.excessQuantity > 0 -> "excess (${intensity(c.excessQuantity)})"
        else -> "balanced"
    }

    private fun intensity(magnitude: Int): String = when {
        magnitude >= 8 -> "severe"
        magnitude >= 4 -> "notable"
        else -> "mild"
    }
}
