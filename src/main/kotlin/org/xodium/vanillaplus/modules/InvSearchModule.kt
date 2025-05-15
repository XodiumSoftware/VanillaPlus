/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.Utils
import org.xodium.vanillaplus.utils.Utils.doesChestContain
import org.xodium.vanillaplus.utils.Utils.protocolUnload
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
    override fun cmd(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("invsearch")
                .requires { it.sender.hasPermission(Perms.InvSearch.USE) }
                .then(
                    Commands.argument("material", StringArgumentType.word())
                        .suggests(materialSuggestionProvider)
                        .executes { ctx -> handleSearch(ctx) }
                )
                .executes { ctx -> handleSearch(ctx) })
    }

    /**
     * Handles the search command execution.
     * @param ctx The command context containing the command source and arguments.
     * @return An integer indicating the result of the command execution.
     */
    @Suppress("UnstableApiUsage")
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val materialName = runCatching { StringArgumentType.getString(ctx, "material") }.getOrNull()
        val material =
            materialName?.let { Material.getMaterial(it.uppercase()) } ?: player.inventory.itemInMainHand.type
        if (material == Material.AIR) {
            player.sendActionBar("You must specify a valid material or hold something in your hand".fireFmt().mm())
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
    private fun search(player: Player, material: Material) {
        //TODO: fix cooldown not working when executing with material and without.
        if (!Utils.cooldown(
                player,
                Config.InvSearchModule.COOLDOWN,
                NamespacedKey(instance, "invsearch_cooldown")
            )
        ) return

        val chests = Utils.findBlocksInRadius(player.location, Config.InvSearchModule.SEARCH_RADIUS)
            .filter { Utils.canPlayerUseChest(it, player) }
        if (chests.isEmpty()) {
            player.sendActionBar("No usable chests found for ${"$material".roseFmt()}".fireFmt().mm())
            return
        }

        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val affectedChests = chests.filter { block ->
            val inventory = (block.state as Container).inventory
            val holder = inventory.holder
            if (holder is DoubleChest) {
                if (!seenDoubleChests.add(holder.leftSide)) return@filter false
            }
            searchItemInContainers(material, inventory)
        }
        if (affectedChests.isEmpty()) {
            player.sendActionBar("No chests contain ${"$material".roseFmt()}".fireFmt().mm())
            return
        }

        affectedChests.forEach { Utils.chestEffect(player, it) }
        Utils.laserEffect(player, affectedChests)
    }

    /**
     * Searches for a specific item in the given inventory and its containers.
     * @param material The material to search for.
     * @param destination The inventory to search in.
     * @return True if the item was found in the inventory or its containers, false otherwise.
     */
    private fun searchItemInContainers(material: Material, destination: Inventory): Boolean {
        if (doesChestContain(destination, ItemStack(material))) {
            destination.location?.let { protocolUnload(it, material, doesChestContainCount(destination, material)) }
            return true
        }
        return false
    }

    /**
     * Get the amount of a specific material in a chest.
     * @param inventory The inventory to check.
     * @param material The material to count.
     * @return The amount of the material in the chest.
     */
    private fun doesChestContainCount(inventory: Inventory, material: Material): Int {
        return inventory.contents.filter { it?.type == material }.sumOf { it?.amount ?: 0 }
    }
}