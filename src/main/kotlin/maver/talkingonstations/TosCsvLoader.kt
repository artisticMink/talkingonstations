package maver.talkingonstations

import com.fs.starfarer.api.Global
import org.json.JSONObject

abstract class TosCsvLoader(
    private val csvFile: String,
    private val idColumn: String = TosStrings.Loader.CSV_ID_COLUMN
) {
    protected abstract fun isEnabled(row: JSONObject): Boolean

    protected fun loadCsvRows(): List<JSONObject> {
        val data = Global.getSettings().getMergedSpreadsheetDataForMod(idColumn, TosStrings.Loader.PATH + csvFile, TosStrings.ModConfig.ID)

        return (0 until data.length()).map { data[it] as JSONObject }
    }
}