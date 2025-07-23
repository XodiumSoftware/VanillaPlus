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
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

internal object SettingsManager {

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun on(event: PlayerCustomClickEvent) {
        if (event.identifier != Key.key("${instance::class.simpleName}.dialog.save")) return

        event.dialogResponseView ?: return
        val connection = event.commonConnection

        if (connection !is PlayerGameConnection) return
        val player = connection.player

        //TODO: Here you would implement saving the modified values back to the config
        player.sendMessage("Settings saved!".mm())
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

    fun dialog(): Dialog {
        val modules = ModuleManager.modules
        val dialogKeys = mutableListOf<Key>()

        for (module in modules) {
            val moduleName = module::class.simpleName.toString().lowercase()
            val config = module.config
            val dialogKey = Key.key("${instance::class.simpleName}", "settings_$moduleName")

            @Suppress("UnstableApiUsage")
            val moduleDialog = Dialog.create { builder ->
                val inputs = mutableListOf<DialogInput>()
                config.javaClass.declaredFields.forEach { field ->
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
                        DialogBase.builder(moduleName.fireFmt().mm())
                            .inputs(inputs)
                            .build()
                    )
                    .type(
                        DialogType.confirmation(
                            ActionButton.builder("Save".mm())
                                .action(
                                    DialogAction.customClick(
                                        Key.key("${instance::class.simpleName}.dialog.save"),
                                        null
                                    )
                                )
                                .build(),
                            ActionButton.builder("Cancel".mm())
                                .build()
                        )
                    )
            }
            instance.server.dialogRegistry.register(dialogKey, moduleDialog)
            dialogKeys.add(dialogKey)
        }

        @Suppress("UnstableApiUsage")
        return Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder("VanillaPlus Settings".fireFmt().mm())
                        .canCloseWithEscape(true)
                        .build()
                )
                .type(
                    DialogType.dialogList(RegistrySet.keySet(*dialogKeys.toTypedArray()))
                        .columns(4)
                        .buttonWidth(160)
                        .build()
                )
        }
    }
}