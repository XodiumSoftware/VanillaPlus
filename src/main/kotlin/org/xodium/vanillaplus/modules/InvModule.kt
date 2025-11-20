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
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.BlockUtils.center
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.PlayerUtils
import org.xodium.vanillaplus.utils.ScheduleUtils
import java.util.concurrent.CompletableFuture
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv mechanics within the system. */
internal class InvModule : ModuleInterface<InvModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("invsearch")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("material", StringArgumentType.word())
                            .suggests { _, builder ->
                                Material.entries
                                    .map { it.name.lowercase() }
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    handleSearch(ctx)
                                }
                            },
                    ).executes { ctx -> ctx.tryCatch { handleSearch(ctx) } },
                "Search nearby chests for specific items",
                listOf("search", "searchinv", "invs"),
            ),
            CommandData(
                Commands
                    .literal("invunload")
                    .requires { it.sender.hasPermission(perms()[1]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            unload(it.sender as Player)
                        }
                    },
                "Unload your inventory into nearby chests",
                listOf("unload", "unloadinv", "invu"),
            ),
        )

    override fun perms(): List<Permission> =
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
            player.sendActionBar(config.i18n.noMaterialSpecified.mm())
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
        val foundChests = mutableListOf<Block>()

        for (chest in PlayerUtils.getChestsAroundPlayer(player)) {
            if (chest.blockInventory.contains(material)) foundChests.add(chest.block)
        }

        if (foundChests.isNotEmpty()) {
            player.sendActionBar(
                "<gradient:#FFE259:#FFA751>Found ${"<gradient:#F4C4F3:#FC67FA><b>$material</b></gradient>"} in chest(s), follow trail(s)</gradient>"
                    .mm(),
            )

            ScheduleUtils.schedule(duration = 200L) {
                foundChests.forEach { chestBlock ->
                    Particle.TRAIL
                        .builder()
                        .location(player.location)
                        .data(Particle.Trail(chestBlock.center, Color.MAROON, 40))
                        .receivers(player)
                        .spawn()
                    Particle.DUST
                        .builder()
                        .location(chestBlock.center)
                        .count(10)
                        .data(Particle.DustOptions(Color.MAROON, 5.0f))
                        .receivers(player)
                        .spawn()
                }
            }
        } else {
            player.sendActionBar(config.i18n.noMatchingItems.mm(Placeholder.component("material", material.name.mm())))
        }
    }

    /**
     * Unloads items from the player's inventory into nearby chests.
     * @param player The player whose inventory is to be unloaded.
     */
    private fun unload(player: Player) {
        player.sendActionBar("<gradient:#CB2D3E:#EF473A>Feature not implemented yet!</gradient>".mm())
        // TODO
    }

    data class Config(
        var soundOnUnload: SoundData =
            SoundData(
                BukkitSound.ENTITY_PLAYER_LEVELUP,
                Sound.Source.PLAYER,
            ),
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var noMaterialSpecified: String =
                "<gradient:#CB2D3E:#EF473A>You must specify a valid material " +
                    "or hold something in your hand</gradient>",
            var noMatchingItems: String =
                "<gradient:#CB2D3E:#EF473A>No chests contain " +
                    "<gradient:#F4C4F3:#FC67FA><b><material></b></gradient></gradient>",
            var noItemsUnloaded: String = "<gradient:#CB2D3E:#EF473A>No items were unloaded</gradient>",
            var inventoryUnloaded: String = "<gradient:#B3E94A:#54F47F>Inventory unloaded</gradient>",
        )
    }
}
