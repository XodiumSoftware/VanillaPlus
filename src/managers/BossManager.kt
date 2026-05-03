package org.xodium.illyriaplus.managers

import org.bukkit.entity.LivingEntity
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * Manages active boss instances and their tick updates.
 */
internal object BossManager {
    private val activeBosses = mutableMapOf<LivingEntity, BossInterface>()

    /**
     * Registers a spawned boss for tick updates.
     *
     * @param entity The boss entity.
     * @param boss The boss interface instance.
     */
    fun registerBoss(
        entity: LivingEntity,
        boss: BossInterface,
    ) {
        activeBosses[entity] = boss
    }

    /**
     * Unregisters a boss (e.g., on death or despawn).
     *
     * @param entity The boss entity.
     */
    fun unregisterBoss(entity: LivingEntity) {
        activeBosses.remove(entity)
    }

    /**
     * Starts the tick scheduler. Should be called once on plugin enable.
     */
    fun startTickScheduler() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                activeBosses.toMap().forEach { (entity, boss) ->
                    if (entity.isValid && !entity.isDead) boss.onTick(entity) else unregisterBoss(entity)
                }
            },
            0L,
            1L,
        )
    }
}
