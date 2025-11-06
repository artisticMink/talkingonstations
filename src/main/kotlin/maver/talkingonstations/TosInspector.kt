package maver.talkingonstations

import java.lang.ref.WeakReference
import kotlin.collections.removeAll

object TosInspector {
    private val instances: MutableList<WeakReference<Any>> = mutableListOf()

    fun register(instance: Any) = instances.add(WeakReference(instance))

    fun get(className: String): List<Any> {
        instances.removeAll { it.get() == null }
        return instances.mapNotNull { it.get() }.filter { it::class.simpleName == className }
    }
}