package maver.talkingonstations.config

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorAPI
import maver.talkingonstations.TosInspector
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * @see /saves/common/talkingonstations/profiles/
 */
object TosProfileStore {
    private const val DIR = "talkingonstations/profiles"
    private const val VERSION = 1

    private const val K_VERSION = "version"
    private const val K_PROFILE = "profile"
    private const val K_COMMISSION_BG = "commissionBackground"
    private const val K_FACTION_BG = "factionBackground"
    private const val K_API = "api"
    private const val K_MODEL = "model"
    private const val K_REASONING = "reasoningEffort"
    private const val K_TEMPERATURE = "temperature"
    private const val K_TOP_P = "topP"
    private const val K_TOP_K = "topK"
    private const val K_FREQ_PEN = "frequencyPenalty"
    private const val K_PRES_PEN = "presencePenalty"
    private const val K_REP_PEN = "repetitionPenalty"
    private const val K_MAX_TOKENS = "maxTokens"

    private var cache: TosProfileConfig? = null
    private var cacheId: String? = null
    private var cacheSector: WeakReference<SectorAPI>? = null

    /**
     * Get the current campaigns config
     */
    fun current(): TosProfileConfig? {
        val sector = Global.getSector() ?: return null
        val id = TosCampaignId.get(create = false)
        if (cacheSector?.get() === sector && cacheId == id && cache != null) return cache

        val cfg = if (id != null) load(id) else TosProfileConfig()
        cache = cfg
        cacheId = id
        cacheSector = WeakReference(sector)
        return cfg
    }

    /**
     * Persist the current campaigns config
     */
    fun save() {
        val cfg = cache ?: return
        val id = TosCampaignId.get(create = true) ?: return
        cacheId = id
        try {
            Global.getSettings().writeJSONToCommon(filename(id), toJson(cfg), true)
        } catch (e: Exception) {
            TosInspector.error("Failed to write profile '$id'", this::class, e)
        }
    }

    private fun filename(id: String) = "$DIR/$id.json"

    /**
     * Loads the config for the given player id
     */
    private fun load(id: String): TosProfileConfig {
        val cfg = TosProfileConfig()
        try {
            val settings = Global.getSettings()
            if (!settings.fileExistsInCommon(filename(id))) return cfg
            fromJson(settings.readJSONFromCommon(filename(id), true), cfg)
        } catch (e: Exception) {
            TosInspector.error("Failed to read profile '$id'; using defaults", this::class, e)
        }
        return cfg
    }

    private fun toJson(cfg: TosProfileConfig): JSONObject {
        val root = JSONObject()
        root.put(K_VERSION, VERSION)
        cfg.profile?.let { root.put(K_PROFILE, it) }
        cfg.commissionBackground?.let { root.put(K_COMMISSION_BG, it) }
        cfg.factionBackground?.let { root.put(K_FACTION_BG, it) }

        val api = JSONObject()
        cfg.model?.let { api.put(K_MODEL, it) }
        cfg.reasoningEffort?.let { api.put(K_REASONING, it) }
        cfg.temperature?.let { api.put(K_TEMPERATURE, it) }
        cfg.topP?.let { api.put(K_TOP_P, it) }
        cfg.topK?.let { api.put(K_TOP_K, it) }
        cfg.frequencyPenalty?.let { api.put(K_FREQ_PEN, it) }
        cfg.presencePenalty?.let { api.put(K_PRES_PEN, it) }
        cfg.repetitionPenalty?.let { api.put(K_REP_PEN, it) }
        cfg.maxTokens?.let { api.put(K_MAX_TOKENS, it) }
        if (api.length() > 0) root.put(K_API, api)

        return root
    }

    private fun fromJson(root: JSONObject, cfg: TosProfileConfig) {
        if (root.has(K_PROFILE)) cfg.profile = root.optString(K_PROFILE).takeIf { it.isNotBlank() }
        if (root.has(K_COMMISSION_BG)) cfg.commissionBackground = root.optString(K_COMMISSION_BG).takeIf { it.isNotBlank() }
        if (root.has(K_FACTION_BG)) cfg.factionBackground = root.optString(K_FACTION_BG).takeIf { it.isNotBlank() }

        val api = root.optJSONObject(K_API) ?: return
        if (api.has(K_MODEL)) cfg.model = api.optString(K_MODEL).takeIf { it.isNotBlank() }
        if (api.has(K_REASONING)) cfg.reasoningEffort = api.optString(K_REASONING).takeIf { it.isNotBlank() }
        if (api.has(K_TEMPERATURE)) cfg.temperature = api.optDouble(K_TEMPERATURE)
        if (api.has(K_TOP_P)) cfg.topP = api.optDouble(K_TOP_P)
        if (api.has(K_TOP_K)) cfg.topK = api.optDouble(K_TOP_K)
        if (api.has(K_FREQ_PEN)) cfg.frequencyPenalty = api.optDouble(K_FREQ_PEN)
        if (api.has(K_PRES_PEN)) cfg.presencePenalty = api.optDouble(K_PRES_PEN)
        if (api.has(K_REP_PEN)) cfg.repetitionPenalty = api.optDouble(K_REP_PEN)
        if (api.has(K_MAX_TOKENS)) cfg.maxTokens = api.optInt(K_MAX_TOKENS)
    }
}
