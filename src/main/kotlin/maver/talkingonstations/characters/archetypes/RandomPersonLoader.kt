package maver.talkingonstations.characters.archetypes

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import org.json.JSONObject

class RandomPersonLoader : TosClassLoader<CharacterArchetypeInterface>(
    csvFile = "Archetypes.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: CharacterArchetypeInterface, row: JSONObject) {
        instance.enabled = row.getString("enabled").toBoolean()
        TosInspector.info("Created archetype ${instance.getKey()}", this::class)
    }

    override fun getName(instance: CharacterArchetypeInterface): String =
        instance::class.java.simpleName
}