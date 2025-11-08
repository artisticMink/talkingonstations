package maver.talkingonstations.characters.market.dto

import maver.talkingonstations.llm.ContextMixinInterface

data class PersonExtensionData(
    val instructions: String = "",
    val lore: String = "",
    val knowledgeWhitelist: List<ContextMixinInterface> = listOf(),
    val knowledgeBlacklist: List<ContextMixinInterface> = listOf()
)
