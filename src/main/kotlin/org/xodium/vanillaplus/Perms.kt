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
        val permissions = mutableListOf<Permission>()
        fun collectPermissions(obj: Any) {
            obj::class.java.declaredFields.forEach { field ->
                field.isAccessible = true
                val value = field.get(obj)
                if (value is Permission) {
                    permissions.add(value)
                } else if (value != null && value::class.java.isMemberClass) {
                    collectPermissions(value)
                }
            }
        }
        collectPermissions(this)
        permissions.forEach(instance.server.pluginManager::addPermission)
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

    /** Permissions for Book commands. */
    object Book {
        private val G1 = this::class.simpleName.toString().lowercase()
        val GUIDE: Permission =
            Permission("${G0}.${G1}.guide", "Allows use of the guide command", PermissionDefault.TRUE)
        val RULES: Permission =
            Permission("${G0}.${G1}.rules", "Allows use of the rules command", PermissionDefault.TRUE)
    }

    /** Permissions for Eclipse commands. */
    object Eclipse {
        private val G1 = this::class.simpleName.toString().lowercase()
        val ECLIPSE: Permission =
            Permission("${G0}.${G1}.eclipse", "Allows use of the eclipse command", PermissionDefault.OP)
    }

    /** Permissions for InvSearch commands. */
    object InvSearch {
        private val G1 = this::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the invsearch command", PermissionDefault.TRUE)
    }

    /** Permissions for InvUnload commands. */
    object InvUnload {
        private val G1 = this::class.simpleName.toString().lowercase()
        val USE: Permission =
            Permission("${G0}.${G1}.use", "Allows use of the invunload command", PermissionDefault.TRUE)
    }
}