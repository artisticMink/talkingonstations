package maver.talkingonstations.httpapi

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import org.json.JSONObject

class HttpApiLoader : TosClassLoader<HttpApiInterface>(
    csvFile = "Api.csv"
) {
    override fun isEnabled(row: JSONObject): Boolean = true

    override fun configureInstance(instance: HttpApiInterface, row: JSONObject) {
        instance.supportsToolCalling = row.optBoolean("supportsToolCalling")
        TosInspector.info("Created HTTP API ${instance.getName()}", this::class)
    }

    override fun getName(instance: HttpApiInterface): String = instance.getName()

    override fun getClassName(row: JSONObject): String = row.getString("fullyQualifiedClassName")
}
