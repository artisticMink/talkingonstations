package maver.talkingonstations.llm.dto

data class ApiSettings(
    val name: String,
    val url: String,
    private val key: String,
) {
    fun getApiKey(): String {
        return key
    }
}