package maver.talkingonstations

import com.fs.starfarer.api.Global
import org.json.JSONObject

/**
 * Base loader for CSV data files, merged via Starsector's
 * [getMergedSpreadsheetDataForMod] to support mod load-order overrides.
 *
 * Expects files to be in [TosStrings.Loader.PATH]
 */
abstract class TosCsvLoader(
    private val csvFile: String,
    private val idColumn: String = TosStrings.Loader.CSV_ID_COLUMN
) {

    /** Determines whether a given CSV row should be included when loading. */
    protected abstract fun isEnabled(row: JSONObject): Boolean

    /**
     * Loads and returns all rows from [csvFile], merged across mods.
     *
     * @return Array of JSONObjects representing the rows
     */
    protected fun loadCsvRows(): List<JSONObject> {
        val data = Global.getSettings().getMergedSpreadsheetDataForMod(idColumn, TosStrings.Loader.PATH + csvFile, TosStrings.ModConfig.ID)

        return (0 until data.length())
            .map { data[it] as JSONObject }
            .filter { isEnabled(it) }
    }
}