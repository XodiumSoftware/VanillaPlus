/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.data.SkinData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.bukkit.Sound as BukkitSound

/**
 * Configuration settings for the VanillaPlus plugin.
 */
object Config {
    var GUI_ANTI_SPAM_DURATION: Duration = 1.seconds // The duration of the anti-spam feature.

    init {
        Database.createTable(this::class)
    }

    /**
     * Configuration settings for the MainModule. This module controls the main features of the plugin.
     */
    object DoorsModule {
        var ENABLED: Boolean = true // Enables or disables the DoorsModule. Set to 'false' to disable.

        var SOUND_DOOR_CLOSE: Sound =
            Sound.sound(
                BukkitSound.BLOCK_IRON_DOOR_CLOSE,
                Sound.Source.BLOCK,
                1.0f,
                1.0f
            ) // The sound effect used for closing doors.

        var SOUND_GATE_CLOSE: Sound =
            Sound.sound(
                BukkitSound.BLOCK_FENCE_GATE_CLOSE,
                Sound.Source.BLOCK,
                1.0f,
                1.0f
            ) // The sound effect used for closing gates.

        var SOUND_KNOCK: Sound =
            Sound.sound(
                BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
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
     * Configuration settings for the MotdModule. This module controls the MOTD features of the plugin.
     */
    object MotdModule {
        var ENABLED: Boolean = true // Enables or disables the MotdModule. Set to 'false' to disable.

        var MOTD: List<String> = listOf(
            "<b><gradient:#CB2D3E:#EF473A>Ultimate Private SMP</gradient></b>",
            "<b><gradient:#FFE259:#FFA751>➤ WELCOME BACK LADS!</gradient></b>"
        ) // The message of the day.
    }

    /**
     * Configuration settings for the RecipiesModule. This module controls the recipe features of the plugin.
     */
    object RecipiesModule {
        var ENABLED: Boolean = true // Enables or disables the RecipiesModule. Set to 'false' to disable.
    }

    /**
     * Configuration settings for the SkinsModule. This module controls the skin features of the plugin.
     */
    object SkinsModule {
        var ENABLED: Boolean = true // Enables or disables the SkinsModule. Set to 'false' to disable.

        var SKINS: List<SkinData> = listOf(
            SkinData(EntityType.WITHER, Material.WITHER_SPAWN_EGG),
            SkinData(EntityType.ELDER_GUARDIAN, Material.ELDER_GUARDIAN_SPAWN_EGG),
            SkinData(EntityType.WARDEN, Material.WARDEN_SPAWN_EGG),
            SkinData(EntityType.ENDER_DRAGON, Material.ENDER_DRAGON_SPAWN_EGG)
        ) // The list of skins available.

        var SOUND_UNLOCK_SKIN: Sound =
            Sound.sound(
                BukkitSound.ENTITY_PLAYER_LEVELUP,
                Sound.Source.PLAYER,
                1.0f,
                1.0f
            ) // The sound effect used when unlocking a skin.
    }

    /**
     * Configuration settings for the TreesModule. This module controls the sapling features of the plugin.
     */
    object TreesModule {
        var ENABLED: Boolean = true // Enables or disables the TreesModule. Set to 'false' to disable.

        var IGNORE_AIR_BLOCKS: Boolean = true // If it should ignore air blocks.

        // If a sapling type is missing here, no custom schematic will be used and default behavior applies.
        // You can define a file, multiple files or a folder.
        var SAPLING_LINK: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce")
        )
    }
}