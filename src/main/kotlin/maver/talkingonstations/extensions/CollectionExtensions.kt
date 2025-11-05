package maver.talkingonstations.extensions

import java.util.TreeMap
import kotlin.random.Random

/**
 * Returns a random element from the list based on weights.
 *
 * @param T The type of the list elements.
 *
 * @return A weighted, random, element from the list.
 */
fun <T> List<Pair<T, Double>>.randomWeighted(): T{
    val weightMap = TreeMap<Double, T>()
    var total = 0.0

    forEach { (item, weight) ->
        total += weight
        weightMap[total] = item
    }

    val value = Random.nextDouble() * total
    return weightMap.ceilingEntry(value).value
}