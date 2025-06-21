/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.clickRunCmd
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.Utils
import java.time.LocalTime
import org.bukkit.Sound as BukkitSound

/**
 * Data class representing the configuration.
 * @property autoRestartModule Configuration for the `AutoRestartModule`.
 * @property booksModule Configuration for the `BooksModule`.
 * @property dimensionsModule Configuration for the `DimensionsModule`.
 * @property doorsModule Configuration for the `DoorsModule`.
 * @property invSearchModule Configuration for the `InvSearchModule`.
 * @property invUnloadModule Configuration for the `InvUnloadModule`.
 * @property joinQuitModule Configuration for the `JoinQuitModule`.
 * @property motdModule Configuration for the `MotdModule`.
 * @property nicknameModule Configuration for the `NicknameModule`.
 * @property recipiesModule Configuration for the `RecipiesModule`.
 * @property tabListModule Configuration for the `TabListModule`.
 * @property treesModule Configuration for the `TreesModule`.
 * @property trowelModule Configuration for the `TrowelModule`.
 */
data class ConfigData(
    var autoRestartModule: AutoRestartModuleData = AutoRestartModuleData(),
    var booksModule: BooksModuleData = BooksModuleData(),
    var dimensionsModule: DimensionsModuleData = DimensionsModuleData(),
    var doorsModule: DoorsModuleData = DoorsModuleData(),
    var invSearchModule: InvSearchModuleData = InvSearchModuleData(),
    var invUnloadModule: InvUnloadModuleData = InvUnloadModuleData(),
    var joinQuitModule: JoinQuitModuleData = JoinQuitModuleData(),
    var motdModule: MotdModuleData = MotdModuleData(),
    var nicknameModule: NicknameModuleData = NicknameModuleData(),
    var recipiesModule: RecipiesModuleData = RecipiesModuleData(),
    var tabListModule: TabListModuleData = TabListModuleData(),
    var treesModule: TreesModuleData = TreesModuleData(),
    var trowelModule: TrowelModuleData = TrowelModuleData(),
)

/**
 * Data class representing the configuration for the `AutoRestartModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property restartTimes A list of times at which the server will restart. Default is a list containing 00:00, 06:00, 12:00, and 18:00.
 * @property bossbarName The name of the boss bar displayed during the restart countdown. Default is "⚡ RESTARTING in <time> minute(s) ⚡".
 * @property bossbar The boss bar object used for displaying the countdown. Default is a boss bar with the name defined above, full progress, red color, and progress overlay.
 * @property scheduleInitDelay The initial delay before the schedule starts, in seconds. Default is 0 seconds.
 * @property scheduleInterval The interval at which the schedule runs, in seconds. Default is 1 second.
 * @property countdownInitDelay The initial delay before the countdown starts, in seconds. Default is 0 seconds.
 * @property countdownInterval The interval at which the countdown runs, in seconds. Default is 1 second.
 * @property countdownStartMinutes The number of minutes to start the countdown from. Default is 5 minutes.
 */
data class AutoRestartModuleData(
    var enabled: Boolean = true,
    var restartTimes: MutableList<LocalTime> = mutableListOf(
        LocalTime.of(0, 0),
        LocalTime.of(6, 0),
        LocalTime.of(12, 0),
        LocalTime.of(18, 0),
    ),
    var bossbarName: String = "⚡ RESTARTING in <time> minute(s) ⚡".fireFmt(),
    var bossbar: BossBarData = BossBarData(
        bossbarName,
        1.0f,
        BossBar.Color.RED,
        BossBar.Overlay.PROGRESS,
    ),
    var scheduleInitDelay: Long = TimeUtils.seconds(0),
    var scheduleInterval: Long = TimeUtils.seconds(1),
    var countdownInitDelay: Long = TimeUtils.seconds(0),
    var countdownInterval: Long = TimeUtils.seconds(1),
    var countdownStartMinutes: Int = 5,
)

/**
 * Data class representing the configuration for the `BooksModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property books A list of `BookData` objects representing the books available in the module.
 */
