package maver.talkingonstations

import com.fs.starfarer.api.characters.PersonAPI
import maver.talkingonstations.characters.market.MarketPersonLoader
import maver.talkingonstations.characters.archetypes.CharacterArchetypeInterface
import maver.talkingonstations.characters.archetypes.RandomPersonLoader
import maver.talkingonstations.characters.market.dto.MarketPersonData
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.ContextMixinLoader

/**
 * Singleton providing instances of Tos-related data
 */
object TosRegistry {

    /**
     * Context mixins like npc knowledge or sector information that are to be included in the LLM context.
     */
    private lateinit var contextMixins: List<ContextMixinInterface>

    /**
     * Random person generated with a specific trait, i.e., trader, smuggler, ...
     */
    private lateinit var archetypes: List<CharacterArchetypeInterface>

    /**
     * Unique person tied to a specific market and listed in that markets comm directory
     */
    private lateinit var marketPersons: Map<String, MarketPersonData>

    fun initialize() {
        contextMixins = ContextMixinLoader().load()
        archetypes = RandomPersonLoader().load()
        marketPersons = MarketPersonLoader().load()
    }

    fun isInitialized() = ::contextMixins.isInitialized

    fun getContextMixins() = contextMixins
    fun getContextMixin(key: String): ContextMixinInterface? = contextMixins.find { it.getKey() == key }
    fun reloadContextMixins() { contextMixins = ContextMixinLoader().load() }
    fun enableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = true
    fun disableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = false

    fun getArchetypes() = archetypes
    fun reloadArchetypes() { archetypes = RandomPersonLoader().load() }
    fun enableArchetype(key: String) = archetypes.find { it.getKey() == key }?.enabled = true
    fun disableArchetype(key: String) = archetypes.find { it.getKey() == key }?.enabled = false

    fun getMarketPersons() = marketPersons
}