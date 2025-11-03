package maver.talkingonstations

import maver.talkingonstations.characters.MarketPersonInterface
import maver.talkingonstations.characters.MarketPersonLoader
import maver.talkingonstations.characters.RandomPersonInterface
import maver.talkingonstations.characters.RandomPersonLoader
import maver.talkingonstations.llm.ContextMixinInterface
import maver.talkingonstations.llm.ContextMixinLoader

object TosSettings {
    private var contextMixins: List<ContextMixinInterface> = ContextMixinLoader().load()
    private var randomPersons: List<RandomPersonInterface> = RandomPersonLoader().load()
    private var marketPersons: List<MarketPersonInterface> = MarketPersonLoader().load()

    fun getContextMixins() = contextMixins
    fun reloadContextMixins() { contextMixins = ContextMixinLoader().load() }
    fun enableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = true
    fun disableContextMixin(key: String) = contextMixins.find { it.getKey() == key }?.enabled = false

    fun getPersonTypes() = randomPersons
    fun reloadPersonTypes() { randomPersons = RandomPersonLoader().load() }
    fun enablePersonType(key: String) = randomPersons.find { it.getKey() == key }?.enabled = true
    fun disablePersonType(key: String) = randomPersons.find { it.getKey() == key }?.enabled = false

    fun getMarketPersons() = marketPersons
}