package maver.talkingonstations.httpapi

import maver.talkingonstations.TosSettings

object HttpApiRegistry {
    private val apiList: List<HttpApiInterface> = HttpApiLoader().load()

    fun getConversationApi(): HttpApiInterface = getApi(TosSettings.api) ?: getDefaultApi() ?: noApiAvailable()

    fun getCombatChatterApi(): HttpApiInterface = getApi(TosSettings.modsCcApi) ?: getDefaultApi() ?: noApiAvailable()

    fun getApi(name: String): HttpApiInterface? {
        return apiList.find { it.getName() == name }
    }

    fun getDefaultApi(): HttpApiInterface? {
        return apiList.firstOrNull()
    }

    fun getApiNames(): List<String> {
        return apiList.map { it.getName() }
    }

    private fun noApiAvailable(): Nothing =
        throw IllegalStateException("No HTTP API driver available — check Api.csv loading errors in starsector.log")
}