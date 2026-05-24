package maver.talkingonstations

import lunalib.lunaSettings.LunaSettings

/**
 * Wrapper for [LunaSettings]
 */
object TosSettings {
    private const val ID = TosStrings.ModConfig.ID

    val api: String get() = LunaSettings.getString(ID, "tos_api") ?: ""
    val apiUrl: String get() = LunaSettings.getString(ID, "tos_apiUrl") ?: ""
    val apiKey: String get() = LunaSettings.getString(ID, "tos_apiKey") ?: ""
    val apiModel: String get() = LunaSettings.getString(ID, "tos_apiModel") ?: ""

    val ignoredFactions: Set<String> get() = commaSet("neutral, remnant, omega, mercenary, ML_bounty, scavengers, sleeper, poor, derelict, nex_derelict, threat, dweller, player, nex_temp")
    val ignoredMarkets: Set<String> get() = commaSet("")
    val ignoredConditions: Set<String> get() = commaSet("")

    val temperature: Double get() = LunaSettings.getDouble(ID, "tos_temperature") ?: 1.0
    val topK: Double get() = LunaSettings.getDouble(ID, "tos_topK") ?: 0.0
    val topP: Double get() = LunaSettings.getDouble(ID, "tos_topP") ?: 0.95

    val guardrailsEnabled: Boolean get() = LunaSettings.getBoolean(ID, "tos_guardrailsEnabled") ?: true
    val maxTokens: Int get() = LunaSettings.getInt(ID, "tos_maxTokens") ?: 1000
    val reasoningEffort: String get() = LunaSettings.getString(ID, "tos_reasoningEffort") ?: "medium"
    val enableCharacterMemory: Boolean get() = LunaSettings.getBoolean(ID, "tos_persistentMemory") ?: false
    val characterMemoryBudget: Int get() = LunaSettings.getInt(ID, "tos_persistentMemoryBudget") ?: 0

    val isToolCallingEnabled: Boolean get() = LunaSettings.getBoolean(ID, "tos_enableToolCalls") ?: false
    val showToolCallingIndicator: Boolean get() = LunaSettings.getBoolean(ID, "tos_showToolCalling") ?: false

    val modsCcApi: String get() = LunaSettings.getString(ID, "tos_cc_api") ?: ""
    val modsCcApiUrl: String get() = LunaSettings.getString(ID, "tos_cc_apiUrl") ?: ""
    val modsCcApiKey: String get() = LunaSettings.getString(ID, "tos_cc_apiKey") ?: ""
    val modsCcApiModel: String get() = LunaSettings.getString(ID, "tos_cc_apiModel") ?: ""
    val modsCcEnabled: Boolean get() = LunaSettings.getBoolean(ID, "tos_cc_enabled") ?: false
    val modsCcOnlyRewrite: Boolean get() = LunaSettings.getBoolean(ID, "tos_cc_onlyRewrite") ?: false
    val modsCcWithDescription: Boolean get() = LunaSettings.getBoolean(ID, "tos_cc_withDescription") ?: false
    val modsCcPersistenceEnabled: Boolean get() = LunaSettings.getBoolean(ID, "tos_cc_persistenceEnabled") ?: false
    val modsCcRequestInterval: Int get() = LunaSettings.getInt(ID, "tos_cc_requestInterval") ?: 20

    private fun commaSet(list: String): Set<String> =
        list
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()
}