data class BooksModuleData(
    var enabled: Boolean = true,
    var books: List<BookData> = listOf(
        BookData(
            "guide",
            "Guide".fireFmt(),
            instance::class.simpleName.toString().fireFmt(),
            listOf(
                // Page 1
                """
                <b><u>${"Tips & Tricks".fireFmt()}
                
                <gold>▶ ${"/home".skylineFmt()}
                <dark_gray>Teleport to your home
                
                <gold>▶ ${"/skills".skylineFmt()}
                <dark_gray>Opens up the Skills GUI
                
                <gold>▶ ${"/rtp".skylineFmt()}
                <dark_gray>Random teleport in the current dimension
                """.trimIndent(),

                // Page 2
                """
                <gold>▶ ${"/unload".skylineFmt()}
                <dark_gray>Unloads your inventory into nearby chests
                
                <gold>▶ ${"/search".skylineFmt()}
                <dark_gray>Search into nearby chests for an item
                
                <gold>▶ ${"/tpa [player]".skylineFmt()}
                <dark_gray>Request to teleport to a player
                """.trimIndent(),

                // Page 3
                """
                <gold>▶ ${"/condense".skylineFmt()}
                <dark_gray>Condenses resources (if possible) to their highest form (blocks)
                
                <gold>▶ ${"/uncondense".skylineFmt()}
                <dark_gray>Uncondenses resources (if possible) to their lowest form (items)
                """.trimIndent(),

                // Page 4
                """
                <gold>▶ ${"/nick".skylineFmt()}
                <dark_gray>Change your nickname, Visit: <b>birdflop.com</b>,
                <dark_gray>Set Color Format on MiniMessage,
                <dark_gray>Copy and Paste it after the command
                
                <gold>▶ ${"Enchantment max level".skylineFmt()}
                <dark_gray>has been incremented by <red><b>x2
                """.trimIndent()
            )
        ),
        BookData(
            "rules",
            "Rules".fireFmt(),
            instance::class.simpleName.toString().fireFmt(),
            listOf(
                // Page 1: Player Rules (1-7)
                """
                <b><u><dark_aqua>Player Rules:<reset>
        
                <gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing
                <gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming
                <gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising
                <gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing
                <gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming
                <gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items
                <gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players
                """.trimIndent(),

                // Page 2: Player Rules (8-13)
                """
                <gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers
                <gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks
                <gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks
                <gold>▶ <dark_aqua>11 <dark_gray>| <red>No Full Caps Messages
                <gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers
                <gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style
                """.trimIndent(),

                // Page 3: Mod/Admin Rules
                """
                <b><u><dark_aqua>Mod/Admin Rules:<reset>
        
                <gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff
                <gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players
                <gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items
                <gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse
                """.trimIndent()
            )
        )
    ),
)

/**
 * Data class representing the configuration for the `DimensionsModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property portalSearchRadius The search radius for portals in the dimensions. Default is 128.0 blocks.
 */
data class DimensionsModuleData(
    var enabled: Boolean = true,
    var portalSearchRadius: Double = 128.0
)

/**
 * Data class representing the configuration for the `DoorsModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property initDelay The initial delay before the doors module starts, in seconds. Default is 1 second.
 * @property interval The interval at which the doors module operates, in seconds. Default is 1 second.
 * @property allowAutoClose Indicates whether doors can automatically close. Default is true.
 * @property allowDoubleDoors Indicates whether double doors are allowed. Default is true.
 * @property allowKnockingDoors Indicates whether knocking on doors is allowed. Default is true.
 * @property allowKnockingGates Indicates whether knocking on gates is allowed. Default is true.
 * @property allowKnockingTrapdoors Indicates whether knocking on trapdoors is allowed. Default is true.
 * @property knockingRequiresEmptyHand Indicates whether knocking requires an empty hand. Default is true.
 * @property knockingRequiresShifting Indicates whether knocking requires the player to be shifting. Default is true.
 * @property autoCloseDelay The delay before doors automatically close, in milliseconds. Default is 6 seconds (6000 milliseconds).
 */
data class DoorsModuleData(
    var enabled: Boolean = true,
    var initDelay: Long = 1L,
    var interval: Long = 1L,
    var allowAutoClose: Boolean = true,
    var allowDoubleDoors: Boolean = true,
    var allowKnockingDoors: Boolean = true,
    var allowKnockingGates: Boolean = true,
    var allowKnockingTrapdoors: Boolean = true,
    var knockingRequiresEmptyHand: Boolean = true,
    var knockingRequiresShifting: Boolean = true,
    var autoCloseDelay: Long = 6L * 1000L,
    var soundDoorClose: SoundData = SoundData(
        BukkitSound.BLOCK_IRON_DOOR_CLOSE,
        Sound.Source.BLOCK
    ),
    var soundGateClose: SoundData = SoundData(
        BukkitSound.BLOCK_FENCE_GATE_CLOSE,
        Sound.Source.BLOCK
    ),
    var soundKnock: SoundData = SoundData(
        BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
        Sound.Source.HOSTILE
    ),
)

/**
 * Data class representing the configuration for the `InvSearchModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property cooldown The cooldown period for searching inventories, in milliseconds. Default is 1 second (1000 milliseconds).
 * @property searchRadius The radius within which to search for inventories, in blocks. Default is 5 blocks.
 */
data class InvSearchModuleData(
    var enabled: Boolean = true,
    var cooldown: Long = 1L * 1000L,
    var searchRadius: Int = 5,
)

