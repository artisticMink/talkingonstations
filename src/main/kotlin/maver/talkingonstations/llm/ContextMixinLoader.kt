package maver.talkingonstations.llm

import maver.talkingonstations.TosClassLoader
import org.json.JSONObject

class ContextMixinLoader : TosClassLoader<ContextMixinInterface>(
    csvPath = "ContextMixin.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: ContextMixinInterface, row: JSONObject) {
        instance.enabled = row.getString("enabled").toBoolean()
    }

    override fun getName(instance: ContextMixinInterface): String =
        instance::class.java.simpleName
}