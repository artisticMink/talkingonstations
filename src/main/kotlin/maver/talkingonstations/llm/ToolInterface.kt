package maver.talkingonstations.llm

interface ToolInterface {
    var enabled: Boolean
    var description: String
    var parameters: ToolParamInterface

    fun getName(): String

    fun execute(params: Map<String,String>): Boolean

    fun getKey(): String = this::class.java.simpleName
}
