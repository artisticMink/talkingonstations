package maver.talkingonstations.characters.market

import com.fs.starfarer.api.characters.PersonAPI

interface MarketPersonInterface {
    var enabled: Boolean
    var instructions: String

    fun getKey(): String = this::class.java.simpleName
    fun getText(person: PersonAPI): String
    fun getPerson(factionId: String): PersonAPI
}