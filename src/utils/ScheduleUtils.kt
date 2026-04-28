package org.xodium.illyriaplus.utils

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitTask
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance

/** Schedule utilities. */
internal object ScheduleUtils {
    /**
     * Schedules a repeating task.
     * @param delay The initial delay before the first execution in ticks.
     * @param period The interval between executions in ticks.
     * @param duration The total duration for which the task should run in ticks. If `null`, the task runs indefinitely.
     * @param content The content to execute in the scheduled task.
     * @return The scheduled [BukkitTask].
     */
    fun schedule(
        delay: Long = 0L,
        period: Long = 2L,
        duration: Long? = null,
        content: () -> Unit,
    ): BukkitTask =
        instance.server.scheduler.runTaskTimer(instance, content, delay, period).also { task ->
            duration?.let { instance.server.scheduler.runTaskLater(instance, task::cancel, it) }
        }

    /**
     * Spawns a repeating particle trail on [entity] every tick until it is no longer valid.
     * @param entity The entity to follow.
     * @param particles Called each tick with the entity's current [Location] to spawn particles.
     * @return The [BukkitTask] running the trail.
     */
    fun spawnProjectileTrail(
        entity: Entity,
        particles: (Location) -> Unit,
    ): BukkitTask {
        lateinit var task: BukkitTask

        task =
            schedule(delay = 1L, period = 1L) {
                if (!entity.isValid) {
                    task.cancel()
                    return@schedule
                }

                particles(entity.location)
            }

        return task
    }
}
