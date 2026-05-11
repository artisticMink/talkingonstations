package maver.talkingonstations

import lunalib.lunaSettings.LunaSettings

object TosSettings {
    private const val ID = TosStrings.ModConfig.ID

    val api: String get() = LunaSettings.getString(ID, "tos_api") ?: ""
    val apiUrl: String get() = LunaSettings.getString(ID, "tos_apiUrl") ?: ""
    val apiKey: String get() = LunaSettings.getString(ID, "tos_apiKey") ?: ""

    val ignoredFactions: Set<String> get() = commaSet("tos_ignoredFactions")
    val ignoredMarkets: Set<String> get() = commaSet("tos_ignoredMarkets")
    val ignoredConditions: Set<String> get() = commaSet("tos_ignoredConditions")

    val temperature: Double get() = LunaSettings.getDouble(ID, "tos_temperature") ?: 1.0
    val topK: Double get() = LunaSettings.getDouble(ID, "tos_topK") ?: 0.0
    val topP: Double get() = LunaSettings.getDouble(ID, "tos_topP") ?: 0.95

    val guardrailsEnabled: Boolean get() = LunaSettings.getBoolean(ID, "tos_guardrailsEnabled") ?: true
    val maxTokens: Int get() = LunaSettings.getInt(ID, "tos_maxTokens") ?: 1000
    val reasoningEffort: String get() = LunaSettings.getString(ID, "tos_reasoningEffort") ?: "medium"

    private fun commaSet(key: String): Set<String> =
        (LunaSettings.getString(ID, key) ?: "")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()
}
