package maver.talkingonstations.characters.market

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import org.json.JSONObject

class MarketPersonExtensionLoader : TosClassLoader<MarketPersonInterface>(
    csvFile = "MarketPerson.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean = true

    override fun configureInstance(instance: MarketPersonInterface, row: JSONObject) {
        instance.id = row.getString("id")
        TosInspector.info("Created person extension ${row.getString("name")} ${row.getString("surname")}", this::class)
    }

    override fun getName(instance: MarketPersonInterface): String =
        instance::class.java.simpleName
}