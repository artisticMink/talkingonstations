package maver.talkingonstations.extensions

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