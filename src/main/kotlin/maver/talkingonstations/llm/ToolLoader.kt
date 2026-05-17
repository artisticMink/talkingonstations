package maver.talkingonstations.llm

import kotlinx.serialization.json.Json
import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import maver.talkingonstations.llm.dto.ToolParameters
import org.json.JSONObject

class ToolLoader : TosClassLoader<ToolInterface>(
    csvFile = "Tools.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: ToolInterface, row: JSONObject) {
        instance.enabled = row.getString("enabled").toBoolean()
        instance.description = row.getString("description")

        val parameters = row.getString("parameters")
        if (parameters.isNotEmpty()) {
            val parameterString = "{\"parameters\":${row.getString("parameters")}}"
            try {
                val toolParameters = Json.decodeFromString<ToolParameters>(parameterString)
                instance.parameters = toolParameters
            } catch (exception: IllegalArgumentException) {
                TosInspector.info(
                    "Could not decode parameters for Tool ${instance.getName()}. Error: ${exception.message}",
                    this::class
                )
            }
        }

        TosInspector.info("Created Tool ${instance.getKey()}", this::class)
    }

    override fun getName(instance: ToolInterface): String =
        instance::class.java.simpleName
}