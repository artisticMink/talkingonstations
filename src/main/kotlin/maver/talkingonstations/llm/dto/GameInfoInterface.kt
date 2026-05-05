package maver.talkingonstations.llm.dto

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI

interface GameInfoInterface {
    val player: PersonAPI?
    val npc: PersonAPI?
    val market: MarketAPI?
}