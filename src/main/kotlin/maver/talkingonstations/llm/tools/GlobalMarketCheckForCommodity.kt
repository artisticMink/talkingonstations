package maver.talkingonstations.llm.tools

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolUtils
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ParamType
import maver.talkingonstations.llm.dto.ToolArguments
import maver.talkingonstations.llm.dto.ToolParameter
import maver.talkingonstations.llm.dto.ToolResult
import maver.talkingonstations.llm.markdown

class GlobalMarketCheckForCommodity : ToolInterface {
    override val name = "global_market_check_for_commodity"
    override val description = "Sector-wide price check for a single commodity. " +
            "Requires a comm relay in range. Returns the best-paying markets currently buying the commodity."
    override val isTransient = false
    override val parameters = listOf(
        ToolParameter(
            name = "commodity_name",
            type = ParamType.STRING,
            description = "The name of a single commodity",
        )
    )

    override fun execute(args: ToolArguments, gameInfo: GameInfoInterface): ToolResult {
        // Presence and non-blankness were validated against [parameters] upstream.
        val name = args.string("commodity_name")

        if (!Global.getSector().intelManager.isPlayerInRangeOfCommRelay) {
            return ToolResult("This market is not in range of a hyperspace comm relay. Price check impossible.")
        }

        val commodity: CommoditySpecAPI = ToolUtils.findCommodity(name)
            ?: return ToolResult("There's no commodity of that name in the sector. Price check impossible.")

        val playerFleet = Global.getSector().playerFleet
            ?: return ToolResult("The player's location is unknown. Price check impossible.")

        val prices: List<Pair<String, String>> = ToolUtils.bestSellPrices(commodity.id, playerFleet)
        if (prices.isEmpty()) {
            return ToolResult("No comm-relay-connected market in the sector is buying ${commodity.name}. Price check impossible.")
        }

        return ToolResult(markdown {
            h2("Sector price check for ${commodity.name}")
            list(prices.map { (price, distance) -> "Sells for $price credits, $distance" })
        })
    }
}
