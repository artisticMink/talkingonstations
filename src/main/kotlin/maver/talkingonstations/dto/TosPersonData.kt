package maver.talkingonstations.dto

import maver.talkingonstations.characters.archetypes.CharacterArchetypeInterface

data class TosPersonData(
    val personType: CharacterArchetypeInterface? = null,
    val commodity: String? = null
)