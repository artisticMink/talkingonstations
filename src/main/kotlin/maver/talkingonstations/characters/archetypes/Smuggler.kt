package maver.talkingonstations.characters.archetypes

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.characters.RandomPerson
import maver.talkingonstations.llm.dto.GameInfoInterface


/**
 * This class initializes a smuggler character with random attributes
 *
 * @param factionId The ID of the faction to which the smuggler will belong. If null, a random faction is selected.
 */
class Smuggler(factionId: String? = null): CharacterArchetypeInterface {
    override var enabled: Boolean = false;
    override val templateVars: Map<String, String> = mapOf(
        "{{commodity}}" to getRandomUnsafeCommodity()
    )

    override fun getPerson(market: MarketAPI): PersonAPI {
        val person = RandomPerson.create(getRandomFaction())

        person.rankId = Ranks.POST_SMUGGLER
        person.postId = Ranks.POST_SMUGGLER
        person.memory.set(TosMemoryKeys.ARCHETYPE, "smuggler" )

        return person
    }

    override fun getText(gameInfo: GameInfoInterface): String {
        TODO()
    }

    private fun getRandomFaction(): String {
        return listOf(
            Factions.INDEPENDENT,
            Factions.SCAVENGERS,
            Factions.LUDDIC_PATH,
            Factions.PIRATES,
        ).random()
    }

    private fun getRandomUnsafeCommodity(): String {
        return listOf(
            Commodities.LOBSTER,
            Commodities.HAND_WEAPONS,
            Commodities.SHIP_WEAPONS,
            Commodities.DRUGS,
            Commodities.ORGANS,
            Commodities.AI_CORES
        ).random()
    }
}