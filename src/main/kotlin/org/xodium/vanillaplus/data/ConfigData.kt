/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/** Data class representing the state of various modules in the application.
 * @property autoRestartModule Indicates if the auto-restart module is enabled.
 * @property booksModule Indicates if the books module is enabled.
 * @property dimensionsModule Indicates if the dimensions module is enabled.
 * @property discordModule Indicates if the Discord module is enabled.
 * @property doorsModule Indicates if the doors module is enabled.
 * @property eclipseModule Indicates if the eclipse module is enabled.
 * @property invSearchModule Indicates if the inventory search module is enabled.
 * @property invUnloadModule Indicates if the inventory unload module is enabled.
 * @property joinQuitModule Indicates if the join/quit module is enabled.
 * @property motdModule Indicates if the message of the day (MOTD) module is enabled.
 * @property recipiesModule Indicates if the recipes module is enabled.
 * @property tabListModule Indicates if the tab list module is enabled.
 * @property treesModule Indicates if the trees module is enabled.
 */
data class ConfigData(
    var autoRestartModule: Boolean = true,
    var booksModule: Boolean = true,
    var dimensionsModule: Boolean = true,
    var discordModule: Boolean = true,
    var doorsModule: Boolean = true,
    var eclipseModule: Boolean = true,
    var invSearchModule: Boolean = true,
    var invUnloadModule: Boolean = true,
    var joinQuitModule: Boolean = true,
    var motdModule: Boolean = true,
    var recipiesModule: Boolean = true,
    var tabListModule: Boolean = true,
    var treesModule: Boolean = true
)
