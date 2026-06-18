package maver.talkingonstations.llm.dto

sealed interface TurnResult {
    data class Reply(val messages: List<Message>) : TurnResult

    // A tool call might forcefully end the conversation. I.e. hanging up.
    data class Ended(val messages: List<Message>, val farewell: String) : TurnResult

    // User-facing soft error
    data class Failed(val notice: Message) : TurnResult
}
