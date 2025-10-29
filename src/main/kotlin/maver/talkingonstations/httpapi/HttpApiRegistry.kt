package maver.talkingonstations.httpapi

import lunalib.lunaSettings.LunaSettings

object HttpApiRegistry {
    private val apiList: List<HttpApiInterface> = listOf(OAICompatibleHttpApi.create())

    fun getSelectedApi(): HttpApiInterface {
        val identifier = requireNotNull(LunaSettings.getString("maver_talkingonstations","tos_api"))

        return getApi(identifier) ?: throw Exception("No API with name $identifier")
    }

    fun getApi(name: String): HttpApiInterface? {
        return apiList.find { it.getName() == name }
    }

    fun getApiNames(): List<String> {
        return apiList.map { it.getName() }
    }
}