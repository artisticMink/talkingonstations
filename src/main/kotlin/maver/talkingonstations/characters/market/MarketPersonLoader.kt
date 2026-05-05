package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Voices
import maver.talkingonstations.TosCsvLoader
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosRegistry
import maver.talkingonstations.TosStrings
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import maver.talkingonstations.extensions.toEnumOrDefault
import org.json.JSONObject

/**
 * Loads custom person definitions from `MarketPerson.csv` and registers them
 * into their respective market comm directories.
 *
 * @see /src/main/modfiles/data/config/tos/MarketPerson.csv
 */
class MarketPersonLoader : TosCsvLoader(
    csvFile = "MarketPerson.csv"
) {
    override fun isEnabled(row: JSONObject): Boolean {
        return row.getBoolean("enabled")
    }

    /**
     * Parses all enabled rows from the CSV, creates [PersonAPI] instances,
     * registers them in their target market's comm directory, and returns
     * the associated [PersonExtensionData] for each.
     *
     * @throws Exception if a referenced market ID does not exist.
     */
    fun load(): Map<PersonAPI, PersonExtensionData> {
        return loadCsvRows().mapNotNull { row ->
            val marketPersonData = MarketPersonData(
                gender = row.getString("gender").toEnumOrDefault(Gender.ANY),
                name = row.getString("name"),
                surename = row.getString("surename"),
                faction = row.getString("faction"),
                market = row.getString("market"),
                rank = row.getString("rank"),
                post = row.getString("post"),
                portrait = row.getString("portrait"),
                voice = row.getString("voice"),
                background = row.getString("background"),
                knowledgeBlacklist = row.getString("knowledgeBlacklist"),
            )

            val existingMarket = Global.getSector().economy.getMarket(marketPersonData.market)
                .let { existingMarket ->
                    existingMarket ?: throw Exception("Market ${marketPersonData.market} not found")
                }

            val person = createPerson(marketPersonData)

            // Remove person if already exists, basically an update.
            // This will cause issues as soon as there's persistent data in person memory
            existingMarket.commDirectory.removePerson(person)
            existingMarket.removePerson(person)

            val extensionData = getExtensionData(marketPersonData)
            person.memory.set(TosStrings.MemoryId.CHAT_ENABLED, true)
            existingMarket.commDirectory.addPerson(person)
            existingMarket.addPerson(person)

            TosInspector.info("Created ${person.name.fullName} at ${existingMarket.name}", this::class)

            person to extensionData
        }.toMap()
    }

    private fun createPerson(data: MarketPersonData): PersonAPI {
        val portraitPath = Global.getSettings().getSpriteName("characters", data.portrait)
        return Global.getFactory().createPerson().apply {
            id = "tos_${data.name}_${data.hashCode()}"
            setFaction(data.faction)
            gender = Gender.valueOf(data.gender.name)
            postId = data.post ?: Ranks.POST_CITIZEN
            rankId = data.rank ?: Ranks.CITIZEN
            name.apply {
                first = data.name
                last = data.surename
            }
            portraitSprite = portraitPath
            voice = data.voice ?: Voices.SPACER
            importance = PersonImportance.MEDIUM
        }
    }

    private fun getExtensionData(data: MarketPersonData): PersonExtensionData {
        val mixins = TosRegistry.getContextMixins().filter { it.enabled }

        return PersonExtensionData(
            background = data.background ?: "",
            knowledgeBlacklist = data.knowledgeBlacklist
                ?.split(",")
                ?.mapNotNull { mixinKey ->
                    mixins.firstOrNull { mixin -> mixin.getKey() == mixinKey.trim() }
                }.orEmpty()
        )
    }
}

