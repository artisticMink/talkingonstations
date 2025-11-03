package maver.talkingonstations.characters.random

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import maver.talkingonstations.characters.RandomPerson
import maver.talkingonstations.extensions.replaceFromMap


/**
 * This class initializes a smuggler character with random attributes
 *
 * @property person The randomly generated person object
 * @property commodity The primary type of commodity associated with the smuggler.
 *
 * @param factionId The ID of the faction to which the smuggler will belong. If null, a random faction is selected.
 */
class RandomSmuggler(factionId: String? = null) {
    val person: PersonAPI = RandomPerson.Companion.create(factionId ?: getRandomFaction())
    val commodity: String = getRandomUnsafeCommodity()

    init {
        person.rankId = Ranks.POST_SMUGGLER
        person.postId = Ranks.POST_SMUGGLER
    }

    fun getText(template: String): String {
        return template.replaceFromMap(getTemplateVariables())
    }

    fun getTemplateVariables(): Map<String, String> {
        return mapOf(
            "{{rank}}" to person.rankId,
            "{{commodity}}" to commodity
        )
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