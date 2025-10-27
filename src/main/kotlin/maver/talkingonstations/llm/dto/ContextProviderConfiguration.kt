package maver.talkingonstations.llm.dto

data class ContextProviderConfiguration(
    val name: String,
    val fullyQualifiedClassName: String,
    val description: String,
    val enabled: Boolean
)