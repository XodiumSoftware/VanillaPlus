/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/**
 * Time utilities
 */
object TimeUtils {
    private const val TICKS_PER_SECOND = 20L

    // Extensions for Int
    val Int.ticks: Long get() = this.toLong()
    val Int.seconds: Long get() = this * TICKS_PER_SECOND
    val Int.minutes: Long get() = this * TICKS_PER_SECOND * 60
    val Int.hours: Long get() = this * TICKS_PER_SECOND * 3600

    // Extensions for Long
    val Long.ticks: Long get() = this
    val Long.seconds: Long get() = this * TICKS_PER_SECOND
    val Long.minutes: Long get() = this * TICKS_PER_SECOND * 60
    val Long.hours: Long get() = this * TICKS_PER_SECOND * 3600
}