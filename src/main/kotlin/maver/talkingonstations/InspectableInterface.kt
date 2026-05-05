package maver.talkingonstations

/**
 * An object must implement this interface and be registered via [TosInspector] to be
 * inspectable with the inspect command.
 */
interface InspectableInterface {
    /**
     * Must return a list of object properties that can be inspected.
     */
    fun canInspect(): List<String>

    /**
     * Must return the formatted value of the given property.
     */
    fun inspect(item: String): String
}