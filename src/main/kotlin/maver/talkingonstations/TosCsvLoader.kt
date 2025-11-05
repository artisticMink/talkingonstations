package maver.talkingonstations

import com.fs.starfarer.api.Global
import org.json.JSONObject

abstract class TosCsvLoader(
    private val csvPath: String,
    private val modId: String = "maver_talkingonstations"
) {
    protected abstract fun isEnabled(row: JSONObject): Boolean

    protected fun loadCsvRows(): List<JSONObject> {
        val csv = Global.getSettings().loadCSV(csvPath, modId)
        return (0 until csv.length()).map { csv[it] as JSONObject }
    }
}