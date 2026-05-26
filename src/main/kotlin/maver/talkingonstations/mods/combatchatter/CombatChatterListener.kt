package maver.talkingonstations.mods.combatchatter

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.Description.Type
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.Misc
import kotlinx.coroutines.launch
import maver.talkingonstations.TosBackgroundScope
import maver.talkingonstations.TosInspector
import maver.talkingonstations.TosSettings
import maver.talkingonstations.chat.ChatRoles
import maver.talkingonstations.httpapi.HttpApiRegistry
import maver.talkingonstations.llm.LLMService
import maver.talkingonstations.llm.dto.ApiSettings
import maver.talkingonstations.llm.dto.GameInfo
import maver.talkingonstations.llm.dto.Message
import maver.talkingonstations.llm.dto.ModelSettings
import maver.talkingonstations.llm.markdown
import org.histidine.chatter.ChatterDataManager
import org.histidine.chatter.ChatterLine
import org.histidine.chatter.ChatterMessage
import org.histidine.chatter.combat.ChatterCombatPlugin
import org.histidine.chatter.utils.ChatterListener
import java.awt.Color
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.TimeUnit

/**
 * Hooks into [org.histidine.chatter.combat.ChatterCombatPlugin]
 *
 * Takes a combat chatter message once per [rewriteInterval] and
 * forwards it to the model for a rewrite along with situational
 * information.
 */
