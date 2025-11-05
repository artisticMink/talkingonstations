package maver.talkingonstations.extensions

/**
 * Replaces placeholders in a string with values from a map.
 *
 * Used for replacing placeholders like {{commodity}} in mixins
 *
 * @param map The map of placeholder keys and values.
 *   Example: mapOf("favorite_food" to Commodities.LOBSTER)
 *
 * @param placeholderPrefix The prefix used to denote placeholders in the string.
 * @param placeholderSuffix The suffix used to denote placeholders in the string.
 *
 * @return The string with placeholders replaced with values from the map.
 */
@Suppress()
fun String.replaceFromMap(
    map: Map<String, String>,
    placeholderPrefix: String = "{{",
    placeholderSuffix: String = "}}"
): String {
    if (map.isEmpty()) return this

    val keysPattern = map.keys
        .sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) }

    val regex = Regex(Regex.escape(placeholderPrefix) + "($keysPattern)" + Regex.escape(placeholderSuffix))

    return regex.replace(this) { matchResult ->
        val key = matchResult.groupValues[1]
        map[key] ?: matchResult.value
    }
}