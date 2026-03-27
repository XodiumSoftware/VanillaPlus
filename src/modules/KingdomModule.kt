@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

/** Represents a module handling kingdom mechanics within the system. */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("kingdom")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ ->
                        player.uniqueId.toKotlinUuid().let { uuid ->
                            KingdomData.get(uuid)?.let { player.showDialog(it.dialog()) }
                                ?: KingdomData(uuid, "${player.name}'s Kingdom", listOf(uuid)).save()
                        }
                    },
                "Claim your kingdom",
                listOf("k"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.kingdom".lowercase(),
                "Allows use of the kingdom command",
                PermissionDefault.TRUE,
            ),
        )

    /** Returns a [Dialog] representing this [KingdomData], with the kingdom name as the title. */
    private fun KingdomData.dialog(): Dialog =
        Dialog.create { builder ->
            builder
                .empty()
                .base(
                    DialogBase
                        .builder(MM.deserialize(name))
                        .inputs(
                            listOf(
                                DialogInput
                                    .text("name", MM.deserialize("Rename Kingdom"))
                                    .maxLength(32)
                                    .build(),
                            ),
                        ).build(),
                ).type(
                    DialogType.confirmation(
                        ActionButton
                            .builder(MM.deserialize("<green>Save</green>"))
                            .action(
                                DialogAction.customClick(
                                    { response, _ ->
                                        copy(
                                            name =
                                                response
                                                    .getText("name")
                                                    ?.takeIf { it.isNotBlank() }
                                                    ?: return@customClick,
                                        ).save()
                                    },
                                    ClickCallback.Options.builder().build(),
                                ),
                            ).build(),
                        ActionButton.builder(MM.deserialize("<red>Cancel</red>")).build(),
                    ),
                )
        }
}
