package maver.talkingonstations.httpapi.body

import kotlinx.serialization.Serializable
import maver.talkingonstations.llm.ToolInterface

@Serializable
data class ToolCallDefinition(
    val type: String = "function",
    val function: FunctionDeclaration,
) {
    companion object {
        fun fromTools(tools: List<ToolInterface>): List<ToolCallDefinition> =
            tools.map(::fromTool)

        fun fromTool(tool: ToolInterface): ToolCallDefinition {
            val params = tool.parameters.parameters
            return ToolCallDefinition(
                function = FunctionDeclaration(
                    name = tool.getName(),
                    description = tool.description,
                    parameters = FunctionParameters(
                        properties = params.mapValues { (_, description) ->
                            SchemaProperty(type = "string", description = description)
                        },
                        required = params.keys.toList(),
                    ),
                ),
            )
        }
    }
}

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: FunctionParameters,
)

@Serializable
data class FunctionParameters(
    val type: String = "object",
    val properties: Map<String, SchemaProperty>,
    val required: List<String> = emptyList(),
)

@Serializable
data class SchemaProperty(
    val type: String,
    val description: String? = null,
    val items: SchemaItems? = null,
)

@Serializable
data class SchemaItems(
    val type: String,
)