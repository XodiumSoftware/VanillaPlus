/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Permissions for the VanillaPlus plugin. */
object Perms {
    private val G0 = instance::class.simpleName.toString().lowercase()

    /** Register all permissions. */
    init {
        listOf<Permission>(
            Use.GENERAL,
            AutoRefill.USE,
            AutoTool.USE,
            Horde.NEW_MOON,
            InvSearch.USE,
            InvUnload.USE,
        ).forEach(instance.server.pluginManager::addPermission)
    }

    /** Permissions for Usage commands. */
    object Use {
        private val G1 = this::class.simpleName.toString().lowercase()
        val GENERAL: Permission =
            Permission("${G0}.${G1}", "Allows use of the commands", PermissionDefault.TRUE)
    }

    /** Permissions for AutoRefill commands. */
    object AutoRefill {
        private val G1 = this::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the autorefill command", PermissionDefault.TRUE)
    }

    /** Permissions for AutoTool commands. */
    object AutoTool {
        private val G1 = this::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the autotool command", PermissionDefault.TRUE)
    }

    /** Permissions for Horde commands. */
    object Horde {
        private val G1 = instance::class.simpleName.toString().lowercase()
        val NEW_MOON: Permission =
            Permission("${G0}.${G1}.newmoon", "Allows use of the newmoon command", PermissionDefault.OP)
    }

    /** Permissions for InvSearch commands. */
    object InvSearch {
        private val G1 = instance::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the invsearch command", PermissionDefault.TRUE)
    }

    /** Permissions for InvUnload commands. */
    object InvUnload {
        private val G1 = instance::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the invunload command", PermissionDefault.TRUE)
    }
}