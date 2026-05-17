package maver.talkingonstations

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import maver.talkingonstations.ui.debug.DebugProbe
import kotlin.reflect.KClass

object TosInspector {
    fun info(message: String, classRef: KClass<*>) {
        Global.getLogger(classRef.java).info(message)
    }

    fun error(message: String?, classRef: KClass<*>, exception: Exception? = null) {
        Global.getLogger(classRef.java).error(message, exception)
    }

    fun debug(message: String, classRef: KClass<*>) {
        Global.getLogger(classRef.java).info(message)
    }

    fun addVisualProbe(panel: CustomPanelAPI, tlX: Float = 0f, tlY: Float = 0f) {
        val probe = DebugProbe(panel, w = 200f, h = 100f)
        probe.placeInTL(panel, tlX, tlY)
    }


}