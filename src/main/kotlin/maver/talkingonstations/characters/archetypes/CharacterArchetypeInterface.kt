package maver.talkingonstations.characters.archetypes

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.llm.dto.GameInfoInterface

/**
 * A randomly generated npc of a specific archetype
 */
interface CharacterArchetypeInterface {
    var enabled: Boolean
    val templateVars: Map<String, String>

    fun getKey(): String = this::class.java.simpleName
    fun getText(gameInfo: GameInfoInterface): String
    fun getPerson(market: MarketAPI): PersonAPI
}