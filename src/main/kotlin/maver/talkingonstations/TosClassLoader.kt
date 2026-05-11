package maver.talkingonstations

import com.fs.starfarer.api.Global
import org.json.JSONObject

/**
 * Load instances of T from a CSV file by reading a class name from each row,
 * instantiating it and optionally configuring it.
 *
 * Subclasses must specify how to extract the class name from a row and how to
 * identify an instance by name.
 */

abstract class TosClassLoader<T : Any>(
    csvFile: String
): TosCsvLoader(csvFile, TosStrings.Loader.CLASS_ID_COLUMN) {
    private val classLoader: ClassLoader = Global.getSettings().scriptClassLoader
    private val logger = Global.getLogger(javaClass)

    /**
     * Allows to modify the instance after loading and before returning.
     */
    protected open fun configureInstance(instance: T, row: JSONObject) {}

    protected abstract fun getName(instance: T): String
    protected abstract fun getClassName(row: JSONObject): String

    /**
     * Loads all valid instances from the CSV, silently skipping any rows that fail.
     */
    fun load(): List<T> {
        return loadCsvRows().mapNotNull { row -> loadInstance(row) }
    }

    /**
     * Attempt to instantiate and configure T.
     *
     * @return null on error, logs error if instantiation or casting fails.
     */
    private fun loadInstance(row: JSONObject): T? {
        val className = getClassName(row)

        return try {
            val loadedClass = classLoader.loadClass(className)
            val instance = loadedClass.newInstance() as T
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