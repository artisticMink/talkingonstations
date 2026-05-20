package maver.talkingonstations.llm.dto

interface ConversationUiInterface {
    fun forceEnd(lastMessage: String, notice: String? = null): Any
}