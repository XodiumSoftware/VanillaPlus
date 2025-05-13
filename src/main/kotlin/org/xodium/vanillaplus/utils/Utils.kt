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
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/** General utilities. */
object Utils {
    private val chestDenyKey = NamespacedKey(instance, "denied_chests")
    val lastUnloads: ConcurrentHashMap<UUID, List<Block>> = ConcurrentHashMap()
    val activeVisualizations: ConcurrentHashMap<UUID, Int> = ConcurrentHashMap()
    private val unloads = ConcurrentHashMap<Location, MutableMap<Material, Int>>()

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
            (ctx.source.sender as Player).sendMessage("${PREFIX}<red>An Error has occurred. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }

    /**
     * A function to get the tps of the server.
     * @return The tps of the server.
     */
    fun getTps(): String {
        val tps = instance.server.tps[0]
        val clampedTps = tps.coerceIn(0.0, 20.0)
        val ratio = clampedTps / 20.0
        val color = getColorForTps(ratio)
        val formattedTps = String.format("%.1f", tps)
        return "<color:$color>$formattedTps</color>"
    }

    /**
     * Calculate a hex colour between red and green based on the provided ratio (0.0 to 1.0).
     * @param ratio The ratio to calculate the colour for.
     * @return The hex colour for the ratio.
     */
    private fun getColorForTps(ratio: Double): String {
        val clamped = ratio.coerceIn(0.0, 1.0)
        val r = (255 * (1 - clamped)).roundToInt()
        val g = (255 * clamped).roundToInt()
        return String.format("#%02X%02X%02X", r, g, 0)
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     * @return A formatted string representing the weather.
     */
    fun getWeather(): String {
        val world = instance.server.worlds[0]
        return when {
            world.isThundering -> "<red>\uD83C\uDF29<reset>"
            world.hasStorm() -> "<yellow>\uD83C\uDF26<reset>"
            else -> "<green>\uD83C\uDF24<reset>"
        }
    }

    /**
     * Checks if the player is on cooldown.
     * @param player The player to check.
     * @param cooldownDuration The cooldown duration in milliseconds.
     * @param key The NamespacedKey to use for the cooldown.
     * @return true if the player is not on cooldown, false otherwise.
     */
    fun cooldown(player: Player, cooldownDuration: Long, key: NamespacedKey): Boolean {
        val now = System.currentTimeMillis()
        val container = player.persistentDataContainer
        val last = container.get(key, PersistentDataType.LONG) ?: 0L
        return if (now >= last + cooldownDuration) {
            container.set(key, PersistentDataType.LONG, now)
            true
        } else {
            player.sendActionBar("You must wait before using this mechanic again".fireFmt().mm())
            false
        }
    }

    /**
     * Returns an EnumSet of the enum constants that match the provided regex list.
     * @param enumClass The class of the enum to search.
     * @param regexList A list of regex patterns to match against the enum constant names.
     * @return An EnumSet containing the matching enum constants.
     */
    fun <E : Enum<E>> getEnumsFromRegexList(enumClass: Class<E>, regexList: List<Regex>): EnumSet<E> {
        return enumClass.enumConstants
            ?.filter { constant -> regexList.any { it.matches(constant.name) } }
            ?.toCollection(EnumSet.noneOf(enumClass))
            ?: EnumSet.noneOf(enumClass)
    }

    /**
     * Checks if two ItemStacks have matching enchantments.
     * @param first The first ItemStack.
     * @param second The second ItemStack.
     * @return True if the enchantments match, false otherwise.
     */
    private fun hasMatchingEnchantments(first: ItemStack, second: ItemStack): Boolean {
        val config = Config.InvUnloadModule

        if (!config.MATCH_ENCHANTMENTS && (!config.MATCH_ENCHANTMENTS_ON_BOOKS || first.type != Material.ENCHANTED_BOOK)) return true

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
            if (block.type.name.contains("CHEST")) {
                denyChestAccess(player, block)
            } else {
                allowChestAccess(player, block)
            }
        }
    }

    /**
     * Allows the player access to the chest at the given block.
     * @param player The player to allow access.
     * @param block The block to allow access to.
     */
    private fun allowChestAccess(player: Player, block: Block) {
        val container = player.persistentDataContainer
        val locString = block.location.serialize().toString()
        val denied = container.get(chestDenyKey, PersistentDataType.STRING) ?: return
        val updated = denied.split(";").filter { it != locString }.joinToString(";")
        if (updated.isEmpty()) {
            container.remove(chestDenyKey)
        } else {
            container.set(chestDenyKey, PersistentDataType.STRING, updated)
        }
    }

    /**
     * Denies the player access to the chest at the given block.
     * @param player The player to deny access.
     * @param block The block to deny access to.
     */
    private fun denyChestAccess(player: Player, block: Block) {
        val container = player.persistentDataContainer
        val locString = block.location.serialize().toString()
        val denied = container.get(chestDenyKey, PersistentDataType.STRING) ?: ""
        val updated = if (denied.isEmpty()) locString else "$denied;$locString"
        container.set(chestDenyKey, PersistentDataType.STRING, updated)
    }

    /**
     * Checks if the player can use the chest at the given block.
     * @param block The block to check.
     * @param player The player to check.
     * @return True if the player can use the chest, false otherwise.
     */
    fun canPlayerUseChest(block: Block?, player: Player?): Boolean {
        if (block == null || player == null) return false
        val container = player.persistentDataContainer
        val denied = container.get(chestDenyKey, PersistentDataType.STRING) ?: ""
        val locString = block.location.serialize().toString()
        return !denied.split(";").contains(locString)
    }

    /**
     * Find all blocks in a given radius from a location.
     * @param loc The location to search from.
     * @param radius The radius to search within.
     * @return A list of blocks found within the radius.
     */
    fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block> {
        val box = BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
        val chunks = getChunksInBox(loc.world, box)
        val radiusSq = radius * radius
        return chunks.flatMap { chunk ->
            chunk.tileEntities.filter { state ->
                state is Container &&
                        MaterialRegistry.CONTAINER_TYPES.contains(state.type) &&
                        state.location.distanceSquared(loc) <= radiusSq &&
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
    private fun getCenterOfBlock(block: Block): Location {
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

    //TODO: merge the 2 laser effect functions.
    /**
     * Creates a laser effect for the specified player and chests.
     * @param player The player to play the effect for.
     * @param affectedChests The list of chests to affect. If null, uses the last unloaded chests.
     */
    fun laserEffect(player: Player, affectedChests: List<Block>? = null) {
        val chests = affectedChests ?: lastUnloads[player.uniqueId] ?: return

        activeVisualizations[player.uniqueId] = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { laserEffect(chests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
            0L,
            2L
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.let {
                    instance.server.scheduler.cancelTask(it)
                    activeVisualizations.remove(player.uniqueId)
                }
            },
            TimeUtils.seconds(5)
        )
    }

    /**
     * Creates a laser effect between the player and the specified blocks.
     * @param destinations The list of blocks to create the laser effect towards.
     * @param player The player to create the laser effect for.
     * @param interval The interval between each particle spawn.
     * @param count The number of particles to spawn at each location.
     * @param particle The type of particle to spawn.
     * @param speed The speed of the particles.
     * @param maxDistance The maximum distance for the laser effect.
     */
    private fun laserEffect(
        destinations: List<Block>,
        player: Player,
        interval: Double,
        count: Int,
        particle: Particle,
        speed: Double,
        maxDistance: Int
    ) {
        destinations.forEach { destination ->
            val start = player.location.clone()
            val end = getCenterOfBlock(destination).add(0.0, -0.5, 0.0)
            val direction = end.toVector().subtract(start.toVector()).normalize()
            val distance = start.distance(destination.location)
            if (distance < maxDistance) {
                var i = 1.0
                while (i <= distance) {
                    val point = start.clone().add(direction.clone().multiply(i))
                    player.spawnParticle(particle, point, count, 0.0, 0.0, 0.0, speed)
                    i += interval
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