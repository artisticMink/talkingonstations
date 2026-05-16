package maver.talkingonstations.characters.market

/**
 * Interface for unique characters connected to a market
 *
 * Optional, can be used for detailed context injection
 */
interface MarketPersonInterface {
    /**
     * Must return a id for this MarketPerson
     */
    var id: String

    /**
     * Can return result of a conditions check
     *
     * When false, character won't be added to the market
     */
    fun canUse(): Boolean = true

    /**
     * Can return additional context for [maver.talkingonstations.llm.mixins.SelfKnowledge]
     *
     * Appended after character instructions.
     */
    fun getInstructions(): String

    /**
     * Can return additional context for [maver.talkingonstations.llm.mixins.SelfKnowledge]
     *
     * Appended after character background.
     */
    fun getBackground(): String
}