package maver.talkingonstations.command

interface InspectableInterface {
    fun canInspect(): List<String>
    fun inspect(item: String): String
}