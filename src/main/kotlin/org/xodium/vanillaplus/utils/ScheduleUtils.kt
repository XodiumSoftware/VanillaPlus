package org.xodium.vanillaplus.utils

import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Schedule utilities. */
internal object ScheduleUtils {
    /**
     * Schedules a repeating task.
     * @param delay The initial delay before the first particle spawn in ticks.
     * @param period The interval between particle spawns in ticks.
     * @param duration The total duration for which the task should run in ticks. If null, the task runs indefinitely.
     * @param content The content to execute in the scheduled task.
     * @return The scheduled BukkitTask.
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
}
