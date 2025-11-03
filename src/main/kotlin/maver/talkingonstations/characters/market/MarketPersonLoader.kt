package maver.talkingonstations.characters

import maver.talkingonstations.TosClassLoader
import org.json.JSONObject

class MarketPersonLoader : TosClassLoader<MarketPersonInterface>(
    csvPath = "data/config/MarketPerson.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: MarketPersonInterface, row: JSONObject) {
        instance.instructions = row.getString("instructions")
        instance.enabled = row.getString("enabled").toBoolean()
    }

    override fun getName(instance: MarketPersonInterface): String =
        instance::class.java.simpleName
}