package maver.talkingonstations.mods.combatchatter

import com.fs.starfarer.api.combat.ShipwideAIFlags

object CcUtils {
    val aiFlagDescriptions: Map<ShipwideAIFlags.AIFlags, List<String>> = mapOf(
        ShipwideAIFlags.AIFlags.HARASS_MOVE_IN to listOf(
            "harassing the enemy at close range",
            "darting in to pressure a contact",
            "running a harass pass",
        ),
        ShipwideAIFlags.AIFlags.AVOIDING_BORDER to listOf(
            "edging away from the map border",
            "keeping clear of the engagement edge",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_AVOID_BORDER to listOf(
            "ignoring the engagement edge",
            "willing to press against the border",
        ),
        ShipwideAIFlags.AIFlags.CAMP_LOCATION to listOf(
            "holding position",
            "anchored, refusing to move off station",
        ),
        ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP to listOf(
            "shadowing a friendly to escort it",
            "flying escort",
        ),
        ShipwideAIFlags.AIFlags.MANEUVER_TARGET to listOf(
            "working an angle on a contact",
            "jockeying for position",
            "actively maneuvering against a target",
        ),
        ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET to listOf(
            "vectoring fighters onto a target",
            "directing strike-craft at a contact",
        ),
        ShipwideAIFlags.AIFlags.MAINTAINING_STRIKE_RANGE to listOf(
            "holding strike-craft range",
            "staying at strike-launch distance",
        ),
        ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP to listOf(
            "operating as a drone mothership",
            "fielding its drone screen",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS to listOf(
            "shields down, tanking on armor",
            "armor-tanking, shields off",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_USE_FLUX to listOf(
            "conserving flux",
            "easing off the guns to manage flux",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_VENT to listOf(
            "refusing to vent, holding the line",
            "keeping flux capped rather than venting",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS to listOf(
            "holding non-essential fire",
            "weapons disciplined, only essentials firing",
        ),
        ShipwideAIFlags.AIFlags.RUN_QUICKLY to listOf(
            "burning hard, full retreat",
            "sprinting away under burn",
        ),
        ShipwideAIFlags.AIFlags.TURN_QUICKLY to listOf(
            "snapping into a hard turn",
            "throwing the helm over",
        ),
        ShipwideAIFlags.AIFlags.PURSUING to listOf(
            "in active pursuit",
            "running down a contact",
            "closing on its prey",
        ),
        ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE to listOf(
            "taking incoming fire",
            "under heavy fire right now",
            "rounds inbound",
        ),
        ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON to listOf(
            "shields locked up, refusing to drop them",
            "holding shield no matter what",
        ),
        ShipwideAIFlags.AIFlags.STAY_PHASED to listOf(
            "holding the phase cloak",
            "remaining phased",
        ),
        ShipwideAIFlags.AIFlags.PHASE_BRAWLER_DUMPING_FLUX to listOf(
            "phased, dumping flux",
            "shedding flux from cloak",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF to listOf(
            "committed, no fallback",
            "fighting forward, refusing to retreat",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF_EVEN_WHILE_VENTING to listOf(
            "venting in place, no withdrawal",
            "holding ground even mid-vent",
        ),
        ShipwideAIFlags.AIFlags.BACK_OFF to listOf(
            "pulling back to recover",
            "disengaging to vent",
            "easing off the line",
        ),
        ShipwideAIFlags.AIFlags.BACKING_OFF to listOf(
            "actively backing away from contact",
            "rolling off the engagement",
        ),
        ShipwideAIFlags.AIFlags.STANDING_OFF_VS_SHIP_ON_MAP_BORDER to listOf(
            "standing off a ship pinned at the border",
            "containing a contact stuck on the edge",
        ),
        ShipwideAIFlags.AIFlags.SAFE_VENT to listOf(
            "venting in clear space",
            "safe enough to vent",
        ),
        ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET to listOf(
            "managing range against a target",
            "working the engagement range",
        ),
        ShipwideAIFlags.AIFlags.PREFER_LEFT_BROADSIDE to listOf(
            "presenting port broadside",
            "turning to bring port guns to bear",
        ),
        ShipwideAIFlags.AIFlags.PREFER_RIGHT_BROADSIDE to listOf(
            "presenting starboard broadside",
            "turning to bring starboard guns to bear",
        ),
        ShipwideAIFlags.AIFlags.DELAY_STRIKE_FIRE to listOf(
            "holding strike weapons for a better shot",
            "delaying torpedo launch",
        ),
        ShipwideAIFlags.AIFlags.PHASE_ATTACK_RUN to listOf(
            "phasing in for an attack run",
            "lining up a phase strike",
        ),
        ShipwideAIFlags.AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT to listOf(
            "phase-perfect, ready to uncloak and strike",
            "in position for a phase strike",
        ),
        ShipwideAIFlags.AIFlags.DO_NOT_PURSUE to listOf(
            "refusing to chase",
            "holding range instead of pursuing",
        ),
        ShipwideAIFlags.AIFlags.TIMID_ESCORT to listOf(
            "flying a cautious escort pattern",
            "hanging back as a wary escort",
        ),
        ShipwideAIFlags.AIFlags.IGNORES_ORDERS to listOf(
            "ignoring fleet orders",
            "off-script, doing its own thing",
        ),
        ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER to listOf(
            "in critical danger, taking heavy DPS",
            "barely holding under sustained fire",
        ),
        ShipwideAIFlags.AIFlags.IN_ATTACK_RUN to listOf(
            "on an attack run",
            "diving in for a strike",
        ),
        ShipwideAIFlags.AIFlags.POST_ATTACK_RUN to listOf(
            "pulling off after a strike",
            "recovering from an attack run",
        ),
        ShipwideAIFlags.AIFlags.WING_NEAR_ENEMY to listOf(
            "wing engaged at close quarters",
            "fighters tangled with the enemy",
        ),
        ShipwideAIFlags.AIFlags.WING_SHOULD_GET_SOME_DISTANCE to listOf(
            "fighters pulling back to reform",
            "wing breaking off to regroup",
        ),
        ShipwideAIFlags.AIFlags.NEEDS_HELP to listOf(
            "calling for support",
            "asking for an escort",
        ),
        ShipwideAIFlags.AIFlags.BIGGEST_THREAT to listOf(
            "marked as the priority target",
            "the most dangerous contact on the board",
            "first on the kill list",
            "the one to put down first",
        ),
        ShipwideAIFlags.AIFlags.HAS_POTENTIAL_MINE_TRIGGER_NEARBY to listOf(
            "wary of a mine trigger nearby",
            "tracking a possible mine in the area",
        ),
        ShipwideAIFlags.AIFlags.TARGET_FOR_SHIP_SYSTEM to listOf(
            "lining up a ship-system attack",
            "winding up to use a system on its target",
        ),
    )
}