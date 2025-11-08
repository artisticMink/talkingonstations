package maver.talkingonstations.characters.market.dto

import com.fs.starfarer.api.characters.FullName

data class MarketPersonData(
    val gender: FullName.Gender,
    val name: String,
    val surename: String,
    val faction: String,
    val market: String,
    val rank: String?,
    val post: String?,
    val portrait: String,
    val voice: String?,
    val instructionOverwrite: String?,
    val personLore: String?,
    val knowledgeWhitelist: String?,
    val knowledgeBlacklist: String?
)