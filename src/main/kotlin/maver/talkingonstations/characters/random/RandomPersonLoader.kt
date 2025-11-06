package maver.talkingonstations.characters.random

import maver.talkingonstations.TosClassLoader
import org.json.JSONObject

class RandomPersonLoader : TosClassLoader<RandomPersonInterface>(
    csvPath = "RandomPerson.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: RandomPersonInterface, row: JSONObject) {
        instance.instructions = row.getString("instructions")
        instance.enabled = row.getString("enabled").toBoolean()
    }

    override fun getName(instance: RandomPersonInterface): String =
        instance::class.java.simpleName
}