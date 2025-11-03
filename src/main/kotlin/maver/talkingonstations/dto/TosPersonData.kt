package maver.talkingonstations.dto

import maver.talkingonstations.characters.MarketPersonInterface


data class TosPersonData(
    val personType: MarketPersonInterface,
    val commodity: String
)