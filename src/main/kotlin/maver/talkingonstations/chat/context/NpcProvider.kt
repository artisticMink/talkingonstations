package maver.talkingonstations.chat.context

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.llm.ContextProviderInterface
import maver.talkingonstations.llm.dto.GameInfoInterface

class NpcProvider: ContextProviderInterface {
    override var enabled: Boolean = false

    override fun canExecute(context: GameInfoInterface): Boolean = context.npc != null && context.market != null

    override fun getText(gameInfo: GameInfoInterface): String {
        val npc: PersonAPI = requireNotNull(gameInfo.npc)
        val market: MarketAPI = requireNotNull(gameInfo.market)

        return "Your description (${npc.name.fullName})\n" +
                "The name of your character is ${npc.name.fullName}, a human ${gender(npc)}. You belong to the ${faction(npc)} faction, currently located in a Bar on ${marketName(market)}." +
                " A ${colony(market)} in the ${location(market)}"
    }

    private fun name(person: PersonAPI) = person.name.fullName

    private fun gender(person: PersonAPI) = when (person.name.gender) {
        FullName.Gender.ANY -> "that presents themselves as nonbinary"
        FullName.Gender.FEMALE -> "female"
        FullName.Gender.MALE -> "male"
    }

    private fun faction(person: PersonAPI) = person.faction.displayNameWithArticle

    private fun marketName(market: MarketAPI) = market.name

    private fun colony(market: MarketAPI) = when(market.size) {
        1 -> "shelter, housing a few dozen people"
        2 -> "small outpost, housing a few hundred people"
        3 -> "small colony, housing a few thousand people"
        4 -> "developing colony, housing tens of thousands"
        5 -> "colony, housing up to a hundreds thousand people"
        6 -> "large colony, housing at least a million people"
        7 -> "metropolis, housing millions, if not tens of millions of people"
        8,9,10,11,12,13,14,15 -> "hive-city, housing tens of millions of people"
        else -> ""
    }

    private fun location(market: MarketAPI) = market.starSystem.name
}