package maver.talkingonstations.characters.market

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName.Gender
import maver.talkingonstations.TosCsvLoader
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.extensions.toEnumOrDefault
import org.json.JSONObject

class MarketPersonLoader : TosCsvLoader(
    csvPath = "data/config/MarketPerson.csv"
) {
    override fun isEnabled(row: JSONObject): Boolean {
      return row.getBoolean("enabled")
    }

    fun load(): List<MarketPerson> {
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
                tosInstructions = row.getString("tosInstructions"),
                tosLore = row.getString("tosLore"),
                tosKnowledge = row.getString("tosKnowledge"),
            )
        }.map { data -> MarketPerson.create(Global.getSector().economy.getMarket(data.market), data)}
    }
}