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

@Suppress("UnstableApiUsage")
/** Represents the settings manager within the system. */
internal object SettingsManager : Listener {

    @EventHandler
    fun on(event: PlayerCustomClickEvent) {
        if (event.identifier.value().startsWith("${instance::class.simpleName.toString()}.dialog.".lowercase())) {
            val connection = event.commonConnection
            if (connection !is PlayerGameConnection) return
            val player = connection.player
            val moduleName = event.identifier.value().substringAfterLast(".")
            val module = ModuleManager.modules.first {
                it::class.simpleName.toString().lowercase() == moduleName.lowercase()
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
                    .executes { ctx -> ctx.tryCatch { (it.sender as Player).showDialog(settings()) } },
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

    /**
     * Creates the main settings dialog for VanillaPlus.
     * @return A [Dialog] instance presenting the VanillaPlus settings menu to the player.
     */
    fun settings(): Dialog {
        return Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.builder("<b>VanillaPlus Settings</b>".fireFmt().mm())
                        .canCloseWithEscape(true)
                        .build()
                )
                .type(
                    DialogType.multiAction(ModuleManager.modules.map { module ->
                        val moduleName = module::class.simpleName.toString()
                        ActionButton.builder(moduleName.fireFmt().mm())
                            .action(
                                DialogAction.customClick(
                                    Key.key(instance, ".dialog.${moduleName.lowercase()}"), null
                                )
                            )
                            .build()
                    })
                        .columns(4)
                        .build()
                )
        }
    }

    /**
     * Builds a configurable dialog interface for the given module.
     * @param module The module whose configuration should be displayed and edited.
     * @return A fully constructed [Dialog] allowing the player to view and modify the module's config.
     */
    fun moduleDialogFor(module: ModuleInterface<*>): Dialog {
        return Dialog.create { builder ->
            builder.empty()
                .base(buildDialogBase(module::class.simpleName.toString(), collectInputs(module)))
                .type(buildConfirmationType())
        }
    }

    /**
     * Collects dialog inputs for a module's configuration fields.
     * @param module The module instance providing the configuration.
     * @return A list of [DialogInput] representing the configurable values of the module.
     */
    private fun collectInputs(module: ModuleInterface<*>): List<DialogInput> {
        return module.config.javaClass.declaredFields.mapNotNull { field ->
            field.isAccessible = true
            val value = field.get(module.config)
            when (field.type) {
                Boolean::class.java -> DialogInput.bool("", field.name.fireFmt().mm())
                    .initial(value as Boolean).build()

                String::class.java, Int::class.java, Float::class.java, Double::class.java -> DialogInput.text(
                    "", field.name.fireFmt().mm()
                ).initial(value.toString()).build()

                else -> null
            }
        }
    }

    /**
     * Builds the [DialogBase] section for the module dialog.
     * @param moduleName The name of the module used as the dialog title.
     * @param inputs The list of [DialogInput] elements to be shown in the dialog.
     * @return A configured [DialogBase] instance for the dialog UI.
     */
    private fun buildDialogBase(moduleName: String, inputs: List<DialogInput>): DialogBase {
        return DialogBase.builder("<b>$moduleName</b>".fireFmt().mm())
            .inputs(inputs)
            .build()
    }

    /**
     * Builds the confirmation dialog type for saving or cancelling configuration changes.
     * @return A [DialogType] configured for confirmation with save and cancel actions.
     */
    private fun buildConfirmationType(): DialogType {
        return DialogType.confirmation(
            ActionButton.builder("Save".mm())
                .action(DialogAction.customClick(Key.key(instance, ".dialog.save"), null))
                .build(),
            ActionButton.builder("Cancel".mm()).build()
        )
    }
}