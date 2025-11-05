package maver.talkingonstations.dto

import maver.talkingonstations.characters.market.MarketPersonInterface


data class TosPersonData(
    val personType: MarketPersonInterface? = null,
    val commodity: String? = null
)