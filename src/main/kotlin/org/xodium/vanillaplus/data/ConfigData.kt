/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material
import org.xodium.vanillaplus.utils.TimeUtils

/**
 * Data class representing the configuration.
 * @property autoRestartModule Configuration for the `AutoRestartModule`.
 * @property booksModule Configuration for the `BooksModule`.
 * @property dimensionsModule Configuration for the `DimensionsModule`.
 * @property discordModule Configuration for the `DiscordModule`.
 * @property doorsModule Configuration for the `DoorsModule`.
 * @property eclipseModule Configuration for the `EclipseModule`.
 * @property invSearchModule Configuration for the `InvSearchModule`.
 * @property invUnloadModule Configuration for the `InvUnloadModule`.
 * @property joinQuitModule Configuration for the `JoinQuitModule`.
 * @property motdModule Configuration for the `MotdModule`.
 * @property recipiesModule Configuration for the `RecipiesModule`.
 * @property tabListModule Configuration for the `TabListModule`.
 * @property treesModule Configuration for the `TreesModule`.
 */
data class ConfigData(
    var autoRestartModule: AutoRestartModuleData = AutoRestartModuleData(),
    var booksModule: BooksModuleData = BooksModuleData(),
    var dimensionsModule: DimensionsModuleData = DimensionsModuleData(),
    var discordModule: DiscordModuleData = DiscordModuleData(),
    var doorsModule: DoorsModuleData = DoorsModuleData(),
    var eclipseModule: EclipseModuleData = EclipseModuleData(),
    var invSearchModule: InvSearchModuleData = InvSearchModuleData(),
    var invUnloadModule: InvUnloadModuleData = InvUnloadModuleData(),
    var joinQuitModule: JoinQuitModuleData = JoinQuitModuleData(),
    var motdModule: MotdModuleData = MotdModuleData(),
    var recipiesModule: RecipiesModuleData = RecipiesModuleData(),
    var tabListModule: TabListModuleData = TabListModuleData(),
    var treesModule: TreesModuleData = TreesModuleData()
)

/**
 * Data class representing the configuration for the `AutoRestartModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property scheduleInitDelay The initial delay before the schedule starts, in seconds. Default is 0 seconds.
 * @property scheduleInterval The interval at which the schedule runs, in seconds. Default is 1 second.
 * @property countdownInitDelay The initial delay before the countdown starts, in seconds. Default is 0 seconds.
 * @property countdownInterval The interval at which the countdown runs, in seconds. Default is 1 second.
 * @property countdownStartMinutes The number of minutes to start the countdown from. Default is 5 minutes.
 */
data class AutoRestartModuleData(
    var enabled: Boolean = true,
    var scheduleInitDelay: Long = TimeUtils.seconds(0),
    var scheduleInterval: Long = TimeUtils.seconds(1),
    var countdownInitDelay: Long = TimeUtils.seconds(0),
    var countdownInterval: Long = TimeUtils.seconds(1),
    var countdownStartMinutes: Int = 5,
)

/**
 * Data class representing the configuration for the `BooksModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class BooksModuleData(
    var enabled: Boolean = true,
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
 * Data class representing the configuration for the `DiscordModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class DiscordModuleData(
    var enabled: Boolean = true,
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
)

/**
 * Data class representing the configuration for the `EclipseModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property randomPoweredCreepers Indicates whether creepers can randomly become powered. Default is true.
 */
data class EclipseModuleData(
    var enabled: Boolean = true,
    var randomPoweredCreepers: Boolean = true,
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
 * @property cooldown The cooldown period for unloading inventories, in milliseconds. Default is 1 second (1000 milliseconds).
 * @property matchEnchantments Indicates whether to match enchantments when unloading inventories. Default is true.
 * @property matchEnchantmentsOnBooks Indicates whether to match enchantments on books when unloading inventories. Default is true.
 */
data class InvUnloadModuleData(
    var enabled: Boolean = true,
    var cooldown: Long = 1L * 1000L,
    var matchEnchantments: Boolean = true,
    var matchEnchantmentsOnBooks: Boolean = true,
)

/**
 * Data class representing the configuration for the `JoinQuitModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class JoinQuitModuleData(
    var enabled: Boolean = true,
)

/**
 * Data class representing the configuration for the `MotdModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 */
data class MotdModuleData(
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
 */
data class TabListModuleData(
    var enabled: Boolean = true,
    var initDelay: Long = 0L,
    var interval: Long = TimeUtils.seconds(10),
)

/**
 * Data class representing the configuration for the `TreesModule`.
 * @property enabled Indicates whether the module is enabled. Default is true.
 * @property copyBiomes Indicates whether to copy biomes when generating trees. Default is false.
 * @property copyEntities Indicates whether to copy entities when generating trees. Default is false.
 * @property ignoreAirBlocks Indicates whether to ignore air blocks when generating trees. Default is true.
 * @property ignoreStructureVoidBlocks Indicates whether to ignore structure void blocks when generating trees. Default is true.
 */
data class TreesModuleData(
    var enabled: Boolean = true,
    var copyBiomes: Boolean = false,
    var copyEntities: Boolean = false,
    var ignoreAirBlocks: Boolean = true,
    var ignoreStructureVoidBlocks: Boolean = true,
    var saplingLink: Map<Material, List<String>> = TODO(),
)