/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

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

data class AutoRestartModuleData(var enabled: Boolean = true)
data class BooksModuleData(var enabled: Boolean = true)
data class DimensionsModuleData(var enabled: Boolean = true)
data class DiscordModuleData(var enabled: Boolean = true)
data class DoorsModuleData(var enabled: Boolean = true)
data class EclipseModuleData(var enabled: Boolean = true)
data class InvSearchModuleData(var enabled: Boolean = true)
data class InvUnloadModuleData(var enabled: Boolean = true)
data class JoinQuitModuleData(var enabled: Boolean = true)
data class MotdModuleData(var enabled: Boolean = true)
data class RecipiesModuleData(var enabled: Boolean = true)
data class TabListModuleData(var enabled: Boolean = true)
data class TreesModuleData(var enabled: Boolean = true)