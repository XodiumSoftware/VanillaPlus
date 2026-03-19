package org.xodium.vanillaplus.utils

import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

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
     * Runs a task asynchronously off the main server thread.
     * @param content The task to execute on a worker thread.
     * @return The scheduled [BukkitTask].
     */
    fun runAsync(content: () -> Unit): BukkitTask = instance.server.scheduler.runTaskAsynchronously(instance, content)

    /**
     * Runs a task synchronously on the main server thread.
     * @param content The task to execute on the main thread.
     * @return The scheduled [BukkitTask].
     */
    fun runSync(content: () -> Unit): BukkitTask = instance.server.scheduler.runTask(instance, content)
}
