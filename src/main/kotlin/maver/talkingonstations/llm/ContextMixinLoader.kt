package maver.talkingonstations.llm

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import maver.talkingonstations.llm.enum.Section
import org.json.JSONObject

class ContextMixinLoader : TosClassLoader<ContextMixinInterface>(
    csvFile = "ContextMixin.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: ContextMixinInterface, row: JSONObject) {
        instance.enabled = row.getString("enabled").toBoolean()
        instance.section = Section.valueOf(row.getString("section"))
        TosInspector.info("Created mixin ${instance.getKey()}", this::class)
    }

    override fun getName(instance: ContextMixinInterface): String =
        instance::class.java.simpleName
}