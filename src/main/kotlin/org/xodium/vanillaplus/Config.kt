/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BookData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.Utils.getTps
import org.xodium.vanillaplus.utils.Utils.getWeather
import java.time.LocalTime
import org.bukkit.Sound as BukkitSound

/** Configuration settings. */
object Config {
    /** Configuration settings for the AutoRefillModule. */
    object AutoRefillModule {
        /** Enables or disables the AutoRefillModule. */
        var ENABLED: Boolean = true
    }

    /** Configuration settings for the AutoRestartModule. */
    object AutoRestartModule {
        /** Enables or disables the AutoRestartModule. */
        var ENABLED: Boolean = true

        /** The times of day when the server should restart. */
        var RESTART_TIMES: MutableList<LocalTime> = mutableListOf(
            LocalTime.of(0, 0),
            LocalTime.of(6, 0),
            LocalTime.of(12, 0),
            LocalTime.of(18, 0)
        )

        /** How many minutes before the restart to start countdown. */
        var COUNTDOWN_START_MINUTES: Int = 5

        /** The placeholder for the countdown time. */
        var BOSSBAR_NAME: String = "⚡ RESTARTING in %t minute(s) ⚡"

        /** Bossbar for the auto-restart. */
        var BOSSBAR: BossBar = BossBar.bossBar(
            BOSSBAR_NAME.fireFmt().mm(),
            1.0f,
            BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
        )
    }

    /** Configuration settings for the AutoToolModule. */
    object AutoToolModule {
        /** Enables or disables the AutoToolModule. */
        var ENABLED: Boolean = true

        /** If the AutoTool feature should not switch tools during battle. */
        var DONT_SWITCH_DURING_BATTLE: Boolean = true

        /** If swords should be considered for breaking leaves. */
        var CONSIDER_SWORDS_FOR_LEAVES: Boolean = true

        /** If swords should be considered for breaking cobwebs. */
        var CONSIDER_SWORDS_FOR_COBWEBS: Boolean = true

        /** If swords should be used on hostile mobs. */
        var USE_SWORD_ON_HOSTILE_MOBS: Boolean = true

        /** If axes should be used as swords. */
        var USE_AXE_AS_SWORD: Boolean = true
    }

    /** Configuration settings for the BloodMoonModule. */
    object BloodMoonModule {
        /** Enables or disables the BloodMoonModule. */
        var ENABLED: Boolean = true

        /**
         * Map of attribute adjustments for mobs during a blood moon.
         * `it` is the current value of the attribute aka base value.
         */
        var MOB_ATTRIBUTE_ADJUSTMENTS: Map<Attribute, (Double) -> Double> = mapOf(
            Attribute.ATTACK_DAMAGE to { it * 1.5 },
            Attribute.MAX_HEALTH to { it * 2.0 }
        )

        /** The initial delay before the first blood moon. */
        var INIT_DELAY: Long = TimeUtils.seconds(0)

        /** The interval between blood moons. */
        var INTERVAL: Long = TimeUtils.seconds(10)
    }

    /** Configuration settings for the BooksModule. */
    object BooksModule {
        /** Enables or disables the BookModule. */
        var ENABLED: Boolean = true

        /** The book data, including title, author, and pages. */
        var BOOK: BookData = BookData(
            title = "Rules",
            author = instance::class.simpleName.toString(),
            pages = listOf(
                "<gold>▶ <dark_aqua>Player Rules:\n" + // Page 1
                        "<gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing.\n" +
                        "<gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming.\n" +
                        "<gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising.\n" +
                        "<gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing.\n" +
                        "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming.\n" +
                        "<gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items.\n" +
                        "<gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players.",

                "<gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers.\n" + // Page 2
                        "<gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks.\n" +
                        "<gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks.\n" +
                        "<gold>▶ <dark_aqua>11 <dark_gray>| <red>No Full Caps Messages.\n" +
                        "<gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers.\n" +
                        "<gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in Medieval style.",

                "<gold>▶ <dark_aqua>Mod/Admin Rules:\n" + // Page 3
                        "<gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff.\n" +
                        "<gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players.\n" +
                        "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items.\n" +
                        "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse."
            )
        )
    }

    /** Configuration settings for the BroadcastModule. */
    object BroadcastModule {
        /** Enables or disables the BroadcastModule. */
        var ENABLED: Boolean = true

        /** The broadcast message prefix. */
        var MESSAGE_PREFIX: String = "<gold>[<dark_aqua>TIP<gold>]"

        /** The messages to be broadcasted. */
        var MESSAGES: List<String> = listOf(
            "${"▶".mangoFmt()} ${"/home".roseFmt()} ${">".mangoFmt()} <white><italic>Teleport to your home.",
            "${"▶".mangoFmt()} ${"/skills".roseFmt()} ${">".mangoFmt()} <white><italic>Opens up the Skills GUI.",
            "${"▶".mangoFmt()} ${"/rtp".roseFmt()} ${">".mangoFmt()} <white><italic>To random teleport in the current dimension.",
            "${"▶".mangoFmt()} ${"/unload".roseFmt()} ${">".mangoFmt()} <white><italic>Unloads your inventory into nearby chests.",
            "${"▶".mangoFmt()} ${"/dump".roseFmt()} ${">".mangoFmt()} <white><italic>Dumps your inventory into nearby chests.",
            "${"▶".mangoFmt()} ${"/tpa [player]".roseFmt()} ${">".mangoFmt()} <white><italic>Request to teleport to a player.",
            "${"▶".mangoFmt()} ${"/condense".roseFmt()} ${">".mangoFmt()} <white><italic>Condenses resources (if possible) to their highest form (blocks).",
            "${"▶".mangoFmt()} ${"/uncondense".roseFmt()} ${">".mangoFmt()} <white><italic>Uncondenses resources (if possible) to their lowest form (items).",
            "${"▶".mangoFmt()} ${"Enchantment max level".roseFmt()} ${">".mangoFmt()} <white><italic>has been incremented by <red><bold>x2<dark_gray><italic>."
        )