/**
 * Data class representing the configuration for the `InvUnloadModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property radius The radius within which to unload inventories, in blocks. Default is 5 blocks.
 * @property cooldown The cooldown period for unloading inventories, in milliseconds. Default is 1 second (1000 milliseconds).
 * @property matchEnchantments Indicates whether to match enchantments when unloading inventories. Default is true.
 * @property matchEnchantmentsOnBooks Indicates whether to match enchantments on books when unloading inventories. Default is true.
 * @property soundOnUnload The sound played when an inventory is unloaded. Default is the player level-up sound with a volume of 1.0 and pitch of 1.0.
 */
data class InvUnloadModuleData(
    var enabled: Boolean = true,
    var radius: Int = 5,
    var cooldown: Long = 1L * 1000L,
    var matchEnchantments: Boolean = true,
    var matchEnchantmentsOnBooks: Boolean = true,
    var soundOnUnload: SoundData = SoundData(
        BukkitSound.ENTITY_PLAYER_LEVELUP,
        Sound.Source.PLAYER
    )
)

/**
 * Data class representing the configuration for the `JoinQuitModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property welcomeText A string representing the welcome message displayed to players when they join the server. Default includes a formatted welcome message with commands for rules and guide.
 */
data class JoinQuitModuleData(
    var enabled: Boolean = true,
    var welcomeText: String =
        """
        ${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)} ${"Welcome".fireFmt()} <player>
        <image>${"⯈".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)} ${"Check out".fireFmt()}<gray>: ${
            "/rules".clickRunCmd(Utils.cmdHover).skylineFmt()
        } <gray>🟅 ${"/guide".clickRunCmd(Utils.cmdHover).skylineFmt()}
        <image>${"⯈".mangoFmt(true)}
        <image>${"⯈".mangoFmt(true)}
        ${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)}
        """.trimIndent()
)

/**
 * Data class representing the configuration for the `MotdModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property motd A list of strings representing the message of the day (MOTD). Default includes a formatted title and welcome message.
 */
data class MotdModuleData(
    var enabled: Boolean = true,
    val motd: List<String> = listOf(
        "<b>Ultimate Private SMP</b>".fireFmt(),
        "<b>➤ WELCOME BACK LADS!</b>".mangoFmt()
    )
)

/**
 * Data class representing the configuration for the `NicknameModuleData`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class NicknameModuleData(
    var enabled: Boolean = true,
)

/**
 * Data class representing the configuration for the `RecipiesModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class RecipiesModuleData(
    var enabled: Boolean = true,
)

/**
 * Data class representing the configuration for the `TabListModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property initDelay The initial delay before the tab list updates, in milliseconds. Default is 0L.
 * @property interval The interval at which the tab list updates, in milliseconds. Default is 10 seconds.
 * @property header A list of strings representing the header of the tab list. Default includes a formatted string with the server version.
 * @property footer A list of strings representing the footer of the tab list. Default includes a formatted string with TPS and weather information.
 */
data class TabListModuleData(
    var enabled: Boolean = true,
    var initDelay: Long = 0L,
    var interval: Long = TimeUtils.seconds(10),
    var header: List<String> = listOf(
        "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG 1.21.6 ⚡".fireFmt()}   ${
            "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
        }",
        ""
    ),
    var footer: List<String> = listOf(
        "",
        "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} <tps> ${"|".mangoFmt()} ${
            "Weather:".fireFmt()
        } <weather>  ${
            "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
        }"
    ),
)

/**
 * Data class representing the configuration for the `TreesModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property copyBiomes Indicates whether to copy biomes when generating trees. Default is false.
 * @property copyEntities Indicates whether to copy entities when generating trees. Default is false.
 * @property ignoreAirBlocks Indicates whether to ignore air blocks when generating trees. Default is true.
 * @property ignoreStructureVoidBlocks Indicates whether to ignore structure void blocks when generating trees. Default is true.
 * @property saplingLink A map linking sapling materials to a list of strings (e.g., tree types or configurations). Default is a predefined map linking various sapling materials to their respective tree types.
 */
data class TreesModuleData(
    var enabled: Boolean = true,
    var copyBiomes: Boolean = false,
    var copyEntities: Boolean = false,
    var ignoreAirBlocks: Boolean = true,
    var ignoreStructureVoidBlocks: Boolean = true,
    var saplingLink: Map<Material, List<String>> = mapOf(
        Material.ACACIA_SAPLING to listOf("trees/acacia"),
        Material.BIRCH_SAPLING to listOf("trees/birch"),
        Material.CHERRY_SAPLING to listOf("trees/cherry"),
        Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
        Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
        Material.JUNGLE_SAPLING to listOf("trees/jungle"),
        Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
        Material.OAK_SAPLING to listOf("trees/oak"),
        Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"),
        Material.SPRUCE_SAPLING to listOf("trees/spruce"),
        Material.WARPED_FUNGUS to listOf("trees/warped"),
    ),
)

/**
 * Data class representing the configuration for the `TrowelModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class TrowelModuleData(
    var enabled: Boolean = true,
)