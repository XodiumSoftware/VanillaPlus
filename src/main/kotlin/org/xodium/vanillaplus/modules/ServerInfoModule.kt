package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import kotlinx.serialization.Serializable
import org.bukkit.ServerLinks
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.dialogs.FaqDialog
import org.xodium.vanillaplus.dialogs.FaqDialog.buildFaqItems
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import java.net.URI

/** Represents a module handling server info mechanics within the system. */
internal object ServerInfoModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("faq")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ ->
                        @Suppress("UnstableApiUsage")
                        val dialogBuilder =
                            Dialog.create { builder ->
                                builder
                                    .empty()
                                    .base(
                                        DialogBase
                                            .builder(MM.deserialize(config.serverInfoModule.faqDialogConfig.faqTitle))
                                            .body(buildFaqItems().map { item -> DialogBody.item(item).build() })
                                            .canCloseWithEscape(true)
                                            .build(),
                                    ).type(DialogType.notice())
                            }
                        player.showDialog(dialogBuilder)
                    },
                "Opens the FAQ interface",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.faq".lowercase(),
                "Allows use of the faq command",
                PermissionDefault.TRUE,
            ),
        )

    init {
        serverLinks()
    }

    /** Configures server links based on the module's configuration. */
    @Suppress("UnstableApiUsage")
    private fun serverLinks() =
        config.serverInfoModule.serverLinks.forEach { (type, url) ->
            runCatching { URI.create(url) }.getOrNull()?.let { instance.server.serverLinks.setLink(type, it) }
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        @Suppress("UnstableApiUsage") var serverLinks: Map<ServerLinks.Type, String> =
            mapOf(
                ServerLinks.Type.WEBSITE to "https://xodium.org/",
                ServerLinks.Type.REPORT_BUG to "https://github.com/XodiumSoftware/VanillaPlus/issues",
                ServerLinks.Type.STATUS to "https://mcsrvstat.us/server/illyria.xodium.org",
                ServerLinks.Type.COMMUNITY to "https://discord.gg/jusYH9aYUh",
            ),
        var faqDialogConfig: FaqDialog.Config = FaqDialog.Config(),
    )
}
