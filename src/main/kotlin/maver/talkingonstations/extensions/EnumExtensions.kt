package maver.talkingonstations.extensions

/**
 * Converts a string to a value or returns the default if the string is not a valid enum value.
 */
@Suppress()
inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
    return try {
        enumValueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        default
    }
}