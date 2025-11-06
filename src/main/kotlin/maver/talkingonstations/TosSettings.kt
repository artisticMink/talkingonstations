package maver.talkingonstations

import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.characters.market.MarketPersonLoader
import maver.talkingonstations.characters.market.dto.PersonExtensionData
import maver.talkingonstations.characters.random.RandomPersonInterface
import maver.talkingonstations.characters.random.RandomPersonLoader
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.ContextMixinLoader

object TosSettings {

    /**
     * Context mixins like npc knowledge or sector information that are to be included in the LLM context.
     */
    private lateinit var contextMixins: List<ContextMixinInterface>

    /**
     * Random person generated with a specific trait, i.e., trader, smuggler, ...
     */
    private lateinit var randomPersons: List<RandomPersonInterface>

    /**
     * Unique person tied to a specific market and listed in that markets comm directory
     */
    private lateinit var marketPersons: Map<PersonAPI, PersonExtensionData>

    fun initialize() {
        if (::contextMixins.isInitialized) {
            throw IllegalStateException("TosSettings already initialized")
        }
        contextMixins = ContextMixinLoader().load()
        randomPersons = RandomPersonLoader().load()
        marketPersons = MarketPersonLoader().load()
    }

    fun getContextMixins() = contextMixins
    fun getContextMixin(key: String): ContextMixinInterface? = contextMixins.find { it.getKey() == key }
    fun reloadContextMixins() { contextMixins = ContextMixinLoader().load() }
    fun enableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = true
    fun disableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = false

    fun getPersonTypes() = randomPersons
    fun reloadPersonTypes() { randomPersons = RandomPersonLoader().load() }
    fun enablePersonType(key: String) = randomPersons.find { it.getKey() == key }?.enabled = true
    fun disablePersonType(key: String) = randomPersons.find { it.getKey() == key }?.enabled = false

    fun getMarketPersons() = marketPersons
}