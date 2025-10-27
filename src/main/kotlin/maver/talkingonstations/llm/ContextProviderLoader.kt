package maver.talkingonstations.llm

import com.fs.starfarer.api.Global
import maver.talkingonstations.llm.dto.ContextProviderConfiguration
import org.json.JSONObject
import java.lang.ClassCastException

class ContextProviderLoader() {
    private val contextProviderSettingsFile = "data/config/ContextProvider.csv"
    private val classLoader: ClassLoader = Global.getSettings().scriptClassLoader

    fun load(): List<ContextProviderInterface> {
        return loadContextProviderSettings().mapNotNull { providerConfiguration -> load(providerConfiguration) }
    }

    private fun load(providerConfiguration: ContextProviderConfiguration): ContextProviderInterface? {
        try {
            val providerClass = classLoader.loadClass(providerConfiguration.fullyQualifiedClassName)
            val provider = providerClass.newInstance() as ContextProviderInterface
           // val provider = providerClass.newInstance() as ContextProviderInterface?
            provider.enabled = providerConfiguration.enabled

            return provider
        } catch (exception: ClassNotFoundException ) {
            Global.getLogger(javaClass).error("Could not load lore module. No class ${providerConfiguration.fullyQualifiedClassName}.")
        } catch (exception: ClassCastException) {
            Global.getLogger(javaClass).error("Could not cast lore module. ${exception.message}")
        }

        return null
    }

    private fun loadContextProviderSettings(): List<ContextProviderConfiguration> {
        val loreModulesCSV = Global.getSettings().loadCSV(contextProviderSettingsFile, "maver_talkingonstations")

        val modules = mutableListOf<ContextProviderConfiguration>()
        for (index in 0 until loreModulesCSV.length()) {
            val loreModule = (loreModulesCSV[index] as JSONObject)
            modules.add(
                ContextProviderConfiguration(
                    loreModule.getString("name"),
                    loreModule.getString("fullyQualifiedClassName"),
                    loreModule.getString("description"),
                    loreModule.getString("enabled").toBoolean()
                )
            )
        }

        return modules.toList()
    }
}