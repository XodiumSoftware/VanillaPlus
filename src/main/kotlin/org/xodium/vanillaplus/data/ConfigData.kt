/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.bukkit.Sound as BukkitSound

/**
 * Configuration settings for the VanillaPlus plugin.
 */
@Serializable
data class ConfigData(
    val guiAntiSpamDuration: Duration = 1.seconds // The duration of the anti-spam feature.
) {
    /**
     * Configuration settings for the AutoRefillModule. This module controls the refill features of the plugin.
     */
    @Serializable
    data class AutoRefillModule(
        val enabled: Boolean = true, // Enables or disables the RefillModule. Set to 'false' to disable.

        val defaultEnabledRefill: Boolean = true, // Default state of the Refill feature for users.
    )

    /**
     * Configuration settings for the AutoToolModule. This module controls the automatic tool selection features of the plugin.
     */
    @Serializable
    data class AutoToolModule(
        val enabled: Boolean = false, // Enables or disables the AutoToolModule. Set to 'false' to disable.

        val defaultEnabledAutoTool: Boolean = true, // Default state of the AutoTool feature for users.

        val dontSwitchDuringBattle: Boolean = true, // If the AutoTool feature should not switch tools during battle.

        val considerSwordsForLeaves: Boolean = true, // If swords should be considered for breaking leaves.
        val considerSwordsForCobwebs: Boolean = true, // If swords should be considered for breaking cobwebs.

        val useSwordOnHostileMobs: Boolean = true, // If swords should be used on hostile mobs.
        val useAxeAsSword: Boolean = true, // If axes should be used as swords.
    )

    /**
     * Configuration settings for the DimensionsModule. This module controls the dimensions features of the plugin.
     */
    @Serializable
    data class DimensionsModule(
        val enabled: Boolean = true, // Enables or disables the DimensionsModule. Set to 'false' to disable.
    )

    /**
     * Configuration settings for the DoorsModule. This module controls the door features of the plugin.
     */
    @Serializable
    data class DoorsModule(
        val enabled: Boolean = true, // Enables or disables the DoorsModule. Set to 'false' to disable.

        val soundDoorClose: Sound = Sound.sound(
            BukkitSound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        ), // The sound effect used for closing doors.
        val soundGateClose: Sound = Sound.sound(
            BukkitSound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        ), // The sound effect used for closing gates.
        val soundKnock: Sound = Sound.sound(
            BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        ), // The sound effect used for knocking.

        val allowAutoClose: Boolean = true, // Enables automatic closing of doors after a set delay.
        val allowDoubleDoors: Boolean = true, // Allows both sides of double doors to open/close simultaneously.
        val allowKnockingDoors: Boolean = true, // Enables knocking on doors.
        val allowKnockingGates: Boolean = true, // Enables knocking on gates.
        val allowKnockingTrapdoors: Boolean = true, // Enables knocking on trapdoors.

        val knockingRequiresEmptyHand: Boolean = true, // Knocking requires the player's hand to be empty.
        val knockingRequiresShift: Boolean = true, // Players must shift (crouch) to knock.

        val autoCloseDelay: Int = 6, // The delay (in seconds) before automatic closure.
    )

    /**
     * Configuration settings for the MotdModule. This module controls the MOTD features of the plugin.
     */
    @Serializable
    data class MotdModule(
        val enabled: Boolean = true, // Enables or disables the MotdModule. Set to 'false' to disable.

        val motd: List<String> = listOf(
            "<b><gradient:#CB2D3E:#EF473A>Ultimate Private SMP</gradient></b>",
            "<b><gradient:#FFE259:#FFA751>➤ WELCOME BACK LADS!</gradient></b>"
        ), // The message of the day, with max 2 lines.
    )

    /**
     * Configuration settings for the RecipiesModule. This module controls the recipe features of the plugin.
     */
    @Serializable
    data class RecipiesModule(
        val enabled: Boolean = true, // Enables or disables the SkinsModule. Set to 'false' to disable.
    )

    /**
     * Configuration settings for the TreesModule. This module controls the sapling features of the plugin.
     */
    @Serializable
    data class TreesModule(
        val enabled: Boolean = true, // Enables or disables the TreesModule. Set to 'false' to disable.

        val ignoreAirBlocks: Boolean = true, // If it should ignore air blocks.
        val ignoreStructureVoidBlocks: Boolean = true, // If it should ignore structure void blocks.

        // If a sapling type is missing here, no custom schematic will be used and default behavior applies.
        // You can define a file, multiple files or a folder.
        val saplingLink: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        ),
    )
}

