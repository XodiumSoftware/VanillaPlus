@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/** Time utilities. */
internal object TimeUtils {
    private const val SECONDS: Long = 20L // 20L
    private const val MINUTES: Long = SECONDS * 60L // 1200L
    private const val HOURS: Long = MINUTES * 60L // 72000L

    /** Converts the given time in seconds to the equivalent number of ticks. */
    fun seconds(time: Long): Long = time * SECONDS

    /** Converts the given time in minutes to the equivalent number of ticks. */
    fun minutes(time: Long): Long = time * MINUTES

    /** Converts the given time in hours to the equivalent number of ticks. */
    fun hours(time: Long): Long = time * HOURS
}