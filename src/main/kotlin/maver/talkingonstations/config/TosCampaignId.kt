package maver.talkingonstations.config

import com.fs.starfarer.api.Global
import java.util.UUID

/**
 * Helper object that provides a stable unique campaign identifier
 */
object TosCampaignId {
    const val KEY = "tos_profileId"

    fun get(create: Boolean = false): String? {
        val sector = Global.getSector() ?: return null
        val pd = sector.persistentData
        (pd[KEY] as? String)?.let { return it }
        if (!create) return null
        val id = mint()
        pd[KEY] = id
        return id
    }

    private fun mint(): String {
        val name = Global.getSector()?.playerPerson?.name?.fullName.orEmpty()
        val sanitized = name
            .map { if (it.isLetterOrDigit()) it else '_' }
            .joinToString("")
            .trim('_')
            .ifBlank { "player" }
        val short = UUID.randomUUID().toString().replace("-", "").take(6)
        return "$sanitized-$short"
    }
}
