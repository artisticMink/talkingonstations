package maver.talkingonstations

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.EveryFrameScript
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Queue for ui updates
 */
object TosEveryFrameScriptQueue {
    private val queue = ConcurrentLinkedQueue<() -> Unit>()

    fun add(block: () -> Unit) = queue.add(block)

    fun setup() = Global.getSector().addTransientScript(object : EveryFrameScript {
        override fun isDone() = false
        override fun runWhilePaused() = true
        override fun advance(amount: Float) {
            while (true) {
                val block = queue.poll() ?: break
                try { block() } catch (e: Exception) { TosInspector.error("Queued block failed", this::class, e) }
            }
        }

    })

}
