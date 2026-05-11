package maver.talkingonstations.llm

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI

object MixinUtils {
    fun gender(person: PersonAPI): String = when (person.name.gender) {
        FullName.Gender.ANY -> "concept of gender does not apply for them"
        FullName.Gender.FEMALE -> "female"
        FullName.Gender.MALE -> "male"
    }

    fun faction(person: PersonAPI): String = person.faction.displayNameWithArticle

    fun marketName(market: MarketAPI): String = market.name
}