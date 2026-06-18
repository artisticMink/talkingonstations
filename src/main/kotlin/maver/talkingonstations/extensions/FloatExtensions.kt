package maver.talkingonstations.extensions

import com.fs.starfarer.api.util.Misc

fun ClosedFloatingPointRange<Float>.random(r: java.util.Random = Misc.random): Float =
    start + r.nextFloat() * (endInclusive - start)

fun Float.remap(inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float =
    (this - inMin) / (inMax - inMin) * (outMax - outMin) + outMin
