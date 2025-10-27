package maver.talkingonstations.llm.dto

import maver.talkingonstations.chat.ChatRoles

data class Message(
    val role: ChatRoles,
    var content: String,

    //Some APIs might require additional metainformation being submitted
    val meta: MutableMap<String, String> = mutableMapOf()
)