/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Data class to hold information about eclipses.
 * @property isActive Indicates if the eclipse is active.
 * @property hasTriggeredThisEclipse Indicates if the eclipse has triggered.
 */
data class EclipseData(
    var isActive: Boolean = false,
    var hasTriggeredThisEclipse: Boolean = false
)