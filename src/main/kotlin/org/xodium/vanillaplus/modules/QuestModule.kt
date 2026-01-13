package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.Serializable
import org.bukkit.inventory.Inventory
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("quests")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> player.openInventory(questUI) },
                "This command allows you to open the quests interface",
                listOf("q"),
            ),
        )

    override val perms =
        config.booksModule.books.map {
            Permission(
                "${instance.javaClass.simpleName}.quests".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE,
            )
        }

    val questUI: Inventory = TODO()

    private data class Quest(
        var difficulty: Difficulty,
    ) {
        private enum class Difficulty {
            EASY,
            MEDIUM,
            HARD,
        }

        private data class Requirement(
            var description: String,
        )

        private data class Reward(
            var description: String,
        )
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
