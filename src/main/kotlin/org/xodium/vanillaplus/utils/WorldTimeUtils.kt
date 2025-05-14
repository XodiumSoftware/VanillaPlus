/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/** Time utilities. */
object WorldTimeUtils {
    val DAWN: LongRange = 0L..999L
    val DAY: LongRange = 1000L..11999L
    val DUSK: LongRange = 12000L..12999L
    val NIGHT: LongRange = 13000L..23999L
}