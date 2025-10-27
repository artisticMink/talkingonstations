package maver.talkingonstations.llm

import maver.talkingonstations.chat.Chat

interface ContextProviderInterface {
    var enabled: Boolean

    fun canExecute(context: Chat.ChatContextInterface): Boolean
    fun getKey(): String = this::class.java.simpleName
    fun getText(chatContext: Chat.ChatContextInterface): String
}