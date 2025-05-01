/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Particle
import org.bukkit.Sound

object EnumUtils {
    fun soundExists(value: String?): Boolean {
        for (sound in Sound.values()) {
            if (sound.name().equals(value, ignoreCase = true)) return true
        }
        return false
    }

    fun particleExists(value: String?): Boolean {
        for (particle in Particle.entries) {
            if (particle.name.equals(value, ignoreCase = true)) return true
        }
        return false
    }
}