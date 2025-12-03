@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
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
import org.xodium.vanillaplus.utils.ExtUtils.executesCatching
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.InvUtils
import org.xodium.vanillaplus.utils.PlayerUtils
import org.xodium.vanillaplus.utils.ScheduleUtils
import java.util.concurrent.CompletableFuture

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
                            .argument("material", StringArgumentType.word())
                            .suggests { _, builder ->
                                Material.entries
                                    .map { it.name.lowercase() }
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.executesCatching {
                                if (it.source.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                handleSearch(it)
                            },
                    ).executesCatching { handleSearch(it) },
                "Search nearby chests for specific items",
                listOf("search", "searchinv", "invs"),
            ),
            CommandData(
                Commands
                    .literal("invunload")
                    .requires { it.sender.hasPermission(perms[1]) }
                    .executesCatching {
                        if (it.source.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                        unload(it.source.sender as Player)
                    },
                "Unload your inventory into nearby chests",
                listOf("unload", "unloadinv", "invu"),
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
     * Handles the search command execution.
     * @param ctx The command context containing the command source and arguments.
     * @return An integer indicating the result of the command execution.
     */
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val materialName = runCatching { StringArgumentType.getString(ctx, "material") }.getOrNull()
        val material =
            materialName?.let { Material.getMaterial(it.uppercase()) } ?: player.inventory.itemInMainHand.type

        if (material == Material.AIR) {
            player.sendActionBar(
                config.invFeature.i18n.noMaterialSpecified
                    .mm(),
            )
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

        for (container in PlayerUtils.getContainersAroundPlayer(player)) {
            if (container.inventory.contains(material)) foundContainers.add(container.block)
        }

        if (foundContainers.isEmpty()) {
            player.sendActionBar(
                config.invFeature.i18n.noMatchingItems.mm(
                    Placeholder.component(
                        "material",
                        material.name.mm(),
                    ),
                ),
            )
            return
        }

        player.sendActionBar(
            config.invFeature.i18n.foundItemsInChests
                .mm(Placeholder.component("material", material.name.mm())),
        )

        ScheduleUtils.schedule(duration = 200L) {
            foundContainers.forEach { container ->
                Particle.TRAIL
                    .builder()
                    .location(player.location)
                    .data(Particle.Trail(container.center, Color.MAROON, 40))
                    .receivers(player)
                    .spawn()
                Particle.DUST
                    .builder()
                    .location(container.center)
                    .count(10)
                    .data(Particle.DustOptions(Color.MAROON, 5.0f))
                    .receivers(player)
                    .spawn()
            }
        }
    }

    /**
     * Unloads items from the player's inventory into nearby chests.
     * @param player The player whose inventory is to be unloaded.
     */
    private fun unload(player: Player) {
        val foundContainers = mutableListOf<Block>()

        for (container in PlayerUtils.getContainersAroundPlayer(player)) {
            val transferred =
                InvUtils.transferItems(
                    source = player.inventory,
                    destination = container.inventory,
                    startSlot = 9,
                    endSlot = 35,
                    onlyMatching = true,
                    enchantmentChecker = { item1, item2 -> item1.enchantments == item2.enchantments },
                )

            if (transferred) foundContainers.add(container.block)
        }

        if (foundContainers.isEmpty()) {
            return player.sendActionBar(
                config.invFeature.i18n.noItemsUnloaded
                    .mm(),
            )
        }

        player.sendActionBar(
            config.invFeature.i18n.inventoryUnloaded
                .mm(),
        )
        player.playSound(config.invFeature.soundOnUnload.toSound(), Sound.Emitter.self())

        ScheduleUtils.schedule(duration = 60L) {
            foundContainers.forEach { container ->
                Particle.DUST
                    .builder()
                    .location(container.center)
                    .count(10)
                    .data(Particle.DustOptions(Color.LIME, 5.0f))
                    .receivers(player)
                    .spawn()
            }
        }
    }
}
