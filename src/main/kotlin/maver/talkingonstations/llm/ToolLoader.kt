package maver.talkingonstations.llm

import maver.talkingonstations.TosClassLoader
import maver.talkingonstations.TosInspector
import org.json.JSONObject

/**
 * Tools are self-describing
 *
 * @see ToolInterface
 */
class ToolLoader : TosClassLoader<ToolInterface>(
    csvFile = "Tools.csv"
) {
    override fun getClassName(row: JSONObject): String =
        row.getString("fullyQualifiedClassName")

    override fun isEnabled(row: JSONObject): Boolean =
        row.getString("enabled").toBoolean()

    override fun configureInstance(instance: ToolInterface, row: JSONObject) {
        TosInspector.info("Created Tool ${instance.getKey()}", this::class)
    }

    override fun getName(instance: ToolInterface): String =
        instance::class.java.simpleName
}
