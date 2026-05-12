package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Voices
import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosCsvLoader
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosMemoryKeys
import maver.talkingonstations.TosStrings
import maver.talkingonstations.characters.market.dto.MarketPersonData
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
     * the associated [MarketPersonData] for each.
     *
     * @throws Exception if a referenced market ID does not exist.
     */
    fun load(): Map<PersonAPI, MarketPersonData> {
        // Instantiate available person extensions first
        val personExtensions: List<MarketPersonInterface> = MarketPersonExtensionLoader().load()

        return loadCsvRows().mapNotNull { row ->
            val marketPersonData = MarketPersonData(
                id = row.getString("id"),
                gender = row.getString("gender").toEnumOrDefault(Gender.ANY),
                name = row.getString("name"),
                surname = row.getString("surname"),
                faction = row.getString("faction"),
                market = row.getString("market"),
                rank = row.getString("rank"),
                post = row.getString("post"),
                portrait = row.getString("portrait"),
                voice = row.getString("voice"),
                background = row.getString("background"),
                knowledgeBlacklist = row.getString("knowledgeBlacklist"),
                personExtension = personExtensions.find { it.id == row.getString("id") },
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

            person.memory.set(TosMemoryKeys.CHAT_ENABLED, true)
            person.memory.set(TosMemoryKeys.IS_MARKET_PERSON, true)
            person.memory.set(TosMemoryKeys.MARKET_PERSON_DATA, marketPersonData)

            existingMarket.commDirectory.addPerson(person)
            existingMarket.addPerson(person)

            TosInspector.info("Created ${person.name.fullName} at ${existingMarket.name}", this::class)

            person to marketPersonData
        }.toMap()
    }

    private fun createPerson(data: MarketPersonData): PersonAPI {
        val portraitPath = if (data.portrait.contains("/")) {
            data.portrait
        } else {
            Global.getSettings().getSpriteName("characters", data.portrait)
        }

        return Global.getFactory().createPerson().apply {
            id = "tos_${data.name}_${data.hashCode()}"
            setFaction(data.faction)
            gender = Gender.valueOf(data.gender.name)
            postId = data.post ?: Ranks.POST_CITIZEN
            rankId = data.rank ?: Ranks.CITIZEN
            name.apply {
                first = data.name
                last = data.surname
            }
            portraitSprite = portraitPath
            voice = data.voice ?: Voices.SPACER
            importance = PersonImportance.MEDIUM
        }
    }
}

