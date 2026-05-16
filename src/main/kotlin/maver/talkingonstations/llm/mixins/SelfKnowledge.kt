package maver.talkingonstations.llm.mixins

import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.characters.market.MarketPersonInterface
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.dto.GameInfoInterface
import maver.talkingonstations.llm.enum.Section
import maver.talkingonstations.llm.markdown

/**
 * Provides contextual information about the person. Along with basic location information.
 *
 * Will fetch specific instructions if the person has been created through a PersonType
 * @see maver.talkingonstations.characters.archetypes.CharacterArchetypeInterface
 */
class SelfKnowledge: ContextMixinInterface {
    override var enabled: Boolean = false
    override lateinit var section: Section

    override fun render(gameInfo: GameInfoInterface): String? {
        val person: PersonAPI = gameInfo.npc ?: return null
        gameInfo.market ?: return null

        return renderFor(person, gameInfo)
    }

    private fun renderFor(person: PersonAPI, gameInfo: GameInfoInterface): String = markdown {
        val marketPersonData: MarketPersonData? = TosRegistry.getMarketPersons()[person.id]
        val personExtension: MarketPersonInterface? = marketPersonData?.personExtension

        h2("${person.name.fullName}")

        if (personExtension != null) {
            h3("Additional character instructions")
            p(personExtension.getInstructions())
        }

        h3("Background")
        if (marketPersonData != null && marketPersonData.background.isNotEmpty()) p(marketPersonData.background)
        if (personExtension != null) p(personExtension.getBackground())

        if (person.memoryWithoutUpdate.contains(TosMemoryKeys.ARCHETYPE)) {
            val archetype = TosRegistry.getArchetypes().find { it.getKey() == person.memoryWithoutUpdate.get(TosMemoryKeys.ARCHETYPE) }
            h3("Archetype: $archetype")
            p(archetype?.getText(gameInfo) ?: "")
        }

        line()
    }
}
