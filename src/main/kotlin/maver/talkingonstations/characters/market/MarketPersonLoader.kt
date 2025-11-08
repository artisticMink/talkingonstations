package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.characters.FullName.Gender
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Voices
import maver.talkingonstations.TosCsvLoader
import maver.talkingonstations.TosSettings
import maver.talkingonstations.TosStrings
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import maver.talkingonstations.extensions.toEnumOrDefault
import org.json.JSONObject

class MarketPersonLoader : TosCsvLoader(
    csvFile = "MarketPerson.csv"
) {
    override fun isEnabled(row: JSONObject): Boolean {
        return row.getBoolean("enabled")
    }

    fun load(): Map<PersonAPI, PersonExtensionData> {
        return loadCsvRows().map { row ->
            MarketPersonData(
                gender = row.getString("gender").toEnumOrDefault(Gender.ANY),
                name = row.getString("name"),
                surename = row.getString("surename"),
                faction = row.getString("faction"),
                market = row.getString("market"),
                rank = row.getString("rank"),
                post = row.getString("post"),
                portrait = row.getString("portrait"),
                voice = row.getString("voice"),
                instructionOverwrite = row.getString("instructionOverwrite"),
                personLore = row.getString("personLore"),
                knowledgeWhitelist = row.getString("knowledgeWhitelist"),
                knowledgeBlacklist = row.getString("knowledgeBlacklist"),
            )
        }.associate { marketPersonData ->
            val existingMarket = Global.getSector().economy.getMarket(marketPersonData.market)
                .let { existingMarket -> existingMarket ?: throw Exception("Market ${marketPersonData.market} not found") }
            val person = createPerson(marketPersonData)
            val extensionData = getExtensionData(marketPersonData)

            person.memory.set(TosStrings.MemoryId.CHAT_ENABLED,true)
            existingMarket.commDirectory.addPerson(person)
            existingMarket.addPerson(person)

            person to extensionData
        }
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
        val mixins = TosSettings.getContextMixins().filter { it.enabled }

        return PersonExtensionData(
            instructions = data.instructionOverwrite ?: "",
            lore = data.personLore ?: "",
            knowledgeWhitelist = data.knowledgeWhitelist
                ?.split(",")
                ?.mapNotNull { mixinKey ->
                    mixins.firstOrNull { mixin -> mixin.getKey() == mixinKey.trim() }
                }.orEmpty(),
            knowledgeBlacklist = data.knowledgeBlacklist
                ?.split(",")
                ?.mapNotNull { mixinKey ->
                    mixins.firstOrNull { mixin -> mixin.getKey() == mixinKey.trim() }
                }.orEmpty()
        )
    }


}

