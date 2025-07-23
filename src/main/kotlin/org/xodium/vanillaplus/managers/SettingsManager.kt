package org.xodium.vanillaplus.managers

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

internal object SettingsManager : Listener {

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun on(event: PlayerCustomClickEvent) {
        val identifier = "${instance::class.simpleName}.dialog."
        instance.logger.info("SettingsManager: $identifier")
        if (event.identifier.value().startsWith(identifier)) {
            val connection = event.commonConnection
            if (connection !is PlayerGameConnection) return
            val player = connection.player
            val moduleName = event.identifier.value().substringAfterLast(".")
            val module = ModuleManager.modules.first {
                it::class.simpleName.toString().lowercase() == moduleName
            }
            player.showDialog(moduleDialogFor(module))
            return
        }
    }

    /**
     * Defines a list of commands for the VanillaPlus module.
     * @return A list of [CommandData] containing the command definition, description, and aliases for usage.
     */
    fun cmds(): List<CommandData> {
        return listOf(
            CommandData(
                Commands.literal("settings")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { (it.sender as Player).showDialog(dialog()) } },
                "Opens up the settings.",
            )
        )
    }

    /**
     * Retrieves a list of permissions related to the module.
     * @return A list of [Permission] objects, each representing a specific permission required for module actions.
     */
    fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.settings.open".lowercase(),
                "Allows use of the settings command",
                PermissionDefault.OP
            )
        )
    }

    @Suppress("UnstableApiUsage")
    fun dialog(): Dialog {
        val modules = ModuleManager.modules
        val buttons = modules.map { module ->
            val moduleName = module::class.simpleName.toString()
            val key = "${instance::class.simpleName.toString()}.dialog.${moduleName}".lowercase()
            instance.logger.info("SettingsManager: $key")
            ActionButton.builder(moduleName.fireFmt().mm())
                .action(
                    DialogAction.customClick(
                        Key.key(key),
                        null
                    )
                )
                .build()
        }

        return Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder("<b>VanillaPlus Settings</b>".fireFmt().mm())
                        .canCloseWithEscape(true)
                        .build()
                )
                .type(
                    DialogType.multiAction(buttons)
                        .columns(4)
                        .build()
                )
        }
    }

    @Suppress("UnstableApiUsage")
    fun moduleDialogFor(module: ModuleInterface<*>): Dialog {
        val moduleName = module::class.simpleName.toString().lowercase()
        val config = module.config
        return Dialog.create { builder ->
            val inputs = mutableListOf<DialogInput>()
            (config as Any).javaClass.declaredFields.forEach { field ->
                field.isAccessible = true
                val value = field.get(config)
                val input = when (field.type) {
                    Boolean::class.java -> {
                        DialogInput.bool(
                            "${instance::class.simpleName}.${moduleName}.${field.name}",
                            field.name.fireFmt().mm()
                        ).initial(value as Boolean).build()
                    }

                    String::class.java -> {
                        DialogInput.text(
                            "${instance::class.simpleName}.${moduleName}.${field.name}",
                            field.name.fireFmt().mm()
                        ).initial(value as String).build()
                    }

                    else -> null
                }
                input?.let { inputs.add(it) }
            }

            builder.empty()
                .base(
                    DialogBase.builder("<b>$moduleName</b>".fireFmt().mm())
                        .inputs(inputs)
                        .build()
                )
                .type(
                    DialogType.confirmation(
                        ActionButton.builder("Save".mm())
                            .action(
                                DialogAction.customClick(
                                    Key.key("${instance::class.simpleName.toString().lowercase()}.dialog.save"),
                                    null
                                )
                            )
                            .build(),
                        ActionButton.builder("Cancel".mm()).build()
                    )
                )
        }
    }
}