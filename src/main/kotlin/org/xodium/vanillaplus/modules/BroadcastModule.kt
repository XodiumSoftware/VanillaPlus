package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.FmtUtils.spellbiteFmt

/** Represents a module handling broadcasting mechanics within the system. */
internal class BroadcastModule : ModuleInterface<BroadcastModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("tips")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { broadcast(it.sender as Player) } },
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.tips".lowercase(),
                "Allows use of the tips command",
                PermissionDefault.TRUE,
            ),
        )

    private fun broadcast(player: Player) {
    }

    data class Config(
        override var enabled: Boolean = true,
        var tips: List<String> =
            listOf(
                "${"Signs can make use of ".spellbiteFmt()}<click:open_url:'https://docs.advntr.dev/minimessage/format.html'><hover:show_text:'${"Click Me!".fireFmt()}'>${"[<u>MiniMessage</u>]".roseFmt()}${" formatting!".spellbiteFmt()}",
            ),
    ) : ModuleInterface.Config
}
