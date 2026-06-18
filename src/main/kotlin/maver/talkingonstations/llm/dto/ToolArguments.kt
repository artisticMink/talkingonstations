package maver.talkingonstations.llm.dto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The arguments of a single tool call
 */
class ToolArguments private constructor(
    private val values: Map<String, String>,
) {
    fun string(name: String): String = values[name] ?: throw ToolArgumentException("'$name' is missing")
    fun optString(name: String): String? = values[name]
    fun int(name: String): Int = string(name).toInt()
    fun boolean(name: String): Boolean = string(name).toBooleanStrict()

    companion object {
        //
        fun parse(raw: String, declared: List<ToolParameter>): ToolArguments {
            val root: JsonObject = when {
                raw.isBlank() || raw == "null" -> JsonObject(emptyMap())
                else -> try {
                    Json.parseToJsonElement(raw) as? JsonObject
                        ?: throw ToolArgumentException("expected a JSON object")
                } catch (exception: ToolArgumentException) {
                    throw exception
                } catch (exception: Exception) {
                    throw ToolArgumentException("arguments are not valid JSON")
                }
            }

            val values = root.mapValues { (_, value) ->
                if (value is JsonPrimitive) value.content else value.toString()
            }

            declared.forEach { param ->
                val value = values[param.name]
                if (value.isNullOrBlank()) {
                    if (param.required)
                        throw ToolArgumentException("'${param.name}' is required: ${param.description}")
                    return@forEach
                }
                val typeOk = when (param.type) {
                    ParamType.STRING -> true
                    ParamType.INTEGER -> value.toIntOrNull() != null
                    ParamType.NUMBER -> value.toDoubleOrNull() != null
                    ParamType.BOOLEAN -> value.toBooleanStrictOrNull() != null
                }
                if (!typeOk)
                    throw ToolArgumentException("'${param.name}' must be a ${param.type.jsonType}, got '$value'")
            }

            return ToolArguments(values)
        }
    }
}

/** @see [ToolArguments]. */
class ToolArgumentException(message: String) : Exception(message)
