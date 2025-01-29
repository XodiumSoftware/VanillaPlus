/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

object Config {
    // Configuration settings for the DoorsModule. This module controls door-related features.
    object DoorsModule {
        var ENABLE: Boolean = true // Enables or disables the DoorsModule. Set to 'false' to disable.

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

    // Configuration settings for the SaplingModule. This module controls sapling-related features.
    object SaplingModule {
        var ENABLE: Boolean = true // Enables or disables the SaplingModule. Set to 'false' to disable.

        var IGNORE_AIR_BLOCKS: Boolean = true // If it should ignore air blocks when placing custom tree.
        var IGNORE_STRUCTURE_VOID_BLOCKS: Boolean =
            true // If it should ignore structure void blocks when placing custom tree.
        var COPY_ENTITIES: Boolean = false // If it should copy entities from the schematic.
        var COPY_BIOMES: Boolean = false // If it should copy biomes from the schematic.

        // If a sapling type is missing here, no custom schematic will be used and default behavior applies.
        // You can define a file, multiple files or a folder.
        var saplingLink: Map<String, List<String>> = mapOf(
            "ACACIA_SAPLING" to listOf("trees/acacia"),
            "BIRCH_SAPLING" to listOf("trees/birch"),
            "CHERRY_SAPLING" to listOf("trees/cherry"),
            "DARK_OAK_SAPLING" to listOf("trees/dark_oak"),
            "JUNGLE_SAPLING" to listOf("trees/jungle"),
            "MANGROVE_PROPAGULE" to listOf("trees/mangrove"),
            "OAK_SAPLING" to listOf("trees/oak"),
            "SPRUCE_SAPLING" to listOf("trees/spruce")
        )
    }
}