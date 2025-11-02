package maver.talkingonstations.chat.context

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.dto.TosPersonData
import maver.talkingonstations.llm.ContextProviderInterface
import maver.talkingonstations.llm.dto.GameInfoInterface

/**
 * Provides contextual information about the person. Along with basic location information.
 *
 * Will fetch specific instructions if the person has been created through a PersonType
 * @see maver.talkingonstations.characters.PersonTypeInterface
 */
class NpcProvider: ContextProviderInterface {
    override var enabled: Boolean = false

    override fun canExecute(context: GameInfoInterface): Boolean = context.npc != null && context.market != null

    override fun getText(gameInfo: GameInfoInterface): String {
        val npc: PersonAPI = requireNotNull(gameInfo.npc)
        val market: MarketAPI = requireNotNull(gameInfo.market)

        var personInstructions = ""
        if (npc.memoryWithoutUpdate.contains("\$tosPersonData"))  {
            val tosPersonData = npc.memoryWithoutUpdate.get("\$tosPersonData") as TosPersonData
            personInstructions = tosPersonData.personType.getText(npc)
        }

        return """
            Your description (${npc.name.fullName})
            The name of your character is ${npc.name.fullName}, a human ${gender(npc)}. You belong to the ${faction(npc)} faction, currently located in a Bar on ${marketName(market)}.
            $personInstructions
        """.trimIndent()
    }

    private fun name(person: PersonAPI) = person.name.fullName

    private fun gender(person: PersonAPI) = when (person.name.gender) {
        FullName.Gender.ANY -> "that presents themselves as nonbinary"
        FullName.Gender.FEMALE -> "female"
        FullName.Gender.MALE -> "male"
    }

    private fun faction(person: PersonAPI) = person.faction.displayNameWithArticle

    private fun marketName(market: MarketAPI) = market.name
}