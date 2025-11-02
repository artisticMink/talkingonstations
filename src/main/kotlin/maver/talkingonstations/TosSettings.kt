package maver.talkingonstations

import maver.talkingonstations.characters.PersonTypeInterface
import maver.talkingonstations.characters.PersonTypeLoader
import maver.talkingonstations.llm.ContextProviderInterface
import maver.talkingonstations.llm.ContextProviderLoader

object TosSettings {
    private var contextProvider: List<ContextProviderInterface> = ContextProviderLoader().load()
    private var personTypes: List<PersonTypeInterface> = PersonTypeLoader().load()

    fun getContextProvider() = contextProvider
    fun reloadContextProvider() { contextProvider = ContextProviderLoader().load() }

    fun enableContextProvider(key: String){
        contextProvider.find { it.getKey() == key }?.enabled = true
    }

    fun disableContextProvider(key: String) {
        contextProvider.find { it.getKey() == key }?.enabled = false
    }

    fun getPersonTypes() = personTypes
    fun reloadPersonTypes() { personTypes = PersonTypeLoader().load() }

    fun enablePersonType(key: String){
        personTypes.find { it.getKey() == key }?.enabled = true
    }

    fun disablePersonType(key: String) {
        personTypes.find { it.getKey() == key }?.enabled = false
    }
}