/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration settings for the VanillaPlus plugin.
 */
object Config {
    /**
     * Configuration settings for the MainModule. This module controls the main features of the plugin.
     */
    object DoorsModule {
        var ENABLED: Boolean = true // Enables or disables the DoorsModule. Set to 'false' to disable.

        var SOUND_CLOSE_DOOR_EFFECT: String = "block_iron_door_close" // The sound effect used for closing doors.
        var SOUND_CLOSE_DOOR_PITCH: Int = 1 // The pitch of the closing doors.
        var SOUND_CLOSE_DOOR_VOLUME: Int = 1 // The volume of the closing doors.

        var SOUND_CLOSE_GATE_EFFECT: String = "block_fence_gate_close" // The sound effect used for closing gates.
        var SOUND_CLOSE_GATE_PITCH: Int = 1 // The pitch of the closing gates.
        var SOUND_CLOSE_GATE_VOLUME: Int = 1 // The volume of the closing gates.

        var SOUND_KNOCK_EFFECT: String = "entity_zombie_attack_wooden_door" // The sound effect used for knocking.
        var SOUND_KNOCK_PITCH: Int = 1 // The pitch of the knocking.
        var SOUND_KNOCK_VOLUME: Int = 1 // The volume of the knocking.

        var ALLOW_AUTO_CLOSE: Boolean = true // Enables automatic closing of doors after a set delay.
        var ALLOW_DOUBLE_DOORS: Boolean = true // Allows both sides of double doors to open/close simultaneously.
        var ALLOW_KNOCKING_DOORS: Boolean = true // Enables knocking on doors.
        var ALLOW_KNOCKING_GATES: Boolean = true // Enables knocking on gates.
        var ALLOW_KNOCKING_TRAPDOORS: Boolean = true // Enables knocking on trapdoors.

        var KNOCKING_REQUIRES_EMPTY_HAND: Boolean = true // Knocking requires the player's hand to be empty.
        var KNOCKING_REQUIRES_SHIFT: Boolean = true // Players must shift (crouch) to knock.

        var AUTO_CLOSE_DELAY: Int = 6 // The delay (in seconds) before automatic closure.
    }

    /**
     * Configuration settings for the GuiModule. This module controls the GUI features of the plugin.
     */
    object GuiModule {
        var ENABLED: Boolean = true // Enables or disables the GuiModule. Set to 'false' to disable.

        var ANTI_SPAM_DURATION: Duration = 1.seconds // The duration of the anti-spam feature.
    }

    /**
     * Configuration settings for the TreesModule. This module controls the sapling features of the plugin.
     */
    object TreesModule {
        var ENABLED: Boolean = true // Enables or disables the TreesModule. Set to 'false' to disable.
    }
}