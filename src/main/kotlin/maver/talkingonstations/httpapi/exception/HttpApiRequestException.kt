package maver.talkingonstations.httpapi.exception

class HttpApiRequestException(
    message: String,
    val statusCode: Int? = null,
    val responseBody: String? = null,
    val requestBody: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