class CombatChatterListener(
    private val plugin: CombatChatterPlugin
) : ChatterListener {
    val rewriteInterval = TimeUnit.SECONDS.toNanos(TosSettings.modsCcRequestInterval.toLong())
    var rewriteInProgress = false

    private val approvedLines: MutableSet<ChatterLine> =
        Collections.newSetFromMap(IdentityHashMap<ChatterLine, Boolean>())
    private val combatEngine: CombatEngineAPI = Global.getCombatEngine()

    var lastRewrite = 0L

    private val llmService: LLMService
    private val ccContext = CombatChatterContext(GameInfo(), mutableListOf<Message>(), null)
    private val modelSettings: ModelSettings = ModelSettings(
        id = TosSettings.modsCcApiModel,
        reasoningEffort = TosSettings.modsCcReasoningEffort
    )

    init {
        val api = HttpApiRegistry.getCombatChatterApi()
        val apiSettings = ApiSettings(api.getName(), TosSettings.modsCcApiUrl, TosSettings.modsCcApiKey)
        llmService = LLMService(api, apiSettings)
    }

    override fun preShowChatMessage(
        member: FleetMemberAPI,
        line: ChatterLine?,
        text: String,
        floaty: Boolean,
        inMessageBox: Boolean,
        textColor: Color?,
    ): Boolean {
        if (!TosSettings.modsCcEnabled || line == null) return true
        if (!TosSettings.modsCcWithFloaters && floaty) return true

        // Skip generated
        if (approvedLines.remove(line)) return true

        // Prevent race condition on context.messages
        if (TosSettings.modsCcPersistenceEnabled && rewriteInProgress) return true

        // Only every so often
        val now = System.nanoTime()
        if (now - lastRewrite < rewriteInterval) return true
        lastRewrite = now

        val myFaction = Misc.getCommissionFaction() ?: Global.getSector().playerFaction
        val myShip: ShipAPI? = combatEngine.ships.firstOrNull() { it.fleetMember === member }

        var enemyFactionId: String? = null
        val firstEnemy = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).deployedCopy.firstOrNull()
        if (firstEnemy != null) {
            enemyFactionId = ChatterDataManager.getFactionFromShip(firstEnemy)
        }

        // No way to access the enemy ship directly.
        val theirShip: ShipAPI? = myShip?.shipTarget
            ?: myShip?.aiFlags?.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI
            ?: myShip?.aiFlags?.getCustom(ShipwideAIFlags.AIFlags.BIGGEST_THREAT) as? ShipAPI

        val prompt = markdown {
            if (TosSettings.modsCcOnlyRewrite) {
                h1("Line to rewrite")
                l(text)
            }

            h2("Your Faction - ${myFaction.displayName}")
            if (TosSettings.modsCcWithDescription) {
                p(Global.getSettings().getDescription(myFaction.id, Type.FACTION).text1)
            }

            if (enemyFactionId != null) {
                h2("Enemy Faction - ${enemyFactionName(enemyFactionId)}")
                if (TosSettings.modsCcWithDescription) {
                    p(Global.getSettings().getDescription(enemyFactionId, Type.FACTION).text1)
                }
            }

            if (myShip != null) {
                h2("Your Ship - ${myShip.name}")
                if (myShip.fleetMember != null) +getShipStatus(myShip)
            }

            if (theirShip != null) {
                h2("Targeted Enemy Ship - ${theirShip.name}")
                if (theirShip.fleetMember != null) +getShipStatus(theirShip)
            }
        }

        rewriteInProgress = true
        TosBackgroundScope.scope.launch {
            val rewritten: String? = try {
                ccContext.messages.add(
                    Message(
                        role = ChatRoles.USER,
                        content = prompt,
                    )
                )

                val responseMessages = llmService.send(
                    context = ccContext,
                    model = modelSettings,
                    tools = emptyList()
                )

                responseMessages.lastOrNull().takeIf { it?.role == ChatRoles.ASSISTANT }?.content
            } catch (t: Throwable) {
                TosInspector.error(
                    "Chatter rewrite failed",
                    this@CombatChatterListener::class,
                    t as? Exception,
                )
                null
            }

            if (!TosSettings.modsCcPersistenceEnabled) ccContext.messages.clear()
            else if (rewritten?.isNotBlank() == true) {
                ccContext.messages.removeLast()
                ccContext.messages.add(
                    Message(
                        ChatRoles.ASSISTANT,
                        rewritten
                    )
                )
            } else ccContext.messages.removeLast()

            val finalText = (rewritten?.takeIf { it.isNotBlank() } ?: text).trim { it == '"' }
            plugin.postToCombatThread {
                rewriteInProgress = false
                enqueueMessage(member, finalText, textColor, floaty)
            }
        }

        // Finally refuse the canned line
        return false
    }

    /**
     * Interface requires passtrough
     */
    override fun shownChatMessage(
        member: FleetMemberAPI,
        line: ChatterLine?,
        text: String,
        floaty: Boolean,
        inMessageBox: Boolean,
        textColor: Color?,
    ) = Unit

    /**
     * Queues the text for display as soon as possible
     */
    private fun enqueueMessage(
        member: FleetMemberAPI,
        text: String,
        textColor: Color?,
        isFloater: Boolean = false,
        ignoreDelay: Boolean = false
    ) {
        val chatter = ChatterCombatPlugin.getInstance() ?: return

        val newLine = ChatterLine(text)
        approvedLines.add(newLine)

        val message = ChatterMessage(member, newLine, ChatterLine.MessageType.REPLY).apply {
            color = textColor ?: Color.WHITE
            floater = isFloater
            force = ignoreDelay
        }

        chatter.queueMessage(message, 0f)
    }

    private fun getShipStatus(ship: ShipAPI): String {
        val member = ship.fleetMember
        return markdown {
            h3("Captain Personality")
            l(member.captain.personalityAPI.description)
            h3("Class")
            l(member.variant.fullDesignationWithHullNameForShip)
            h3("Category")
            l(getCategory(member))
            h3("Hull Modifications")
            l(getHullmods(member, TosSettings.modsCcWithDescription))
            h3("Weapons")
            l(getWeapons(member, TosSettings.modsCcWithDescription))
            val wings = getWings(member)
            if (wings.isNotEmpty()) {
                h3("Strike-Craft")
                l(wings)
            }
            h3("Combat Readiness")
            l("${member.repairTracker.cr * 100}%")

            val activeAiFlags = ShipwideAIFlags.AIFlags.entries
                .filter { ship.aiFlags?.hasFlag(it) == true }
                .joinToString(",") { it.name }

            h3("AI Behavior")
            l(activeAiFlags)
        }
    }

    private fun enemyFactionName(factionId: String): String {

        return Global.getSector()?.getFaction(factionId)?.displayName ?: "unknown forces"
    }

    private fun getWings(member: FleetMemberAPI): String {
        return member.variant.wings
            .distinct()
            .joinToString("\n") { wingId ->
                val spec = Global.getSettings().getFighterWingSpec(wingId)
                markdown {
                    h4(spec?.wingName ?: wingId)
                    l(spec?.role?.toString().orEmpty())
                }
            }
    }

    private fun getCategory(member: FleetMemberAPI): String {
        val category = if (member.isCapital) "Capital Ship"
        else if (member.isCarrier) "Carrier"
        else if (member.isCruiser) "Cruiser"
        else if (member.isDestroyer) "Destroyer"
        else if (member.isFrigate) "Frigate"
        else if (member.isFighterWing) "Fighter Wing"
        else "Spaceship"

        val prefix = if (member.isCivilian) "Civilian " else ""

        return "$prefix$category"
    }

    private fun getWeapons(member: FleetMemberAPI, exhaustive: Boolean = false): String {
        return member.variant.fittedWeaponSlots
            .map { member.variant.getWeaponSpec(it) }
            .distinct()
            .joinToString("\n") { spec ->
                if (exhaustive)
                    markdown {
                        h4(spec.weaponName)
                        l("Size: ${spec.size.displayName}")
                        l("Type: ${spec.type.displayName}")
                        l("Damage: ${spec.damageType.displayName}")
                        l(Global.getSettings().getDescription(spec.weaponId, Type.WEAPON)?.text1.orEmpty())
                    }
                else
                    markdown {
                        l("- ${spec.weaponName}")
                    }
            }
    }

    private fun getHullmods(member: FleetMemberAPI, exhaustive: Boolean = false): String {
        return member.variant.hullMods.joinToString("\n") { id ->
            val spec = Global.getSettings().getHullModSpec(id)
            if (exhaustive)
                markdown {
                    h4(spec.displayName)
                    l(spec.getDescription(member.hullSpec.hullSize))
                }
            else
                markdown {
                    l("- ${spec.displayName}")
                }
        }
    }
}
