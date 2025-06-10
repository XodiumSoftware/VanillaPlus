/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/** Data class representing the state of various modules in the application.
 * @property AutoRestartModule Indicates if the auto-restart module is enabled.
 * @property BooksModule Indicates if the books module is enabled.
 * @property DimensionsModule Indicates if the dimensions module is enabled.
 * @property DiscordModule Indicates if the Discord module is enabled.
 * @property DoorsModule Indicates if the doors module is enabled.
 * @property EclipseModule Indicates if the eclipse module is enabled.
 * @property InvSearchModule Indicates if the inventory search module is enabled.
 * @property InvUnloadModule Indicates if the inventory unload module is enabled.
 * @property JoinQuitModule Indicates if the join/quit module is enabled.
 * @property MotdModule Indicates if the message of the day (MOTD) module is enabled.
 * @property RecipiesModule Indicates if the recipes module is enabled.
 * @property TabListModule Indicates if the tab list module is enabled.
 * @property TreesModule Indicates if the trees module is enabled.
 */
data class ModuleStateData(
    var AutoRestartModule: Boolean = true,
    var BooksModule: Boolean = true,
    var DimensionsModule: Boolean = true,
    var DiscordModule: Boolean = true,
    var DoorsModule: Boolean = true,
    var EclipseModule: Boolean = true,
    var InvSearchModule: Boolean = true,
    var InvUnloadModule: Boolean = true,
    var JoinQuitModule: Boolean = true,
    var MotdModule: Boolean = true,
    var RecipiesModule: Boolean = true,
    var TabListModule: Boolean = true,
    var TreesModule: Boolean = true
)
