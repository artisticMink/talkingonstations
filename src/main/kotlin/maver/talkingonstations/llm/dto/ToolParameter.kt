package maver.talkingonstations.llm.dto

data class ToolParameter(
    val name: String,
    val type: ParamType,
    val description: String,
    val required: Boolean = true,
)

enum class ParamType(val jsonType: String) {
    STRING("string"),
    INTEGER("integer"),
    NUMBER("number"),
    BOOLEAN("boolean"),
}
