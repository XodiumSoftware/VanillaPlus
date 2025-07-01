/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material

/**
 * Represents a tier in the Vanilla Plus quest system
 * @property icon The material icon representing the tier.
 * @property title The title of the tier.
 * @property requirement The requirement to achieve this tier.
 * @property reward The reward for completing this tier.
 */
data class Tier(
    val icon: Material,
    val title: String,
    val requirement: String,
    val reward: String
)