package maver.talkingonstations.characters.market

/**
 * Interface for unique characters connected to a market
 *
 * Optional, can be used for detailed context injection
 */
interface MarketPersonInterface {
    /**
     * Unique id for this MarketPerson
     */
    var id: String

    /**
     * Additional context for [maver.talkingonstations.llm.mixins.SelfKnowledge]
     *
     * Appended after character instructions.
     */
    fun getInstructions(): String

    /**
     * Additional context for [maver.talkingonstations.llm.mixins.SelfKnowledge]
     *
     * Appended after character background.
     */
    fun getBackground(): String
}