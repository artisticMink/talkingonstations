package maver.talkingonstations

import lunalib.lunaSettings.LunaSettings

object TosSettings {
    val ignoredFactions: Set<String> = commaSet("tos_ignoredFactions")
    val ignoredMarkets: Set<String> = commaSet("tos_ignoredMarkets")
    val ignoredConditions: Set<String> = commaSet("tos_ignoredConditions")

    private fun commaSet(key: String): Set<String> =
        (LunaSettings.getString("maver_talkingonstations", key) ?: "")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()
}
