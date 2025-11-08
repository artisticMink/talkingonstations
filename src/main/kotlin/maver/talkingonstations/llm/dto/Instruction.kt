package maver.talkingonstations.llm.dto

data class Instruction(val key: String, var value: String) {
    override fun toString(): String {
        return value
    }
}