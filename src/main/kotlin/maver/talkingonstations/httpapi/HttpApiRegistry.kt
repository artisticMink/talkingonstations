package maver.talkingonstations.httpapi

object HttpApiRegistry {
    private val apiList: List<HttpApiInterface> = listOf(OAICompatibleHttpApi.create())

    fun getApi(name: String): HttpApiInterface? {
        return apiList.find { it.getName() == name }
    }
}