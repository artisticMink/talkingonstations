package maver.talkingonstations.httpapi

import maver.talkingonstations.TosSettings

object HttpApiRegistry {
    private val apiList: List<HttpApiInterface> = HttpApiLoader().load()

    fun getConversationApi(): HttpApiInterface {
        val identifier = TosSettings.api

        // Fallback to default api when requested api is not available
        // ToDo: Should have user-facing feedback.
        return getApi(identifier) ?: getDefaultApi() ?: throw Exception("No API available")
    }

    fun getCombatChatterApi(): HttpApiInterface {
        val identifier = TosSettings.modsCcApi

        return getApi(identifier) ?: getDefaultApi() ?: throw Exception("No API available")
    }

    fun getApi(name: String): HttpApiInterface? {
        return apiList.find { it.getName() == name }
    }

    fun getDefaultApi(): HttpApiInterface? {
        return apiList.firstOrNull()
    }

    fun getApiNames(): List<String> {
        return apiList.map { it.getName() }
    }
}