package maver.talkingonstations.llm.tools

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import maver.talkingonstations.llm.ToolInterface
import maver.talkingonstations.llm.ToolParamInterface
import maver.talkingonstations.llm.ToolUtils
import maver.talkingonstations.llm.dto.ConversationUi
import maver.talkingonstations.llm.dto.ConversationUiInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.dto.ToolResult
import maver.talkingonstations.llm.markdown

class GlobalMarketCheckForCommodity : ToolInterface {
    override val isTransient: Boolean = false
    override var enabled = false
    override lateinit var description: String
    override lateinit var parameters: ToolParamInterface

    override fun getName(): String = "global_market_check_for_commodity"

    /**
     * {
     *   "commodity_name": "The name of the commodity that will be price-checked"
     * }
     */
    override fun execute(params: Map<String, String>, gameInfo: GameInfoInterface, conversationUi: ConversationUiInterface?): ToolResult {
        val name = params["commodity_name"]
        if (name.isNullOrBlank()) return ToolResult("commodity_name parameter is empty. Bad Request.")

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
