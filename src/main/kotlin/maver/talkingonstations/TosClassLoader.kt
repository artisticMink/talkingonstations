package maver.talkingonstations

import com.fs.starfarer.api.Global
import org.json.JSONObject

abstract class TosClassLoader<T : Any>(
    private val csvPath: String,
    private val modId: String = "maver_talkingonstations"
) {
    private val classLoader: ClassLoader = Global.getSettings().scriptClassLoader
    private val logger = Global.getLogger(javaClass)

    fun load(): List<T> {
        return loadCsvRows().mapNotNull { row -> loadInstance(row) }
    }

    protected abstract fun getClassName(row: JSONObject): String
    protected abstract fun isEnabled(row: JSONObject): Boolean
    protected open fun configureInstance(instance: T, row: JSONObject) {}
    protected abstract fun getName(instance: T): String

    private fun loadCsvRows(): List<JSONObject> {
        val csv = Global.getSettings().loadCSV(csvPath, modId)
        return (0 until csv.length()).map { csv[it] as JSONObject }
    }

    private fun loadInstance(row: JSONObject): T? {
        val className = getClassName(row)

        return try {
            val clazz = classLoader.loadClass(className)
            val instance = clazz.newInstance() as T
            configureInstance(instance, row)
            instance
        } catch (e: ClassNotFoundException) {
            logger.error("Could not load class: $className")
            null
        } catch (e: ClassCastException) {
            logger.error("Could not cast class $className to expected type: ${e.message}")
            null
        } catch (e: Exception) {
            logger.error("Error loading $className: ${e.message}")
            null
        }
    }
}