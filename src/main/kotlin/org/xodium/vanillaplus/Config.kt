/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.xodium.vanillaplus.Utils.fireFmt
import org.xodium.vanillaplus.Utils.getTps
import org.xodium.vanillaplus.Utils.getWeather
import org.xodium.vanillaplus.Utils.mangoFmt
import org.bukkit.Sound as BukkitSound

/**
 * Configuration settings for the VanillaPlus plugin.
 */
object Config {
    /**
     * Configuration settings for the AutoRefillModule. This module controls the refill features of the plugin.
     */
    object AutoRefillModule {
        /**
         * Enables or disables the AutoRefillModule.
         */
        var ENABLED: Boolean = true
    }

    /**
     * Configuration settings for the AutoRestartModule. This module controls the automatic restart features of the plugin.
     */
    object AutoRestartModule {
        /**
         * Enables or disables the AutoRestartModule.
         */
        var ENABLED: Boolean = true

        /**
         * The times of day when the server should restart.
         */
        var RESTART_TIMES: MutableList<String> = mutableListOf(
            "00:00", "06:00", "12:00", "18:00"
        )

        /**
         * How many minutes before the restart to start countdown.
         */
        var COUNTDOWN_START_MINUTES: Int = 5

        /**
         * At which minutes to announce the countdown
         */
        var COUNTDOWN_ANNOUNCE_AT: List<Int> = listOf(5, 4, 3, 2, 1)

        /**
         * The message to display when the server is restarting in a countdown.
         */
        var MESSAGE_COUNTDOWN: String = "<red>Server will restart in <yellow>%time%</yellow> minutes!</red>"

        /**
         * The message to display when the server is restarting.
         */
        var MESSAGE_RESTARTING: String = "<red>Server is restarting now!</red>"
    }

    /**
     * Configuration settings for the AutoToolModule. This module controls the automatic tool selection features of the plugin.
     */
    object AutoToolModule {
        /**
         * Enables or disables the AutoToolModule.
         */
        var ENABLED: Boolean = false

        /**
         * If the AutoTool feature should not switch tools during battle.
         */
        var DONT_SWITCH_DURING_BATTLE: Boolean = true

        /**
         * If swords should be considered for breaking leaves.
         */
        var CONSIDER_SWORDS_FOR_LEAVES: Boolean = true

        /**
         * If swords should be considered for breaking cobwebs.
         */
        var CONSIDER_SWORDS_FOR_COBWEBS: Boolean = true

        /**
         * If swords should be used on hostile mobs.
         */
        var USE_SWORD_ON_HOSTILE_MOBS: Boolean = true

        /**
         * If axes should be used as swords.
         */
        var USE_AXE_AS_SWORD: Boolean = true
    }

    /**
     * Configuration settings for the DimensionsModule. This module controls the dimensions features of the plugin.
     */
    object DimensionsModule {
        /**
         * Enables or disables the DimensionsModule.
         */
        var ENABLED: Boolean = true
    }

    /**
     * Configuration settings for the DoorsModule. This module controls the door features of the plugin.
     */
    object DoorsModule {
        /**
         * Enables or disables the DoorsModule.
         */
        var ENABLED: Boolean = true

        /**
         * The sound effect used for closing doors.
         */
        var SOUND_DOOR_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /**
         * The sound effect used for closing gates.
         */
        var SOUND_GATE_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /**
         * The sound effect used for knocking.
         */
        var SOUND_KNOCK: Sound = Sound.sound(
            BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /**
         * Enables automatic closing of doors after a set delay.
         */
        var ALLOW_AUTO_CLOSE: Boolean = true

        /**
         * Allows both sides of double doors to open/close simultaneously.
         */
        var ALLOW_DOUBLE_DOORS: Boolean = true

        /**
         * Enables knocking on doors.
         */
        var ALLOW_KNOCKING_DOORS: Boolean = true

        /**
         * Enables knocking on gates.
         */
        var ALLOW_KNOCKING_GATES: Boolean = true

        /**
         * Enables knocking on trapdoors.
         */
        var ALLOW_KNOCKING_TRAPDOORS: Boolean = true

        /**
         * Knocking requires the player's hand to be empty.
         */
        var KNOCKING_REQUIRES_EMPTY_HAND: Boolean = true

        /**
         * Players must shift (crouch) to knock.
         */
        var KNOCKING_REQUIRES_SHIFT: Boolean = true

        /**
         * The delay (in seconds) before automatic closure.
         */
        var AUTO_CLOSE_DELAY: Int = 6
    }

    /**
     * Configuration settings for the InvUnloadModule. This module controls the inventory unload features of the plugin.
     */
    object InvUnloadModule {
        /**
         * Enables or disables the InvUnloadModule.
         */
        var ENABLED: Boolean = false

        /**
         * If the ChestSort plugin should be used.
         */
        var USE_CHESTSORT: Boolean = true
    }

    /**
     * Configuration settings for the MotdModule. This module controls the MOTD features of the plugin.
     */
    object MotdModule {
        /**
         * Enables or disables the MotdModule.
         */
        var ENABLED: Boolean = true

        /**
         * The message of the day, with max 2 lines.
         */
        val MOTD: List<String> = listOf(
            "<b><gradient:#CB2D3E:#EF473A>Ultimate Private SMP</gradient></b>",
            "<b><gradient:#FFE259:#FFA751>➤ WELCOME BACK LADS!</gradient></b>"
        )
    }

    /**
     * Configuration settings for the RecipiesModule. This module controls the recipe features of the plugin.
     */
    object RecipiesModule {
        /**
         * Enables or disables the RecipiesModule.
         */
        var ENABLED: Boolean = true
    }

    /**
     * Configuration settings for the TabListModule. This module controls the tab list features of the plugin.
     */
    object TabListModule {
        /**
         * Enables or disables the TabListModule.
         */
        var ENABLED: Boolean = true

        /**
         * The header of the tab list. Each element is a line.
         */
        var HEADER: List<String> = listOf(
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG 1.21.4 ⚡".fireFmt()}   ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }",
            ""
        )

        /**
         * The footer of the tab list. Each element is a line.
         */
        var FOOTER: List<String> = listOf(
            "",
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} ${getTps()} ${"|".mangoFmt()} ${
                "Weather:".fireFmt()
            } ${getWeather()}  ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }"
        )
    }

    /**
     * Configuration settings for the TreesModule. This module controls the sapling features of the plugin.
     */
    object TreesModule {
        /**
         * Enables or disables the TreesModule.
         */
        var ENABLED: Boolean = true

        /**
         * If it should ignore air blocks.
         */
        var IGNORE_AIR_BLOCKS: Boolean = true

        /**
         * If it should ignore structure void blocks.
         */
        var IGNORE_STRUCTURE_VOID_BLOCKS: Boolean = true

        /**
         * If a sapling type is missing here, no custom schematic will be used and default behavior applies.
         * You can define a file, multiple files or a folder.
         */
        var SAPLING_LINK: Map<Material, List<String>> = mapOf(
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