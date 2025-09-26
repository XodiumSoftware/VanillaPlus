package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("kingdom")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            gui().open(it.sender as Player)
                        }
                    },
                "This command allows you to open the kingdom gui",
                listOf("k"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.kingdom".lowercase(),
                "Allows use of the kingdom command",
                PermissionDefault.TRUE,
            ),
        )

    private fun gui(): Gui =
        buildGui {
            containerType = hopper()
            spamPreventionDuration = config.guiSpamPreventionDuration
            title("Kingdom".mm()) // TODO: change to player kingdom name.
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Duration = 1.seconds,
    ) : ModuleInterface.Config
}
