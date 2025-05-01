/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold

import org.bukkit.ChatColor
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

object Messages {
    val MSG_COOLDOWN: String
    private val PREFIX: String =
        ChatColor.translateAlternateColorCodes('&', instance.config.getString("message-prefix")!!)
    val MSG_COULD_NOT_UNLOAD: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString(
                "message-could-not-unload",
                "&7Nothing to unload: There are no chests for the remaining items."
            )!!
    )
    val MSG_RADIUS_TOO_HIGH: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString("message-radius-too-high", "&cError:&7 The radius cannot be higher than %d blocks.")!!
    )
    val MSG_NOT_A_NUMBER: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString("message-error-not-a-number", "&cError:&7 '%s' is not a valid number.")!!
    )
    val MSG_NO_CHESTS_NEARBY: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString(
                "message-no-chests-nearby",
                "&7Nothing to unload: There are no chests nearby. Adjust the radius or walk closer to your chests."
            )!!
    )
    val MSG_INVENTORY_EMPTY: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString("message-inventory-empty", "&7Nothing to unload: Your inventory is already empty.")!!
    )
    val MSG_NOTHING_FOUND: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&',
        instance.config.getString("message-nothing-found", "&7Could not find any chests containing %s.")!!
    )
    val MSG_COULD_NOT_UNLOAD_BLACKLIST: String = PREFIX + ChatColor.translateAlternateColorCodes(
        '&', instance.config
            .getString(
                "message-could-not-unload-blacklist",
                "&7Nothing to unload: All items in your inventory are blacklisted. Type /blacklist to see it."
            )!!
    )
    val BL_ADDED: String
    val BL_INVALID: String
    val BL_REMOVED: String
    val BL_NOTHINGSPECIFIED: String
    val BL_EMPTY: String
    val MSG_WILL_USE_HOTBAR: String
    val MSG_WILL_NOT_USE_HOTBAR: String

    init {

        MSG_WILL_USE_HOTBAR = getMsg("will-use-hotbar", "&7%s will now use items from your hotbar.")
        MSG_WILL_NOT_USE_HOTBAR = getMsg("will-not-use-hotbar", "&7%s will no longer use items from your hotbar.")

        MSG_COOLDOWN = getMsg("message-cooldown", "&cPlease wait a moment before running the command again.")

        BL_EMPTY = getMsg("blacklist-empty", "&7You blacklist is empty.")
        BL_ADDED = getMsg("blacklist-added", "&2Added to blacklist:&7 %s")
        BL_INVALID = getMsg("blacklist-invalid", "&4Invalid items:&7 %s")
        BL_REMOVED = getMsg("blacklist-removed", "&2Removed from blacklist:&7 %s")
        BL_NOTHINGSPECIFIED = getMsg(
            "blacklist-nothing-specified",
            "&7You must either hold an item in your hand or specify at least material as parameter."
        )
        instance.config.addDefault("blacklist-title", "----- &cBlacklist&r -----")
    }

    private fun getMsg(path: String?, defaultText: String?): String {
        return PREFIX + ChatColor.translateAlternateColorCodes(
            '&',
            instance.config.getString("message-$path", defaultText)!!
        )
    }
}
