/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.managers.ChestAccessManager
import org.xodium.vanillaplus.modules.InvUnloadModule
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** General utilities. */
object Utils {
    private val unloads = ConcurrentHashMap<Location, MutableMap<Material, Int>>()
    val lastUnloads: ConcurrentHashMap<UUID, List<Block>> = ConcurrentHashMap()
    val activeVisualizations: ConcurrentHashMap<UUID, Int> = ConcurrentHashMap()

    /**
     * A helper function to wrap command execution with standardised error handling.
     * @param ctx The CommandContext used to get the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    @Suppress("UnstableApiUsage")
    fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (CommandSourceStack) -> Unit): Int {
        try {
            action(ctx.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occurred: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("$PREFIX <red>An Error has occurred. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }

    /**
     * Checks if two ItemStacks have matching enchantments.
     * @param first The first ItemStack.
     * @param second The second ItemStack.
     * @return True if the enchantments match, false otherwise.
     */
    private fun hasMatchingEnchantments(first: ItemStack, second: ItemStack): Boolean {
        val config = InvUnloadModule.Config()

        if (!config.matchEnchantments && (!config.matchEnchantmentsOnBooks || first.type != Material.ENCHANTED_BOOK)) return true

        val firstMeta = first.itemMeta
        val secondMeta = second.itemMeta

        if (firstMeta == null && secondMeta == null) return true
        if (firstMeta == null || secondMeta == null) return false

        if (firstMeta is EnchantmentStorageMeta && secondMeta is EnchantmentStorageMeta) {
            return firstMeta.storedEnchants == secondMeta.storedEnchants
        }

        if (!firstMeta.hasEnchants() && !secondMeta.hasEnchants()) return true
        if (firstMeta.hasEnchants() != secondMeta.hasEnchants()) return false

        return firstMeta.enchants == secondMeta.enchants
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        val player = event.player
        if (event.action == Action.RIGHT_CLICK_BLOCK && block != null) {
            val key = NamespacedKey(instance, "denied_chest")
            if (block.type.name.contains("CHEST")) {
                ChestAccessManager.deny(player, key, block)
            } else {
                ChestAccessManager.allow(player, key, block)
            }
        }
    }

    /**
     * Find all blocks in a given radius from a location.
     * @param loc The location to search from.
     * @param radius The radius to search within.
     * @return A list of blocks found within the radius.
     */
    fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block> {
        return getChunksInBox(
            loc.world,
            BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
        ).flatMap { chunk ->
            chunk.tileEntities.filter { state ->
                state is Container &&
                        MaterialRegistry.CONTAINER_TYPES.contains(state.type) &&
                        state.location.distanceSquared(loc) <= radius * radius &&
                        (state.type != Material.CHEST ||
                                !(state.block.getRelative(BlockFace.UP).type.isSolid &&
                                        state.block.getRelative(BlockFace.UP).type.isOccluding))
            }.map { (it as Container).block }
        }.toMutableList()
    }

    /**
     * Check if a chest contains an item with matching enchantments.
     * @param inv The inventory to check.
     * @param item The item to check for.
     * @return True if the chest contains the item, false otherwise.
     */
    fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
        return inv.contents.any { otherItem ->
            otherItem != null
                    && otherItem.type == item.type
                    && hasMatchingEnchantments(item, otherItem)
        }
    }

    /**
     * Get the center of a block.
     * @param block The block to get the center of.
     * @return The center location of the block.
     */
    fun getCenterOfBlock(block: Block): Location {
        val baseLoc = block.location.clone()
        val state = block.state
        val centerLoc = if (state is Chest && state.inventory.holder is DoubleChest) {
            val doubleChest = state.inventory.holder as? DoubleChest
            val left = (doubleChest?.leftSide as? Chest)?.block?.location
            val right = (doubleChest?.rightSide as? Chest)?.block?.location
            if (left != null && right != null) {
                left.clone().add(right).multiply(0.5)
            } else {
                baseLoc
            }
        } else {
            baseLoc
        }
        centerLoc.add(Vector(0.5, 1.0, 0.5))
        return centerLoc
    }

    /**
     * Get all chunks in a bounding box.
     * @param world The world to get chunks from.
     * @param box The bounding box to get chunks from.
     * @return A list of chunks in the bounding box.
     */
    private fun getChunksInBox(world: World, box: BoundingBox): List<Chunk> {
        val minChunkX = Math.floorDiv(box.minX.toInt(), 16)
        val maxChunkX = Math.floorDiv(box.maxX.toInt(), 16)
        val minChunkZ = Math.floorDiv(box.minZ.toInt(), 16)
        val maxChunkZ = Math.floorDiv(box.maxZ.toInt(), 16)
        return mutableListOf<Chunk>().apply {
            for (x in minChunkX..maxChunkX) {
                for (z in minChunkZ..maxChunkZ) {
                    if (world.isChunkLoaded(x, z)) {
                        add(world.getChunkAt(x, z))
                    }
                }
            }
        }
    }

    /**
     * Creates a chest effect for the specified block and player.
     * @param player The player to create the laser effect for.
     * @param block The block to create the laser effect towards.
     */
    fun chestEffect(player: Player, block: Block) {
        player.spawnParticle(Particle.CRIT, getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
    }

    /**
     * Unloads the specified amount of material from the given location.
     * @param loc The location to unload from.
     * @param mat The material to unload.
     * @param amount The amount of material to unload.
     */
    fun protocolUnload(loc: Location, mat: Material, amount: Int) {
        if (amount == 0) return
        unloads.computeIfAbsent(loc) { mutableMapOf() }.merge(mat, amount, Int::plus)
    }
}