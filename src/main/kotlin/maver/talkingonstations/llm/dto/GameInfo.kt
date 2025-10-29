package maver.talkingonstations.llm.dto

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.PersonAPI

data class GameInfo(
    override val player: PersonAPI? = null,
    override val npc: PersonAPI? = null,
    override val market: MarketAPI? = null
): GameInfoInterface