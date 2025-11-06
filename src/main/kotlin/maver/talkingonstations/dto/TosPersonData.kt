package maver.talkingonstations.dto

import maver.talkingonstations.characters.random.RandomPersonInterface

data class TosPersonData(
    val personType: RandomPersonInterface? = null,
    val commodity: String? = null
)