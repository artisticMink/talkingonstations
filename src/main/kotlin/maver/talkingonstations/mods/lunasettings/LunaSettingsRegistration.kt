package maver.talkingonstations.mods.lunasettings

import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import maver.talkingonstations.TosStrings
import maver.talkingonstations.httpapi.HttpApiRegistry

object LunaSettingsRegistration {

    private const val MOD_ID = TosStrings.ModConfig.ID

    private const val TAB_CONVERSATION = "Conversation"
    private const val TAB_SAMPLER = "Sampler"
    private const val TAB_TOOL_CALLING = "Tool Calling"
    private const val TAB_COMBAT_CHATTER = "Combat Chatter"

    private const val REASONING_CHOICES = "none,minimal,low,medium,high,xhigh,max"

    fun register() {
        registerConversation()
        registerSampler()
        registerToolCalling()
        if (Global.getSettings().modManager.isModEnabled("chatter")) registerCombatChatter()
        LunaSettings.SettingsCreator.refresh(MOD_ID)
    }

    private fun registerConversation() {
        LunaSettings.SettingsCreator.addHeader(MOD_ID, "tos_serviceHeader", "API", TAB_CONVERSATION)
        LunaSettings.SettingsCreator.addRadio(
            MOD_ID, "tos_api", "Service", "",
            "Openrouter", HttpApiRegistry.getApiNames().joinToString(), TAB_CONVERSATION
        )
        LunaSettings.SettingsCreator.addText(MOD_ID, "tos_api", "Conversation models are set up for a save. After starting a save, look for the [Tri-Chat Profile] skill in your ability bar.", TAB_CONVERSATION)
        /**
         *         LunaSettings.SettingsCreator.addString(
         *             MOD_ID, "tos_apiUrl", "API Url", "Url of the endpoint to connect to.",
         *             "https://openrouter.ai/api/v1/chat/completions", TAB_CONVERSATION
         *         )
         *         LunaSettings.SettingsCreator.addString(
         *             MOD_ID, "tos_apiKey", "API Key", "Optional authentification token.",
         *             "", TAB_CONVERSATION
         *         )
         *         LunaSettings.SettingsCreator.addString(
         *             MOD_ID, "tos_apiModel", "API Model", "Model ID for endpoints that serve multiple models.",
         *             "", TAB_CONVERSATION
         *         )
         *         LunaSettings.SettingsCreator.addRadio(
         *             MOD_ID, "tos_reasoningEffort", "Reasoning Effort",
         *             "Only affects models capable of reasoning. Common values are medium or high.",
         *             "medium", REASONING_CHOICES, TAB_CONVERSATION
         *         )
         */
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_npcStartsChat", "NPC starts the chat",
            "The NPC will first check whether they want to talk to the player and respond with a welcome message.",
            true, TAB_CONVERSATION
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_guardrailsEnabled", "Add Guardrails",
            "Guardrails aim to keep the model on track and allow for content appropriate for Starsectors setting while restricting certain topics.",
            true, TAB_CONVERSATION
        )
        LunaSettings.SettingsCreator.addInt(
            MOD_ID, "tos_maxTokens", "Max Response Tokens",
            "The max amount of tokens that are returned in an answer. A token is roughly 0.8 words. Thinking models may need considerably more tokens as the thinking process also takes from this budget.",
            1000, 0, 10000, TAB_CONVERSATION
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_persistentMemory", "NPC Memory",
            "NPCs will have some memory of when and what the last conversation was about. Only affects ToS comms NPCs. Adds a llm request to summarize the conversation on quitting. ",
            false, TAB_CONVERSATION
        )
        LunaSettings.SettingsCreator.addInt(
            MOD_ID, "tos_persistentMemoryBudget", "Memory Budget in words",
            "The amount of words the model should spend on character memory.",
            600, 200, 10000, TAB_CONVERSATION
        )
    }

    private fun registerSampler() {
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_temperature", "Temperature",
            "A lower temperature makes it more likely that the conversation stays on track but might get repetitive, a higher temperature allows for a more diverse output that's prone to incoherence.",
            1.0, 0.0, 2.0, TAB_SAMPLER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_topK", "Top k",
            "Limits the pool of words in a conversation to the K highest candidates.",
            0.0, 0.0, 500.0, TAB_SAMPLER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_topP", "Top p",
            "Limits the pool of words in a conversation to those of whose probabilities sum up to P.",
            0.95, 0.0, 1.0, TAB_SAMPLER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_frequency_penalty", "Frequency Penalty",
            "Aaims to control the repetition of tokens based on how often they appear in the input.",
            0.0, -2.0, 2.0, TAB_SAMPLER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_presence_penalty", "Presence Penalty",
            "Adjusts how often the model repeats specific tokens already used in the input. Higher values make such repetition less likely, while negative values do the opposite..",
            0.0, -2.0, 2.0, TAB_SAMPLER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_repetition_penalty", "Repetition Penalty",
            "Helps to reduce the repetition of tokens from the input. A higher value makes the model less likely to repeat tokens but may lead to incoherence.",
            1.0, 0.0, 2.0, TAB_SAMPLER
        )
    }

    private fun registerToolCalling() {
        LunaSettings.SettingsCreator.addHeader(MOD_ID, "tos_toolHeader", "Tool Calling", TAB_TOOL_CALLING)
        LunaSettings.SettingsCreator.addText(
            MOD_ID, "tos_toolText",
            "Allows NPCs to interface with Starsector via tool calling. 9 out of 10 REDACTED recommend turning this option on.",
            TAB_TOOL_CALLING
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_enableToolCalls", "Enable Tool Calling",
            "Enable the use of tools. Such as looking up the price of a commodity.",
            false, TAB_TOOL_CALLING
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_enableDangerousToolCalls", "Enable dangerous tool calls",
            "Enables the use of potentially dangerous tool calls. Such as placing a bounty on the players fleet.",
            false, TAB_TOOL_CALLING
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_showToolCalling", "Notify on tool calls",
            "Shows a notification whenever a model requests a tool call.",
            false, TAB_TOOL_CALLING
        )
    }

    private fun registerCombatChatter() {
        LunaSettings.SettingsCreator.addHeader(MOD_ID, "tos_cc_serviceHeader", "Combat Chatter", TAB_COMBAT_CHATTER)
        LunaSettings.SettingsCreator.addText(
            MOD_ID, "tos_cc_description",
            "Integration for Combat Chatter by Histidine.\n\nWhen enabled, Chatter is rewritten on-the-fly during combat according to the settings below.",
            TAB_COMBAT_CHATTER)
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_cc_enabled", "Enable",
            "",
            false, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addRadio(
            MOD_ID, "tos_cc_api", "Service", "",
            "Chat-Completion", HttpApiRegistry.getApiNames().joinToString(), TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addString(
            MOD_ID, "tos_cc_apiUrl", "API Url", "Url of the endpoint to connect to.",
            "http://127.0.0.1:8080/v1/chat/completions", TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addString(
            MOD_ID, "tos_cc_apiKey", "API Key", "Optional authentification token.",
            "", TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addString(
            MOD_ID, "tos_cc_apiModel", "API Model", "Model ID for endpoints that serve multiple models.",
            "", TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addRadio(
            MOD_ID, "tos_cc_reasoningEffort", "Reasoning Effort",
            "Only affects models capable of reasoning.",
            "minimal", REASONING_CHOICES, TAB_COMBAT_CHATTER
        )

        // Sampler
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_temperature", "Temperature",
            "A lower temperature makes it more likely that the conversation stays on track but might get repetitive, a higher temperature allows for a more diverse output that's prone to incoherence.",
            1.0, 0.0, 2.0, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_topK", "Top k",
            "Limits the pool of words in a conversation to the K highest candidates.",
            0.0, 0.0, 500.0, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_topP", "Top p",
            "Limits the pool of words in a conversation to those of whose probabilities sum up to P.",
            0.95, 0.0, 1.0, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_frequency_penalty", "Frequency Penalty",
            "Aaims to control the repetition of tokens based on how often they appear in the input.",
            0.0, -2.0, 2.0, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_presence_penalty", "Presence Penalty",
            "Adjusts how often the model repeats specific tokens already used in the input. Higher values make such repetition less likely, while negative values do the opposite..",
            0.0, -2.0, 2.0, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addDouble(
            MOD_ID, "tos_cc_repetition_penalty", "Repetition Penalty",
            "Helps to reduce the repetition of tokens from the input. A higher value makes the model less likely to repeat tokens but may lead to incoherence.",
            1.0, 0.0, 2.0, TAB_COMBAT_CHATTER
        )

        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_cc_onlyRewrite", "Only rewrite chatter",
            "Enrich lines coming from CC. If this is disabled, the model will make up the chatter entirely on its own on the basis of the information provided. Together with 'Add details' and 'Continious chatter' this can great results, but uses a lot of tokens.",
            true, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_cc_withFloaters", "Rewrite floaters",
            "Also process floating messages. These are smaller CC texts that pop up over ships or in the right chatterbox.",
            true, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addInt(
            MOD_ID, "tos_cc_requestInterval", "Delay between rewrites",
            "In seconds.", 10, 1, 60, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_cc_persistenceEnabled", "Continuous Chatter",
            "Sends the whole combat-chatter history of the ongoing fight instead of one-offs.",
            false, TAB_COMBAT_CHATTER
        )
        LunaSettings.SettingsCreator.addBoolean(
            MOD_ID, "tos_cc_withDescription", "Add details",
            "Add exhaustive details on factions involved in the fight, hullmods, wings, weapons and combat AI behavior. Will increase chatter quality at expense of tokens used.",
            false, TAB_COMBAT_CHATTER
        )
    }
}
