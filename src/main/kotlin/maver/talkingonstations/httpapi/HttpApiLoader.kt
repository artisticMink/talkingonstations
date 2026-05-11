package maver.talkingonstations.httpapi

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosSettings
import maver.talkingonstations.llm.dto.ApiSettings
import org.json.JSONObject

class HttpApiLoader : TosClassLoader<HttpApiInterface>(
    csvFile = "Api.csv"
) {
    private val apiSettings = ApiSettings(
        TosSettings.api,
        TosSettings.apiUrl,
        TosSettings.apiKey,
    )

    override fun isEnabled(row: JSONObject): Boolean = true

    override fun configureInstance(instance: HttpApiInterface, row: JSONObject) {
        instance.apiSettings = apiSettings
        TosInspector.info("Created HTTP API ${instance.getName()}", this::class)
    }

    override fun getName(instance: HttpApiInterface): String = instance.getName()

    override fun getClassName(row: JSONObject): String = row.getString("fullyQualifiedClassName")
}
