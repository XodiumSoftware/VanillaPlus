@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/** Time utilities. */
@Deprecated("since 1.16.3, use Duration instead.", replaceWith = ReplaceWith("Duration"))
internal object TimeUtils {
    private const val SECONDS: Long = 20L // 20L
    private const val MINUTES: Long = SECONDS * 60L // 1200L
    private const val HOURS: Long = MINUTES * 60L // 72000L

    /** Converts the given time in seconds to the equivalent number of ticks. */
    fun seconds(time: Long): Long = time * SECONDS

    fun Int.sec() = this * SECONDS // TODO: Temporary fix, see if you can move to using [Duration].

    /** Converts the given time in minutes to the equivalent number of ticks. */
    fun minutes(time: Long): Long = time * MINUTES

    /** Converts the given time in hours to the equivalent number of ticks. */
    fun hours(time: Long): Long = time * HOURS
}
