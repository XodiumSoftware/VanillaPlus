/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.Utils
import org.xodium.vanillaplus.utils.invunload.BlockUtils
import org.xodium.vanillaplus.utils.invunload.InvUtils
import org.xodium.vanillaplus.utils.invunload.PlayerUtils
import java.util.concurrent.CompletableFuture

/** Represents a module handling inv-search mechanics within the system. */
class InvSearchModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvSearchModule.ENABLED

    @Suppress("UnstableApiUsage")
    private val materialSuggestionProvider = SuggestionProvider<CommandSourceStack> { ctx, builder ->
        Material.entries
            .map { it.name.lowercase() }
            .filter { it.startsWith(builder.remaining.lowercase()) }
            .forEach { builder.suggest(it) }
        CompletableFuture.completedFuture(builder.build())
    }

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invsearch")
            .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
            .then(
                Commands.argument("radius", IntegerArgumentType.integer(1))
                    .then(
                        Commands.argument("material", StringArgumentType.word())
                            .suggests(materialSuggestionProvider)
                            .executes { ctx -> handleSearch(ctx) }
                    )
                    .executes { ctx -> handleSearch(ctx) }
            )
            .then(
                Commands.argument("material", StringArgumentType.word())
                    .suggests(materialSuggestionProvider)
                    .executes { ctx -> handleSearch(ctx) }
            )
            .executes { ctx -> handleSearch(ctx) }
    }

    @Suppress("UnstableApiUsage")
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val radius = runCatching { IntegerArgumentType.getInteger(ctx, "radius") }.getOrNull()
        val materialName = runCatching { StringArgumentType.getString(ctx, "material") }.getOrNull()
        val material =
            materialName?.let { Material.getMaterial(it.uppercase()) } ?: player.inventory.itemInMainHand.type
        if (material == Material.AIR) {
            player.sendActionBar("You must specify a valid material or hold something in your hand".fireFmt().mm())
            return 0
        }
        search(player, radius ?: 5, material)
        return 1
    }

    /**
     * Searches for chests within the specified radius of the player that contain the specified material.
     * @param player The player who initiated the search.
     * @param radius The radius within which to search for chests.
     * @param material The material to search for in the chests.
     */
    private fun search(player: Player, radius: Int, material: Material) {
        if (!Utils.cooldown(
                player,
                Config.InvSearchModule.COOLDOWN,
                NamespacedKey(instance, "${InvSearchModule::class.simpleName}:cooldown")
            )
        ) return

        val chests = BlockUtils.findChestsInRadius(player.location, radius)
            .filter { PlayerUtils.canPlayerUseChest(it, player) }

        if (chests.isEmpty()) {
            player.sendActionBar("No usable chests found for ${"$material".roseFmt()}".fireFmt().mm())
            return
        }

        val invUnloadModule = InvUnloadModule()
        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val affectedChests = chests.filter { block ->
            val inventory = (block.state as Container).inventory
            val holder = inventory.holder
            if (holder is DoubleChest) {
                val left = holder.leftSide
                if (!seenDoubleChests.add(left)) return@filter false
            }
            InvUtils.searchItemInContainers(material, inventory, invUnloadModule)
        }

        invUnloadModule.print(player)
        if (affectedChests.isEmpty()) {
            player.sendActionBar("No chests contain ${"$material".roseFmt()}".fireFmt().mm())
            return
        }

        affectedChests.forEach { invUnloadModule.chestEffect(it, player) }
        invUnloadModule.play(player, affectedChests)
    }
}