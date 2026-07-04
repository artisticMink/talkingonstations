package maver.talkingonstations.ui

enum class ButtonId(val id: Int) {
    CHAT_SEND_BUTTON(1),
    CHAT_RETRY_BUTTON(2),
    CHAT_QUIT_BUTTON(3),
    DEBUG_CHAT_PRINT_HISTORY(101),
    DEBUG_CLOSE(102),
    DEBUG_TOGGLE_BLOCK(103),

    // TriChat profile dialog
    PROFILE_TAB(201),
    API_SETTINGS_TAB(202),
    PROFILE_CLOSE(203),
    STATUS(204),
    FACTION_TAB(205)
}