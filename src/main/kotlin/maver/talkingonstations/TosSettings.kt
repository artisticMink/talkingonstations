package maver.talkingonstations

import maver.talkingonstations.llm.ContextProviderInterface
import maver.talkingonstations.llm.ContextProviderLoader

object TosSettings {
    private var contextProvider: List<ContextProviderInterface> = ContextProviderLoader().load()

    fun getContextProvider() = contextProvider
    fun reloadContextProvider() { contextProvider = ContextProviderLoader().load() }

    fun enableContextProvider(key: String){
        contextProvider.find { it.getKey() == key }?.enabled = true
    }

    fun disableContextProvider(key: String) {
        contextProvider.find { it.getKey() == key }?.enabled = false
    }
}