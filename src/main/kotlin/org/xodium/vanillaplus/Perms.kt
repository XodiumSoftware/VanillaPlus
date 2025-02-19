/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/**
 * Permissions for the VanillaPlus plugin.
 */
object Perms {
    private val G0 = instance::class.simpleName.toString().lowercase()

    init {
        listOf<Permission>(
            Use.GENERAL,
            Gui.FAQ,
            Gui.DIMS,
            Gui.SETTINGS,
            Gui.SKINS
        ).forEach(instance.server.pluginManager::addPermission)
    }

    /**
     * Permissions for Usage commands.
     */
    object Use {
        private val G1 = this::class.simpleName.toString().lowercase()
        val GENERAL: Permission =
            Permission("${G0}.${G1}", "Allows use of the commands", PermissionDefault.TRUE)
    }

    /**
     * Permissions for Gui commands.
     */
    object Gui {
        private val G1 = this::class.simpleName.toString().lowercase()
        val FAQ: Permission =
            Permission("${G0}.${G1}.faq", "Allows viewing the FAQ gui", PermissionDefault.TRUE)
        val DIMS: Permission =
            Permission("${G0}.${G1}.dims", "Allows viewing the dimensions gui", PermissionDefault.TRUE)
        val SETTINGS: Permission =
            Permission("${G0}.${G1}.settings", "Allows viewing the settings gui", PermissionDefault.TRUE)
        val SKINS: Permission =
            Permission("${G0}.${G1}.skins", "Allows viewing the skins gui", PermissionDefault.TRUE)
    }
}