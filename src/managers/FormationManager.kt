package org.xodium.vanillaplus.managers

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.xodium.vanillaplus.data.FormationMemberData
import org.xodium.vanillaplus.managers.FormationManager.attack
import org.xodium.vanillaplus.managers.FormationManager.idle
import org.xodium.vanillaplus.modules.HordeModule
import kotlin.math.cos
import kotlin.math.sin

/** Manages formation behavior for a horde, driving idle and attack states each tick. */
internal object FormationManager {
    /**
     * Evaluates the formation state for one tick.
     * Finds the nearest eligible player; if one is within [HordeModule.Config.detectionRange],
     * delegates to [attack], otherwise delegates to [idle].
     * @param warlord The commanding [Zombie] leading the formation.
     * @param formation The list of [FormationMemberData] belonging to this formation.
     */
    fun tick(
        warlord: Zombie,
        formation: MutableList<FormationMemberData>,
    ) {
        formation.removeAll { it.mob.isDead || !it.mob.isValid }

        val target = nearestTarget(warlord)

        if (target != null) attack(warlord, formation, target) else idle(warlord, formation)
    }

    /**
     * Drives the attack state: the [warlord] marches toward [target] and all living
     * [formation] members hold their march offsets relative to the warlord.
     * @param warlord The commanding [Zombie] leading the formation.
     * @param formation The list of [FormationMemberData] belonging to this formation.
     * @param target The [Player] the formation is marching toward.
     */
    fun attack(
        warlord: Zombie,
        formation: MutableList<FormationMemberData>,
        target: Player,
    ) {
        warlord.pathfinder.moveTo(target, 1.0)
        formation.forEach { (mob, marchOffset, _) ->
            if (!mob.isDead && mob.isValid) mob.pathfinder.moveTo(warlord.location.clone().add(marchOffset), 1.0)
        }
    }

    /**
     * Drives the idle state: the [warlord] stops pathfinding and each living [formation]
     * member returns to its idle circle slot if it has strayed beyond [HordeModule.Config.roamRadius].
     * @param warlord The commanding [Zombie] leading the formation.
     * @param formation The list of [FormationMemberData] belonging to this formation.
     */
    fun idle(
        warlord: Zombie,
        formation: MutableList<FormationMemberData>,
    ) {
        val circleRadius = HordeModule.Config.idleCircleRadius
        val roamRadiusSq = HordeModule.Config.roamRadius.let { it * it }

        warlord.pathfinder.stopPathfinding()
        formation.forEach { (mob, _, idleAngle) ->
            if (!mob.isDead && mob.isValid) {
                val idleHome =
                    warlord.location.clone().add(cos(idleAngle) * circleRadius, 0.0, sin(idleAngle) * circleRadius)

                if (mob.location.distanceSquared(idleHome) > roamRadiusSq) mob.pathfinder.moveTo(idleHome, 1.0)
            }
        }
    }

    /**
     * Finds the nearest survival/adventure [Player] to the [warlord] within detection range,
     * or null if none exists.
     */
    private fun nearestTarget(warlord: Zombie): Player? {
        val rangeSq = HordeModule.Config.detectionRange.let { it * it }

        return warlord.world.players
            .filter { !it.isDead && it.gameMode in setOf(GameMode.SURVIVAL, GameMode.ADVENTURE) }
            .filter { it.location.distanceSquared(warlord.location) <= rangeSq }
            .minByOrNull { it.location.distanceSquared(warlord.location) }
    }
}
