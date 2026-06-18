package maver.talkingonstations.config

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.BaseSharedJSONFile
import maver.talkingonstations.TosInspector
import org.json.JSONObject


object TosCredentialsVault : BaseSharedJSONFile() {
    private const val FILE = "talkingonstations/credentials.json"
    private const val VERSION = 1
    private const val K_VERSION = "version"
    private const val K_NAME = "name"
    private const val K_URL = "url"
    private const val K_KEY = "key"

    private var loaded = false

    override fun getFilename(): String = FILE

    private fun ensureLoaded() {
        if (loaded) return
        loadIfNeeded()
        loaded = true
    }

    fun url(): String? = currentEntry()?.optString(K_URL, "")?.takeIf { it.isNotBlank() }
    fun key(): String? = currentEntry()?.optString(K_KEY, "")?.takeIf { it.isNotBlank() }
    fun hasKey(): Boolean = key() != null

    private fun currentEntry(): JSONObject? {
        val id = TosCampaignId.get(create = false) ?: return null
        ensureLoaded()
        return json.optJSONObject(id)
    }

    /**
     * Updates the api key
     */
    fun update(url: String, key: String) {
        val id = TosCampaignId.get(create = true) ?: return
        ensureLoaded()
        try {
            val entry = json.optJSONObject(id) ?: JSONObject()
            entry.put(K_NAME, Global.getSector()?.playerPerson?.name?.fullName ?: "")
            entry.put(K_URL, url)
            entry.put(K_KEY, key)
            json.put(K_VERSION, VERSION)
            json.put(id, entry)
            saveIfNeeded()
        } catch (e: Exception) {
            TosInspector.error("Failed to write credentials for '$id'", this::class, e)
        }
    }
}
