package maver.talkingonstations.chat

import maver.talkingonstations.llm.dto.Message

class MessageEnhancer(val chat: Chat) {

    fun prepareAll(messages: List<Message>){
        messages.forEach {
            prepareMessage(it)
        }
    }

    fun prepareMessage(message: Message, withPrefix: Boolean = true) {
        if (withPrefix) addNamePrefix(message)
        message.content = replacePlayerPlaceholder(message.content)
        message.content = replaceNpcPlaceholder(message.content)
    }

    fun revertMessage(message: Message) {
        removeNamePrefix(message)
    }

    fun replacePlayerPlaceholder(content: String): String {
        return content.replace("{{player}}",chat.getPlayer().name.fullName)
    }

    fun replaceNpcPlaceholder(content: String): String {
        return content.replace("{{npc}}",chat.getNpc().name.fullName)
    }

    fun addNamePrefix(message: Message) {
        message.content = when (message.role) {
            ChatRoles.USER -> "${chat.getPlayer().name.fullName}:\n" + message.content
            ChatRoles.ASSISTANT -> "${chat.getNpc().name.fullName}:\n" + message.content
            else -> message.content
        }
    }

    fun removeNamePrefix(message: Message) {
        message.content = when (message.role) {
            ChatRoles.USER -> message.content.removePrefix("${chat.getPlayer().name.fullName}:\n")
            ChatRoles.ASSISTANT -> message.content.removePrefix("${chat.getNpc().name.fullName}:\n")
            else -> message.content
        }
    }
}