        /** The initial delay before the first broadcast. */
        var INIT_DELAY: Long = TimeUtils.seconds(1)

        /** The interval between broadcasts. */
        var INTERVAL: Long = TimeUtils.minutes(5)
    }

    /** Configuration settings for the DimensionsModule. */
    object DimensionsModule {
        /** Enables or disables the DimensionsModule. */
        var ENABLED: Boolean = true

        /** The radius within which to search for portals in the overworld. */
        var PORTAL_SEARCH_RADIUS: Double = 128.0
    }

    /** Configuration settings for the DoorsModule. */
    object DoorsModule {
        /** Enables or disables the DoorsModule. */
        var ENABLED: Boolean = true

        /** The initial delay before the first autoclose. */
        var INIT_DELAY: Long = 1L

        /** The interval between autoclosing. */
        var INTERVAL: Long = 1L

        /** The sound effect used for closing doors. */
        var SOUND_DOOR_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for closing gates. */
        var SOUND_GATE_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for knocking. */
        var SOUND_KNOCK: Sound = Sound.sound(
            BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /** Enables automatic closing of doors after a set delay. */
        var ALLOW_AUTO_CLOSE: Boolean = true

        /** Allows both sides of double doors to open/close simultaneously. */
        var ALLOW_DOUBLE_DOORS: Boolean = true

        /** Enables knocking on doors. */
        var ALLOW_KNOCKING_DOORS: Boolean = true

        /** Enables knocking on gates. */
        var ALLOW_KNOCKING_GATES: Boolean = true

        /** Enables knocking on trapdoors. */
        var ALLOW_KNOCKING_TRAPDOORS: Boolean = true

        /** Knocking requires the player's hand to be empty. */
        var KNOCKING_REQUIRES_EMPTY_HAND: Boolean = true

        /** Players must shift (crouch) to knock. */
        var KNOCKING_REQUIRES_SHIFT: Boolean = true

        /** The delay (in milliseconds) before automatic closure. */
        var AUTO_CLOSE_DELAY: Long = 6L * 1000L // 6 seconds
    }

    /** Configuration settings for the InvSearchModule. */
    object InvSearchModule {
        /** Enables or disables the InvSearchModule. */
        var ENABLED: Boolean = true

        /** The cooldown to use the mechanic again. */
        var COOLDOWN: Long = 1L * 1000L // 1 second

        /** The radius used for searching. */
        var SEARCH_RADIUS: Int = 5
    }

    /** Configuration settings for the InvUnloadModule. */
    object InvUnloadModule {
        /** Enables or disables the InvUnloadModule. */
        var ENABLED: Boolean = true

        /** If the ChestSort plugin should be used. */
        var USE_CHESTSORT: Boolean = true

        /** The cooldown to use the mechanic again. */
        var COOLDOWN: Long = 1L * 1000L // 1 second

        /** If it should match enchantments. */
        var MATCH_ENCHANTMENTS: Boolean = true

        /** If it should match enchantments on books. */
        var MATCH_ENCHANTMENTS_ON_BOOKS: Boolean = true

        /** The sound effect used for unloading. */
        var SOUND_ON_UNLOAD: Sound = Sound.sound(
            BukkitSound.ENTITY_PLAYER_LEVELUP,
            Sound.Source.PLAYER,
            1.0f,
            1.0f
        )
    }

    /** Configuration settings for the MotdModule. */
    object MotdModule {
        /** Enables or disables the MotdModule. */
        var ENABLED: Boolean = true

        /** The message of the day, with max 2 lines. */
        val MOTD: List<String> = listOf(
            "<b>Ultimate Private SMP</b>".fireFmt(),
            "<b>➤ WELCOME BACK LADS!</b>".mangoFmt()
        )
    }

    /** Configuration settings for the RecipiesModule. */
    object RecipiesModule {
        /** Enables or disables the RecipiesModule. */
        var ENABLED: Boolean = true
    }

    /** Configuration settings for the TabListModule. */
    object TabListModule {
        /** Enables or disables the TabListModule. */
        var ENABLED: Boolean = true

        /** The initial delay before the first update. */
        var INIT_DELAY: Long = 0L

        /** The interval between updates. */
        var INTERVAL: Long = TimeUtils.seconds(10)

        /** The header of the tab list. Each element is a line. */
        var HEADER: List<String> = listOf(
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG 1.21.5 ⚡".fireFmt()}   ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }",
            ""
        )

        /** The footer of the tab list. Each element is a line. */
        var FOOTER: List<String> = listOf(
            "",
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} ${getTps()} ${"|".mangoFmt()} ${
                "Weather:".fireFmt()
            } ${getWeather()}  ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }"
        )
    }

    /** Configuration settings for the TreesModule. */
    object TreesModule {
        /** Enables or disables the TreesModule. */
        var ENABLED: Boolean = true

        /** If it should ignore air blocks. */
        var IGNORE_AIR_BLOCKS: Boolean = true

        /** If it should ignore structure void blocks. */
        var IGNORE_STRUCTURE_VOID_BLOCKS: Boolean = true

        /**
         * If a sapling type is missing here, no custom schematic will be used and default behaviour applies.
         * You can define a file, multiple files, or a folder.
         */
        var SAPLING_LINK: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            //Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"), // TODO: add when artist has schematics ready.
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        )
    }
}