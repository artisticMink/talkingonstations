package maver.talkingonstations

import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import maver.talkingonstations.config.TosCredentialsVault
import maver.talkingonstations.config.TosProfileStore

/**
 * Source of truth for all mod settings
 *
 * Frontend for [maver.talkingonstations.mods.lunasettings.LunaSettingsRegistration] and
 * [TosProfileStore]
 */
object TosSettings {
    private const val MOD_ID = TosStrings.ModConfig.ID

    val api: String get() = LunaSettings.getString(MOD_ID, "tos_api") ?: ""
    val apiUrl: String get() = TosCredentialsVault.url() ?: LunaSettings.getString(MOD_ID, "tos_apiUrl") ?: ""
    val apiKey: String get() = TosCredentialsVault.key() ?: LunaSettings.getString(MOD_ID, "tos_apiKey") ?: ""
    val apiModel: String
        get() = TosProfileStore.current()?.model?.takeIf { it.isNotBlank() }
            ?: LunaSettings.getString(MOD_ID, "tos_apiModel") ?: ""

    val playerProfile: String
        get() = TosProfileStore.current()?.profile?.takeIf { it.isNotBlank() }
            ?: Global.getSector()?.playerPerson?.memoryWithoutUpdate?.getString(TosMemoryKeys.PLAYER_PROFILE)
            ?: ""

    val commissionBackground: String
        get() = TosProfileStore.current()?.commissionBackground?.takeIf { it.isNotBlank() } ?: ""

    val factionBackground: String
        get() = TosProfileStore.current()?.factionBackground?.takeIf { it.isNotBlank() } ?: ""

    val npcStartsChat: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_npcStartsChat") ?: true

    val ignoredFactions: Set<String> get() = commaSet("neutral, remnant, omega, mercenary, ML_bounty, scavengers, sleeper, poor, derelict, nex_derelict, threat, dweller, player, nex_temp")
    val ignoredMarkets: Set<String> get() = commaSet("")
    val ignoredConditions: Set<String> get() = commaSet("")

    val temperature: Double
        get() = TosProfileStore.current()?.temperature ?: LunaSettings.getDouble(MOD_ID, "tos_temperature") ?: 1.0
    val topK: Double
        get() = TosProfileStore.current()?.topK ?: LunaSettings.getDouble(MOD_ID, "tos_topK") ?: 0.0
    val topP: Double
        get() = TosProfileStore.current()?.topP ?: LunaSettings.getDouble(MOD_ID, "tos_topP") ?: 0.95
    val frequencyPenalty: Double
        get() = TosProfileStore.current()?.frequencyPenalty ?: LunaSettings.getDouble(MOD_ID, "tos_frequency_penalty")
        ?: 0.0
    val presencePenalty: Double
        get() = TosProfileStore.current()?.presencePenalty ?: LunaSettings.getDouble(MOD_ID, "tos_presence_penalty")
        ?: 0.0
    val repetitionPenalty: Double
        get() = TosProfileStore.current()?.repetitionPenalty ?: LunaSettings.getDouble(MOD_ID, "tos_repetition_penalty")
        ?: 1.0

    val guardrailsEnabled: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_guardrailsEnabled") ?: true
    val maxTokens: Int
        get() = TosProfileStore.current()?.maxTokens ?: LunaSettings.getInt(MOD_ID, "tos_maxTokens") ?: 1000
    val reasoningEffort: String
        get() = TosProfileStore.current()?.reasoningEffort?.takeIf { it.isNotBlank() }
            ?: LunaSettings.getString(MOD_ID, "tos_reasoningEffort") ?: "medium"
    val enableCharacterMemory: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_persistentMemory") ?: false
    val characterMemoryBudget: Int get() = LunaSettings.getInt(MOD_ID, "tos_persistentMemoryBudget") ?: 0

    val isToolCallingEnabled: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_enableToolCalls") ?: false
    val isDangerousToolCallingEnabled: Boolean
        get() = LunaSettings.getBoolean(MOD_ID, "tos_enableDangerousToolCalls") ?: false
    val showToolCallingIndicator: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_showToolCalling") ?: false

    val modsCcApi: String get() = LunaSettings.getString(MOD_ID, "tos_cc_api") ?: ""
    val modsCcApiUrl: String get() = LunaSettings.getString(MOD_ID, "tos_cc_apiUrl") ?: ""
    val modsCcApiKey: String get() = LunaSettings.getString(MOD_ID, "tos_cc_apiKey") ?: ""
    val modsCcApiModel: String get() = LunaSettings.getString(MOD_ID, "tos_cc_apiModel") ?: ""
    val modsCcEnabled: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_cc_enabled") ?: false
    val modsCcOnlyRewrite: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_cc_onlyRewrite") ?: false
    val modsCcWithDescription: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_cc_withDescription") ?: false
    val modsCcPersistenceEnabled: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_cc_persistenceEnabled") ?: false
    val modsCcRequestInterval: Int get() = LunaSettings.getInt(MOD_ID, "tos_cc_requestInterval") ?: 20
    val modsCcReasoningEffort: String get() = LunaSettings.getString(MOD_ID, "tos_cc_reasoningEffort") ?: "minimal"
    val modsCcWithFloaters: Boolean get() = LunaSettings.getBoolean(MOD_ID, "tos_cc_withFloaters") ?: false
    val modsCcTemperature: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_temperature") ?: 1.0
    val modsCcTopK: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_topK") ?: 0.0
    val modsCcTopP: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_topP") ?: 0.95
    val modsCcFrequencyPenalty: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_frequency_penalty") ?: 0.0
    val modsCcPresencePenalty: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_presence_penalty") ?: 0.0
    val modsCcRepetitionPenalty: Double get() = LunaSettings.getDouble(MOD_ID, "tos_cc_repetition_penalty") ?: 1.0

    private fun commaSet(list: String): Set<String> =
        list
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()
}
