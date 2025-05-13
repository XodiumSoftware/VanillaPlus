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
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
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
import org.xodium.vanillaplus.registries.EntityRegistry
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
     * A function to get the damage to an item stack against an entity type.
     * @param itemStack The item stack to get the damage to.
     * @param entityType The entity type to get the damage against.
     * @return The damage to the item stack against the entity type.
     */
    fun getDamage(itemStack: ItemStack?, entityType: EntityType): Double {
        val base = MaterialRegistry.BASE_DAMAGE_MAP[itemStack?.type ?: Material.AIR] ?: 0.0
        return if (base == 0.0) 0.0 else base + getBonus(itemStack, entityType)
    }

    /**
     * A function to get the bonus damage to an item stack against an entity type.
     * @param itemStack The item stack to get the bonus damage to.
     * @param entityType The entity type to get the bonus damage against.
     * @return The bonus damage to the item stack against the entity type.
     */
    private fun getBonus(itemStack: ItemStack?, entityType: EntityType): Double =
        itemStack?.itemMeta?.enchants?.entries?.sumOf { (enchantment, level) ->
            when (enchantment) {
                Enchantment.SHARPNESS -> 0.5 * level + 0.5
                Enchantment.BANE_OF_ARTHROPODS -> if (EntityRegistry.ARTHROPODS.contains(entityType)) 2.5 * level else 0.0
                Enchantment.SMITE -> if (EntityRegistry.UNDEAD.contains(entityType)) 2.5 * level else 0.0
                else -> 0.0
            }
        } ?: 0.0

    /**
     * A function to move bowls and bottles in an inventory.
     * @param inv The inventory to move the bowls and bottles in.
     * @param slot The slot to move the bowls and bottles from.
     * @return True if the bowls and bottles were moved successfully, false otherwise.
     */
    fun moveBowlsAndBottles(inv: Inventory, slot: Int): Boolean {
        val itemStack = inv.getItem(slot) ?: return false
        if (!MaterialRegistry.BOWL_OR_BOTTLE.contains(itemStack.type)) return false

        inv.clear(slot)

        val leftovers = inv.addItem(itemStack)
        if (inv.getItem(slot)?.amount == null ||
            inv.getItem(slot)?.amount == 0 ||
            inv.getItem(slot)?.type == Material.AIR
        ) return true

        if (leftovers.isNotEmpty()) {
            val holder = inv.holder
            if (holder !is Player) return false
            for (leftover in leftovers.values) {
                holder.world.dropItem(holder.location, leftover)
            }
            return false
        }

        for (i in 35 downTo 0) {
            if (inv.getItem(i)?.amount == null ||
                inv.getItem(i)?.amount == 0 ||
                inv.getItem(i)?.type == Material.AIR
            ) {
                inv.setItem(i, itemStack)
                return true
            }
        }
        return false
    }

    /**
     * A function to check if the inventory contains a specific item.
     * @param inventory The inventory to check.
     * @param predicate The predicate to check against the item type.
     * @return True if the inventory contains the item, false otherwise.
     */
    private fun inventoryContains(
        inventory: Array<ItemStack?>,
        predicate: (Material) -> Boolean
    ): Boolean {
        for (i in 0..<9) {
            val item = inventory[i] ?: continue
            if (predicate(item.type)) return true
        }
        return false
    }

    /**
     * A function to check if the inventory contains shears.
     * @param inventory The inventory to check.
     * @return True if the inventory contains shears, false otherwise.
     */
    fun hasShears(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it == Material.SHEARS }

    /**
     * A function to check if the inventory contains a sword.
     * @param inventory The inventory to check.
     * @return True if the inventory contains a sword, false otherwise.
     */
    fun hasSword(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it.name.endsWith("_SWORD") }

    /**
     * A function to check if the inventory contains a hoe.
     * @param inventory The inventory to check.
     * @return True if the inventory contains a hoe, false otherwise.
     */
    fun hasHoe(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it.name.endsWith("_HOE") }

    /**
     * A function to get the multiplier of an item stack.
     * @param itemStack The item stack to get the multiplier of.
     * @return The multiplier of the item stack.
     */
    fun getMultiplier(itemStack: ItemStack): Int {
        val base = getBaseMultiplier(itemStack)
        val itemMeta = itemStack.itemMeta ?: return base
        val efficiency = Enchantment.EFFICIENCY ?: return base
        if (!itemMeta.hasEnchant(efficiency)) return base
        val efficiencyLevel = itemMeta.getEnchantLevel(efficiency)
        return base + (efficiencyLevel * efficiencyLevel) + 1
    }

    /**
     * A function to get the base multiplier of an item stack.
     * @param itemStack The item stack to get the base multiplier of.
     * @return The base multiplier of the item stack.
     */
    private fun getBaseMultiplier(itemStack: ItemStack): Int {
        val itemName = itemStack.type.name
        return when {
            itemName.startsWith("DIAMOND") -> 8
            itemName.startsWith("IRON") -> 6
            itemName.startsWith("NETHERITE") -> 9
            itemName.startsWith("STONE") -> 4
            itemName.startsWith("WOOD") -> 2
            itemName.startsWith("GOLD") -> 12
            else -> 1
        }
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
     * Charges the player the specified amount of XP.
     * @param player The player to charge.
     * @param amount The amount of XP to charge.
     */
    fun chargePlayerXp(player: Player, amount: Int): Player {
        return player.apply {
            val remainingXp = maxOf(0, totalExperience - amount)
            totalExperience = 0
            level = 0
            exp = 0f
            if (remainingXp > 0) giveExp(remainingXp)
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
            player.sendActionBar(("You must wait before using this mechanic again".fireFmt()).mm())
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
    private fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
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
     * Get the amount of a specific material in a chest.
     * @param inventory The inventory to check.
     * @param material The material to count.
     * @return The amount of the material in the chest.
     */
    private fun doesChestContainCount(inventory: Inventory, material: Material): Int {
        return inventory.contents.filter { it?.type == material }.sumOf { it?.amount ?: 0 }
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
     * Searches for a specific item in the given inventory and its containers.
     * @param material The material to search for.
     * @param destination The inventory to search in.
     * @return True if the item was found in the inventory or its containers, false otherwise.
     */
    fun searchItemInContainers(material: Material, destination: Inventory): Boolean {
        if (doesChestContain(destination, ItemStack(material))) {
            val amount = doesChestContainCount(destination, material)
            destination.location?.let { protocolUnload(it, material, amount) }
            return true
        }
        return false
    }

    /**
     * Counts the total number of items in the given inventory.
     * @param inv The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    private fun countInventoryContents(inv: Inventory): Int = inv.contents.filterNotNull().sumOf { it.amount }

    /**
     * Moves items from the player's inventory to another inventory.
     * @param player The player whose inventory is being moved.
     * @param destination The destination inventory to move items into.
     * @param onlyMatchingStuff If true, only moves items that match the destination's contents.
     * @param startSlot The starting slot in the player's inventory to move items from.
     * @param endSlot The ending slot in the player's inventory to move items from.
     * @return True if items were moved, false otherwise.
     */
    fun stuffInventoryIntoAnother(
        player: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
    ): Boolean {
        val source = player.inventory
        val initialCount = countInventoryContents(source)
        var moved = false

        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue
            if (Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox) continue
            if (onlyMatchingStuff && !doesChestContain(destination, item)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }
            if (movedAmount > 0) {
                moved = true
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
                destination.location?.let { protocolUnload(it, item.type, movedAmount) }
            }
        }
        return moved && initialCount != countInventoryContents(source)
    }

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
     * Creates a chest effect for the specified block and player.
     * @param player The player to create the laser effect for.
     * @param block The block to create the laser effect towards.
     */
    fun chestEffect(player: Player, block: Block) {
        player.spawnParticle(Particle.CRIT, getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
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
     * Unloads the specified amount of material from the given location.
     * @param loc The location to unload from.
     * @param mat The material to unload.
     * @param amount The amount of material to unload.
     */
    private fun protocolUnload(loc: Location, mat: Material, amount: Int) {
        if (amount == 0) return
        unloads.computeIfAbsent(loc) { mutableMapOf() }.merge(mat, amount, Int::plus)
    }
}