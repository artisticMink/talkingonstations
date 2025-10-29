package maver.talkingonstations.chat

enum class ChatRoles {
    /**
     * The player
     */
    USER,

    /**
     * The NPC
     */
    ASSISTANT,

    /**
     * Some endpoints may use these for instructions.
     */
    SYSTEM,

    /**
     * User-facing status/info/error messages.
     * Should not be sent to the API client.
     */
    INFO,
}