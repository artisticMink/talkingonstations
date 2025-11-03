package maver.talkingonstations.characters.random

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import maver.talkingonstations.characters.MarketPersonInterface
import maver.talkingonstations.characters.RandomPerson
import maver.talkingonstations.dto.TosPersonData
import maver.talkingonstations.extensions.replaceFromMap

object RandomTrader : MarketPersonInterface {
    override var enabled: Boolean = false
    override var instructions: String = ""

    override fun getNewPerson(factionId: String): PersonAPI {
        val person = RandomPerson.create()
        person.rankId = Ranks.POST_TRADER
        person.postId = getRandomPost()

        val commodity = when (person.postId) {
            Ranks.POST_ARMS_DEALER -> Commodities.HAND_WEAPONS
            else -> getRandomSafeCommodity()
        }

        person.memoryWithoutUpdate.set("\$tosPersonData", TosPersonData(
            personType = RandomTrader,
            commodity = commodity
        ))

        return person
    }

    override fun getText(person: PersonAPI): String {
        if (person.memoryWithoutUpdate.contains("\$tosPersonData")) {
            val data = person.memoryWithoutUpdate.get("\$tosPersonData") as TosPersonData
            instructions.replaceFromMap(
                mapOf(
                    "rank" to person.rankId,
                    "commodity" to data.commodity
                )
            )
        }

        return ""
    }

    private fun getRandomPost(): String {
        return listOf(
            Ranks.POST_TRADER,
            Ranks.POST_ARMS_DEALER,
            Ranks.POST_MERCHANT,
            Ranks.POST_SPACER,
            Ranks.POST_AGENT,
            Ranks.POST_ENTREPRENEUR
        ).random()
    }

    private fun getRandomSafeCommodity(): String {
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