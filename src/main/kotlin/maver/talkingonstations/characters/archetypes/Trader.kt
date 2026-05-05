package maver.talkingonstations.characters.archetypes

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.characters.RandomPerson
import maver.talkingonstations.extensions.replaceFromMap
import maver.talkingonstations.llm.dto.GameInfoInterface

class Trader : CharacterArchetypeInterface {
    override var enabled: Boolean = false
    override val templateVars: Map<String, String> = mapOf(
        "{{commodity}}" to randomSafeCommodity()
    )

    override fun getPerson(market: MarketAPI): PersonAPI {
        val person = RandomPerson.create()

        person.rankId = Ranks.POST_TRADER
        person.postId = randomPost()
        person.memory.set(TosMemoryKeys.ARCHETYPE, "trader" )

        return person
    }

    override fun getText(gameInfo: GameInfoInterface): String {
        return "You specialize in {{commodity}}".replaceFromMap(templateVars)
    }

    private fun randomPost(): String {
        return listOf(
            Ranks.POST_TRADER,
            Ranks.POST_ARMS_DEALER,
            Ranks.POST_MERCHANT,
            Ranks.POST_SPACER,
            Ranks.POST_AGENT,
            Ranks.POST_ENTREPRENEUR
        ).random()
    }

    private fun randomSafeCommodity(): String {
        return listOf(
            Commodities.SUPPLIES,
            Commodities.FUEL,
            Commodities.CREW,
            Commodities.MARINES,
            Commodities.FOOD,
            Commodities.ORGANICS,
            Commodities.VOLATILES,
            Commodities.ORE,
            Commodities.RARE_ORE,
            Commodities.METALS,
            Commodities.RARE_METALS,
            Commodities.HEAVY_MACHINERY,
            Commodities.DOMESTIC_GOODS,
            Commodities.HAND_WEAPONS,
            Commodities.LUXURY_GOODS,
            Commodities.LOBSTER,
            Commodities.SHIPS,
            Commodities.SURVEY_DATA,
            Commodities.BLUEPRINTS,
            Commodities.SHIP_WEAPONS,
        ).random()
    }
}