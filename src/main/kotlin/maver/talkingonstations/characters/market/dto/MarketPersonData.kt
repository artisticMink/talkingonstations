package maver.talkingonstations.characters.market.dto

import com.fs.starfarer.api.characters.FullName
import maver.talkingonstations.characters.market.MarketPersonInterface

/**
 * Represents a row in MarketPerson.csv
 * @see /data/config/tos/MarketPerson.csv
 */
data class MarketPersonData(
    val id: String,
    val gender: FullName.Gender,
    val name: String,
    val surname: String,
    val faction: String,
    val market: String,
    val rank: String,
    val post: String,
    val portrait: String,
    val voice: String,
    val background: String,
    val knowledgeBlacklist: String,
    val personExtension: MarketPersonInterface?
)