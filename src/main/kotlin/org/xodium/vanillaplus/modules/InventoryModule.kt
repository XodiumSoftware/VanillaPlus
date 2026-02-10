@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Container
import org.bukkit.block.Lidded
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.BlockUtils.center
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.PlayerUtils.getContainersAround
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling inventory mechanics within the system. */
internal object InventoryModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("search")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("material", ArgumentTypes.itemStack())
                            .playerExecuted { player, ctx ->
                                searchContainer(player, ctx.getArgument("material", ItemStack::class.java).type)
                            },
                    ).playerExecuted { player, _ -> searchContainer(player, player.inventory.itemInMainHand.type) },
                "Search nearby chests for specific items",
                listOf("invsearch", "searchinv", "invs", "sinv", "s"),
            ),
            CommandData(
                Commands
                    .literal("unload")
                    .requires { it.sender.hasPermission(perms[1]) }
                    .playerExecuted { player, _ -> unloadInventory(player) },
                "Unload inventory into nearby chests",
                listOf("invunload", "unloadinv", "invu", "uinv", "u"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.invsearch".lowercase(),
                "Allows use of the invsearch command",
                PermissionDefault.TRUE,
            ),
            Permission(
                "${instance.javaClass.simpleName}.invunload".lowercase(),
                "Allows use of the invunload command",
                PermissionDefault.TRUE,
            ),
        )

    /**
     * Searches for chests within the specified radius of the player that contain the specified material.
     * @param player The player who initiated the search.
     * @param material The material to search for in the chests.
     */
    private fun searchContainer(
        player: Player,
        material: Material,
    ) {
        if (material == Material.AIR) {
            player.sendActionBar(MM.deserialize(config.inventoryModule.i18n.noMaterialSpecified))
            return
        }

        val containers = player.getContainersAround().filter { it.inventory.contains(material) }

        if (containers.isEmpty()) {
            player.sendActionBar(
                MM.deserialize(
                    config.inventoryModule.i18n.noMatchingItems,
                    Placeholder.component("material", MM.deserialize(material.name)),
                ),
            )
            return
        }

        player.sendActionBar(
            MM.deserialize(
                config.inventoryModule.i18n.foundItemsInChests,
                Placeholder.component("material", MM.deserialize(material.name)),
            ),
        )

        ScheduleUtils.schedule(duration = 200L) {
            containers.forEach { container ->
                Particle.TRAIL
                    .builder()
                    .location(player.location)
                    .data(Particle.Trail(container.block.center(), Color.MAROON, 40))
                    .receivers(player)
                    .spawn()
                Particle.DUST
                    .builder()
                    .location(container.block.center())
                    .count(10)
                    .data(Particle.DustOptions(Color.MAROON, 5.0f))
                    .receivers(player)
                    .spawn()
            }
        }
    }

    /**
     * Moves items from the player's inventory into nearby containers.
     * @param player The player whose inventory items are to be unloaded into nearby containers.
     */
    private fun unloadInventory(player: Player) {
        val containers =
            player
                .getContainersAround()
                .filter { it.block.state is Lidded }
                .filter { container ->
                    container.inventory.storageContents.any { it == null || it.amount < it.maxStackSize }
                }

        if (containers.isEmpty()) {
            player.sendActionBar(MM.deserialize(config.inventoryModule.i18n.noContainersFound))
            return
        }

        val usedContainers = mutableSetOf<Container>()

        for (slot in 9..35) {
            val itemStack = player.inventory.getItem(slot) ?: continue

            if (itemStack.type.isAir) continue

            var remaining = itemStack

            val sortedContainers =
                containers.sortedByDescending { container ->
                    container.inventory.storageContents
                        .filterNotNull()
                        .filter { it.type == remaining.type }
                        .sumOf { it.amount }
                }

            for (container in sortedContainers) {
                if (remaining.amount <= 0) break

                val before = remaining.amount
                val leftovers = container.inventory.addItem(remaining.clone())

                remaining = leftovers.values.firstOrNull() ?: ItemStack.of(Material.AIR)

                if (remaining.amount < before) usedContainers.add(container)
            }

            if (remaining.type.isAir) {
                player.inventory.setItem(slot, null)
            } else if (remaining != itemStack) {
                player.inventory.setItem(slot, remaining)
            }
        }

        ScheduleUtils.schedule(duration = 20L) {
            usedContainers.forEach { container ->
                Particle.CRIT
                    .builder()
                    .location(container.block.center())
                    .count(10)
                    .receivers(player)
                    .spawn()
            }
        }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var i18n: I18n = I18n(),
    ) {
        /** Represents the internationalization strings for the module. */
        @Serializable
        data class I18n(
            var noMaterialSpecified: String =
                "<gradient:#CB2D3E:#EF473A>You must specify a valid material " +
                    "or hold something in your hand</gradient>",
            var noMatchingItems: String =
                "<gradient:#CB2D3E:#EF473A>No containers contain " +
                    "<gradient:#F4C4F3:#FC67FA><b><material></b></gradient></gradient>",
            var foundItemsInChests: String =
                "<gradient:#FFE259:#FFA751>Found <gradient:#F4C4F3:#FC67FA><b><material></b></gradient> " +
                    "in container(s), follow trail(s)</gradient>",
            var noContainersFound: String = "<gradient:#CB2D3E:#EF473A>No containers found nearby</gradient>",
        )
    }
}
