@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.BlockUtils.center
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.PlayerUtils.getContainersAround
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling inv mechanics within the system. */
internal object InvModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("invsearch")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("material", ArgumentTypes.resource(RegistryKey.ITEM))
                            .playerExecuted { _, ctx -> handleSearch(ctx) },
                    ).executesCatching { handleSearch(it) },
                "Search nearby chests for specific items",
                listOf("search", "searchinv", "invs"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.invsearch".lowercase(),
                "Allows use of the invsearch command",
                PermissionDefault.TRUE,
            ),
        )

    /**
     * Handles the search command execution.
     * @param ctx The command context containing the command source and arguments.
     * @return An integer indicating the result of the command execution.
     */
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val material =
            runCatching { ctx.getArgument("material", Material::class.java) }.getOrNull()
                ?: player.inventory.itemInMainHand.type

        if (material == Material.AIR) {
            player.sendActionBar(MM.deserialize(config.invModule.i18n.noMaterialSpecified))
            return 0
        }

        search(player, material)
        return 1
    }

    /**
     * Searches for chests within the specified radius of the player that contain the specified material.
     * @param player The player who initiated the search.
     * @param material The material to search for in the chests.
     */
    private fun search(
        player: Player,
        material: Material,
    ) {
        val foundContainers = mutableListOf<Block>()

        for (container in player.getContainersAround()) {
            if (container.inventory.contains(material)) foundContainers.add(container.block)
        }

        if (foundContainers.isEmpty()) {
            player.sendActionBar(
                MM.deserialize(
                    config.invModule.i18n.noMatchingItems,
                    Placeholder.component("material", MM.deserialize(material.name)),
                ),
            )
            return
        }

        player.sendActionBar(
            MM.deserialize(
                config.invModule.i18n.foundItemsInChests,
                Placeholder.component("material", MM.deserialize(material.name)),
            ),
        )

        ScheduleUtils.schedule(duration = 200L) {
            foundContainers.forEach { container ->
                Particle.TRAIL
                    .builder()
                    .location(player.location)
                    .data(Particle.Trail(container.center(), Color.MAROON, 40))
                    .receivers(player)
                    .spawn()
                Particle.DUST
                    .builder()
                    .location(container.center())
                    .count(10)
                    .data(Particle.DustOptions(Color.MAROON, 5.0f))
                    .receivers(player)
                    .spawn()
            }
        }
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var noMaterialSpecified: String =
                "<gradient:#CB2D3E:#EF473A>You must specify a valid material " +
                    "or hold something in your hand</gradient>",
            var noMatchingItems: String =
                "<gradient:#CB2D3E:#EF473A>No containers contain " +
                    "<gradient:#F4C4F3:#FC67FA><b><material></b></gradient></gradient>",
            var foundItemsInChests: String =
                "<gradient:#FFE259:#FFA751>Found <gradient:#F4C4F3:#FC67FA><b><material></b></gradient> in container(s), follow trail(s)</gradient>",
        )
    }
}
