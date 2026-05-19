package maver.talkingonstations.llm.tools

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolParamInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult
import maver.talkingonstations.llm.markdown

class CheckLocalMarket : ToolInterface {
    override var enabled = false
    override lateinit var description: String
    override lateinit var parameters: ToolParamInterface

    override fun getName(): String = "check_local_market"

    /**
     * No parameters.
     *
     * Returns price, stockpile, and shortage/excess.
     */
    override fun execute(params: Map<String, String>, game: GameInfoInterface): ToolResult {
        val market = game.market
            ?: return ToolResult("There is no market at this location. Market overview impossible.")

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
        return "${c.commodity.name} — buy $buy / sell $sell credits, ${c.stockpile} in stock, $status"
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
