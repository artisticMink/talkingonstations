package maver.talkingonstations.characters.market.dto

import maver.talkingonstations.llm.ContextMixinInterface

data class PersonExtensionData(
    val background: String = "",
    val knowledgeBlacklist: List<ContextMixinInterface> = listOf()
)
