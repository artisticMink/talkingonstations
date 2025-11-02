package maver.talkingonstations.characters

import maver.talkingonstations.TosClassLoader
import org.json.JSONObject

class PersonTypeLoader : TosClassLoader<PersonTypeInterface>(
    csvPath = "data/config/PersonTypes.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: PersonTypeInterface, row: JSONObject) {
        instance.instructions = row.getString("instructions")
        instance.enabled = row.getString("enabled").toBoolean()
    }

    override fun getName(instance: PersonTypeInterface): String =
        instance::class.java.simpleName
}