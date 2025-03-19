/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import net.kyori.adventure.sound.Sound
import org.bukkit.Material

/**
 * Configuration settings for the VanillaPlus plugin.
 */
object Config {
    /**
     * Configuration settings for the AutoRefillModule. This module controls the refill features of the plugin.
     */
    object AutoRefillModule {
        var ENABLED: Boolean = true // Enables or disables the AutoRefillModule. Set to 'false' to disable.
    }

    /**
     * Configuration settings for the AutoToolModule. This module controls the automatic tool selection features of the plugin.
     */
    object AutoToolModule {
        var ENABLED: Boolean = false // Enables or disables the AutoToolModule. Set to 'false' to disable.
        var DONT_SWITCH_DURING_BATTLE: Boolean =
            true // If the AutoTool feature should not switch tools during battle.
        var CONSIDER_SWORDS_FOR_LEAVES: Boolean = true // If swords should be considered for breaking leaves.
        var CONSIDER_SWORDS_FOR_COBWEBS: Boolean = true // If swords should be considered for breaking cobwebs.
        var USE_SWORD_ON_HOSTILE_MOBS: Boolean = true // If swords should be used on hostile mobs.
        var USE_AXE_AS_SWORD: Boolean = true // If axes should be used as swords.
    }

    /**
     * Configuration settings for the DimensionsModule. This module controls the dimensions features of the plugin.
     */
    object DimensionsModule {
        var ENABLED: Boolean = true // Enables or disables the DimensionsModule. Set to 'false' to disable.
    }

    /**
     * Configuration settings for the DoorsModule. This module controls the door features of the plugin.
     */
    object DoorsModule {
        var ENABLED: Boolean = true // Enables or disables the DoorsModule. Set to 'false' to disable.
        val SOUND_DOOR_CLOSE: Sound = Sound.sound(
            org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        ) // The sound effect used for closing doors.
        val SOUND_GATE_CLOSE: Sound = Sound.sound(
            org.bukkit.Sound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        ) // The sound effect used for closing gates.
        val SOUND_KNOCK: Sound = Sound.sound(
            org.bukkit.Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        ) // The sound effect used for knocking.
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
     * Configuration settings for the InvUnloadModule. This module controls the inventory unload features of the plugin.
     */
    object InvUnloadModule {
        var ENABLED: Boolean = false // Enables or disables the InvUnloadModule. Set to 'false' to disable.
        var USE_CHESTSORT: Boolean = true // If the ChestSort plugin should be used.
    }

    /**
     * Configuration settings for the MotdModule. This module controls the MOTD features of the plugin.
     */
    object MotdModule {
        var ENABLED: Boolean = true // Enables or disables the MotdModule. Set to 'false' to disable.
        val MOTD: List<String> = listOf(
            "<b><gradient:#CB2D3E:#EF473A>Ultimate Private SMP</gradient></b>",
            "<b><gradient:#FFE259:#FFA751>➤ WELCOME BACK LADS!</gradient></b>"
        ) // The message of the day, with max 2 lines.
    }

    /**
     * Configuration settings for the RecipiesModule. This module controls the recipe features of the plugin.
     */
    object RecipiesModule {
        var ENABLED: Boolean = true // Enables or disables the RecipiesModule. Set to 'false' to disable.
    }

    /**
     * Configuration settings for the TabListModule. This module controls the tab list features of the plugin.
     */
    object TabListModule {
        var ENABLED: Boolean = true // Enables or disables the TabListModule. Set to 'false' to disable.
    }

    /**
     * Configuration settings for the TreesModule. This module controls the sapling features of the plugin.
     */
    object TreesModule {
        var ENABLED: Boolean = true // Enables or disables the TreesModule. Set to 'false' to disable.
        var IGNORE_AIR_BLOCKS: Boolean = true // If it should ignore air blocks.
        var IGNORE_STRUCTURE_VOID_BLOCKS: Boolean = true // If it should ignore structure void blocks.

        // If a sapling type is missing here, no custom schematic will be used and default behavior applies.
        // You can define a file, multiple files or a folder.
        val SAPLING_LINK: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        )
    }
}