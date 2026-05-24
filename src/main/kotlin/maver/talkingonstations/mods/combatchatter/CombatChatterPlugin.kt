package maver.talkingonstations.mods.combatchatter

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import maver.talkingonstations.TosInspector
import org.histidine.chatter.combat.ChatterCombatPlugin
import java.util.concurrent.ConcurrentLinkedQueue

class CombatChatterPlugin : BaseEveryFrameCombatPlugin() {
    private val combatThreadTasks = ConcurrentLinkedQueue<() -> Unit>()
    private var registrationSettled = false

    fun postToCombatThread(task: () -> Unit) {
        combatThreadTasks.add(task)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (!registrationSettled) tryRegisterListener()

        while (true) {
            val task = combatThreadTasks.poll() ?: break
            try {
                task()
            } catch (t: Throwable) {
                TosInspector.error(
                    "Combat-thread chatter task failed",
                    this::class,
                    t as? Exception,
                )
            }
        }
    }

    private fun tryRegisterListener() {
        if (!Global.getSettings().modManager.isModEnabled("chatter")) {
            registrationSettled = true
            return
        }

        ChatterCombatPlugin.getInstance() ?: return

        ChatterCombatPlugin.addListener(CombatChatterListener(this))
        registrationSettled = true

        TosInspector.info("CombatChatter listener attached", this::class)
    }
}
