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

        /**
         * @see [ToolInterface]
         */
        fun fromTool(tool: ToolInterface): ToolCallDefinition =
            ToolCallDefinition(
                function = FunctionDeclaration(
                    name = tool.name,
                    description = tool.description,
                    parameters = FunctionParameters(
                        properties = tool.parameters.associate { param ->
                            param.name to SchemaProperty(
                                type = param.type.jsonType,
                                description = param.description,
                            )
                        },
                        required = tool.parameters.filter { it.required }.map { it.name },
                    ),
                ),
            )
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