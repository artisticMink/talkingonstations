package maver.talkingonstations.dto

import maver.talkingonstations.characters.PersonTypeInterface


data class TosPersonData(
    val personType: PersonTypeInterface,
    val commodity: String
)