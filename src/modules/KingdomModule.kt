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
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
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
                            (
                                KingdomData.get(uuid)
                                    ?: KingdomData(uuid, "${player.name}'s Kingdom", listOf(uuid)).also { it.save() }
                            ).let { player.showDialog(it.dialog()) }
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
                .base(DialogBase.builder(MM.deserialize(name)).build())
                .type(
                    DialogType
                        .multiAction(
                            buildList {
                                add(
                                    ActionButton
                                        .builder(MM.deserialize("Rename"))
                                        .action(
                                            DialogAction.customClick(
                                                { _, audience ->
                                                    (audience as? Player)?.showDialog(renameDialog())
                                                },
                                                ClickCallback.Options.builder().build(),
                                            ),
                                        ).build(),
                                )
                                members.forEach { uuid ->
                                    val memberName =
                                        instance.server.getOfflinePlayer(uuid.toJavaUuid()).name ?: uuid.toString()
                                    add(
                                        ActionButton
                                            .builder(MM.deserialize("Manage $memberName"))
                                            .action(
                                                DialogAction.customClick(
                                                    { _, audience ->
                                                        (audience as? Player)?.showDialog(memberDialog(uuid))
                                                    },
                                                    ClickCallback.Options.builder().build(),
                                                ),
                                            ).build(),
                                    )
                                }
                            },
                        ).exitAction(ActionButton.builder(MM.deserialize("<red>Cancel</red>")).build())
                        .build(),
                )
        }

    /** Returns a rename [Dialog] for this [KingdomData]. */
    private fun KingdomData.renameDialog(): Dialog =
        Dialog.create { builder ->
            builder
                .empty()
                .base(
                    DialogBase
                        .builder(MM.deserialize("Rename Kingdom"))
                        .inputs(
                            listOf(
                                DialogInput
                                    .text("name", MM.deserialize("New Name"))
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
                                    { response, audience ->
                                        val newName =
                                            response.getText("name")?.takeIf { it.isNotBlank() }
                                                ?: return@customClick
                                        copy(name = newName).also { it.save() }.let {
                                            (audience as? Player)?.showDialog(it.dialog())
                                        }
                                    },
                                    ClickCallback.Options.builder().build(),
                                ),
                            ).build(),
                        ActionButton.builder(MM.deserialize("<red>Cancel</red>")).build(),
                    ),
                )
        }

    /** Returns a member-management [Dialog] for the given [memberUuid] within this [KingdomData]. */
    private fun KingdomData.memberDialog(memberUuid: Uuid): Dialog =
        Dialog.create { builder ->
            val memberName = instance.server.getOfflinePlayer(memberUuid.toJavaUuid()).name ?: memberUuid.toString()
            builder
                .empty()
                .base(DialogBase.builder(MM.deserialize("Manage $memberName")).build())
                .type(
                    if (memberUuid == owner) {
                        DialogType.confirmation(
                            ActionButton
                                .builder(MM.deserialize("<red>Delete Kingdom</red>"))
                                .action(
                                    DialogAction.customClick(
                                        { _, _ -> delete() },
                                        ClickCallback.Options.builder().build(),
                                    ),
                                ).build(),
                            ActionButton.builder(MM.deserialize("<gray>Cancel</gray>")).build(),
                        )
                    } else {
                        DialogType
                            .multiAction(
                                listOf(
                                    ActionButton
                                        .builder(MM.deserialize("Transfer Ownership"))
                                        .action(
                                            DialogAction.customClick(
                                                { _, _ ->
                                                    delete()
                                                    copy(owner = memberUuid).save()
                                                },
                                                ClickCallback.Options.builder().build(),
                                            ),
                                        ).build(),
                                    ActionButton
                                        .builder(MM.deserialize("<red>Kick</red>"))
                                        .action(
                                            DialogAction.customClick(
                                                { _, _ -> copy(members = members - memberUuid).save() },
                                                ClickCallback.Options.builder().build(),
                                            ),
                                        ).build(),
                                ),
                            ).exitAction(ActionButton.builder(MM.deserialize("<gray>Cancel</gray>")).build())
                            .build()
                    },
                )
        }
}
