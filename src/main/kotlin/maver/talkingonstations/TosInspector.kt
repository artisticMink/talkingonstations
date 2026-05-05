package maver.talkingonstations

import com.fs.starfarer.api.Global
import maver.talkingonstations.characters.market.MarketPersonLoader
import java.lang.ref.WeakReference
import kotlin.collections.removeAll
import kotlin.reflect.KClass

object TosInspector {
    // We keep weak references to observable objects, stopping them to be persisted on creating a save game.
    private val instances: MutableList<WeakReference<Any>> = mutableListOf()

    fun register(instance: Any) = instances.add(WeakReference(instance))

    fun get(className: String): List<Any> {
        instances.removeAll { it.get() == null }
        return instances.mapNotNull { it.get() }.filter { it::class.simpleName == className }
    }

    fun info(message: String, classRef: KClass<*>) {
        Global.getLogger(classRef.java).info(message)
    }

    fun debug(message: String, classRef: KClass<*>) {
        Global.getLogger(classRef.java).debug(message)
    }
}