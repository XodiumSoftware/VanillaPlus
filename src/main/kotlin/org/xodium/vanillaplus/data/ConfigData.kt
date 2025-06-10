/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Data class representing the configuration.
 * @property autoRestartModule Configuration for the [AutoRestartModule].
 * @property booksModule Configuration for the [BooksModule].
 * @property dimensionsModule Configuration for the [DimensionsModule].
 * @property discordModule Configuration for the [DimensionsModule].
 * @property doorsModule Configuration for the [DoorsModule].
 * @property eclipseModule Configuration for the [EclipseModule].
 * @property invSearchModule Configuration for the [InvSearchModule].
 * @property invUnloadModule Configuration for the [InvUnloadModule].
 * @property joinQuitModule Configuration for the [JoinQuitModule].
 * @property motdModule Configuration for the [MotdModule].
 * @property recipiesModule Configuration for the [RecipiesModule].
 * @property tabListModule Configuration for the [TabListModule].
 * @property treesModule Configuration for the [TreesModule].
 */
data class ConfigData(
    var autoRestartModule: AutoRestartModule = AutoRestartModule(),
    var booksModule: BooksModule = BooksModule(),
    var dimensionsModule: DimensionsModule = DimensionsModule(),
    var discordModule: DiscordModule = DiscordModule(),
    var doorsModule: DoorsModule = DoorsModule(),
    var eclipseModule: EclipseModule = EclipseModule(),
    var invSearchModule: InvSearchModule = InvSearchModule(),
    var invUnloadModule: InvUnloadModule = InvUnloadModule(),
    var joinQuitModule: JoinQuitModule = JoinQuitModule(),
    var motdModule: MotdModule = MotdModule(),
    var recipiesModule: RecipiesModule = RecipiesModule(),
    var tabListModule: TabListModule = TabListModule(),
    var treesModule: TreesModule = TreesModule()
)

data class AutoRestartModule(var enabled: Boolean = true)
data class BooksModule(var enabled: Boolean = true)
data class DimensionsModule(var enabled: Boolean = true)
data class DiscordModule(var enabled: Boolean = true)
data class DoorsModule(var enabled: Boolean = true)
data class EclipseModule(var enabled: Boolean = true)
data class InvSearchModule(var enabled: Boolean = true)
data class InvUnloadModule(var enabled: Boolean = true)
data class JoinQuitModule(var enabled: Boolean = true)
data class MotdModule(var enabled: Boolean = true)
data class RecipiesModule(var enabled: Boolean = true)
data class TabListModule(var enabled: Boolean = true)
data class TreesModule(var enabled: Boolean = true)