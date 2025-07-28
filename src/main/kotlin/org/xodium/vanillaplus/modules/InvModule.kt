@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.CooldownManager
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.TimeUtils
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv mechanics within the system. */
internal class InvModule : ModuleInterface<InvModule.Config> {
    override val config: Config = Config()

    private val unloads = ConcurrentHashMap<Location, MutableMap<Material, Int>>()
    val lastUnloads: ConcurrentHashMap<UUID, List<Block>> = ConcurrentHashMap()
    val activeVisualizations: ConcurrentHashMap<UUID, Int> = ConcurrentHashMap()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("invsearch")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("material", StringArgumentType.word())
                            .suggests { ctx, builder ->
                                Material.entries
                                    .map { it.name.lowercase() }
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.executes { ctx -> ctx.tryCatch { handleSearch(ctx) } },
                    ).executes { ctx -> ctx.tryCatch { handleSearch(ctx) } },
                "Search nearby chests for specific items",
                listOf("search", "searchinv", "invs"),
            ),
            CommandData(
                Commands
                    .literal("invunload")
                    .requires { it.sender.hasPermission(perms()[1]) }
                    .executes { ctx -> ctx.tryCatch { unload(it.sender as Player) } },
                "Unload your inventory into nearby chests",
                listOf("unload", "unloadinv", "invu"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.invsearch".lowercase(),
                "Allows use of the invsearch command",
                PermissionDefault.TRUE,
            ),
            Permission(
                "${instance::class.simpleName}.invunload".lowercase(),
                "Allows use of the invunload command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        val uuid = event.player.uniqueId
        lastUnloads.remove(uuid)
        activeVisualizations.remove(uuid)
    }

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
    private fun search(
        player: Player,
        material: Material,
    ) {
        val cooldownKey = NamespacedKey(instance, "invsearch_cooldown")
        val cooldownDuration = config.cooldown
        if (CooldownManager.isOnCooldown(player, cooldownKey, cooldownDuration)) {
            return player.sendActionBar("You must wait before using this again.".fireFmt().mm())
        }
        CooldownManager.setCooldown(player, cooldownKey, System.currentTimeMillis())

        val chests =
            findBlocksInRadius(player.location, config.searchRadius)
                .filter { it.state is Container }
        if (chests.isEmpty()) {
            return player.sendActionBar("No usable chests found for ${"$material".roseFmt()}".fireFmt().mm())
        }

        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val affectedChests =
            chests.filter { block ->
                val inventory = (block.state as Container).inventory
                val holder = inventory.holder
                if (holder is DoubleChest) {
                    if (!seenDoubleChests.add(holder.leftSide)) return@filter false
                }
                searchItemInContainers(material, inventory)
            }
        if (affectedChests.isEmpty()) {
            return player.sendActionBar("No chests contain ${"$material".roseFmt()}".fireFmt().mm())
        }

        affectedChests.forEach { chestEffect(player, it) }
        laserEffectSchedule(player, affectedChests)
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        val cooldownKey = NamespacedKey(instance, "invunload_cooldown")
        val cooldownDuration = config.cooldown
        if (CooldownManager.isOnCooldown(player, cooldownKey, cooldownDuration)) {
            return player.sendActionBar("You must wait before using this again.".fireFmt().mm())
        }
        CooldownManager.setCooldown(player, cooldownKey, System.currentTimeMillis())

        val startSlot = 9
        val endSlot = 35
        val chests =
            findBlocksInRadius(player.location, config.unloadRadius)
                .filter { it.state is Container }
        if (chests.isEmpty()) {
            return player.sendActionBar("No chests found nearby".fireFmt().mm())
        }

        val affectedChests = mutableListOf<Block>()
        for (block in chests) {
            val inv = (block.state as Container).inventory
            if (stuffInventoryIntoAnother(player, inv, true, startSlot, endSlot)) {
                affectedChests.add(block)
            }
        }

        if (affectedChests.isEmpty()) {
            return player.sendActionBar("No items were unloaded".fireFmt().mm())
        }

        player.sendActionBar("Inventory unloaded".mangoFmt().mm())
        lastUnloads[player.uniqueId] = affectedChests

        for (block in affectedChests) {
            chestEffect(player, block)
        }

        player.playSound(config.soundOnUnload.toSound(), Sound.Emitter.self())
    }

    /**
     * Moves items from the player's inventory to another inventory.
     * @param player The player whose inventory is being moved.
     * @param destination The destination inventory to move items into.
     * @param onlyMatchingStuff If true, only moves items that match the destination's contents.
     * @param startSlot The starting slot in the player's inventory to move items from.
     * @param endSlot The ending slot in the player's inventory to move items from.
     * @return True if items were moved, false otherwise.
     */
    private fun stuffInventoryIntoAnother(
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
     * Counts the total number of items in the given inventory.
     * @param inv The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    private fun countInventoryContents(inv: Inventory): Int = inv.contents.filterNotNull().sumOf { it.amount }

    /**
     * Searches for a specific item in the given inventory and its containers.
     * @param material The material to search for.
     * @param destination The inventory to search in.
     * @return True if the item was found in the inventory or its containers, false otherwise.
     */
    private fun searchItemInContainers(
        material: Material,
        destination: Inventory,
    ): Boolean {
        if (doesChestContain(destination, ItemStack(material))) {
            destination.location?.let {
                protocolUnload(
                    it,
                    material,
                    doesChestContainCount(destination, material),
                )
            }
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
    private fun doesChestContainCount(
        inventory: Inventory,
        material: Material,
    ): Int = inventory.contents.filter { it?.type == material }.sumOf { it?.amount ?: 0 }

    /**
     * Creates a laser effect for the specified player and chests.
     * @param player The player to play the effect for.
     * @param affectedChests The list of chests to affect. If null, will use the last unloaded chests.
     */
    private fun laserEffectSchedule(
        player: Player,
        affectedChests: List<Block>? = null,
    ) {
        val chests = affectedChests ?: lastUnloads[player.uniqueId] ?: return

        activeVisualizations[player.uniqueId] =
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                { laserEffect(chests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
                0L,
                2L,
            )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.let {
                    instance.server.scheduler.cancelTask(it)
                    activeVisualizations.remove(player.uniqueId)
                }
            },
            TimeUtils.seconds(5),
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
        maxDistance: Int,
    ) {
        require(destinations.isNotEmpty()) { "Destinations list cannot be empty" }
        require(interval > 0) { "Interval must be positive" }
        require(count > 0) { "Count must be positive" }
        require(speed >= 0) { "Speed must be non-negative" }
        require(maxDistance > 0) { "Max distance must be positive" }

        val playerLocation = player.location

        destinations.forEach { destination ->
            val start = playerLocation.clone()
            val end = getCenterOfBlock(destination).add(0.0, -0.5, 0.0)
            val direction = end.toVector().subtract(start.toVector()).normalize()
            val distance = start.distance(destination.location)

            if (distance < maxDistance) {
                var currentDistance = 1.0
                val steps = (distance / interval).toInt()

                repeat(steps) {
                    val point = start.clone().add(direction.multiply(currentDistance))
                    player.spawnParticle(particle, point, count, 0.0, 0.0, 0.0, speed)
                    direction.normalize()
                    currentDistance += interval
                }
            }
        }
    }

    /**
     * Checks if two ItemStacks have matching enchantments.
     * @param first The first ItemStack.
     * @param second The second ItemStack.
     * @return True if the enchantments match, false otherwise.
     */
    private fun hasMatchingEnchantments(
        first: ItemStack,
        second: ItemStack,
    ): Boolean {
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

    /**
     * Find all blocks in a given radius from a location.
     * @param loc The location to search from.
     * @param radius The radius to search within.
     * @return A list of blocks found within the radius.
     */
    fun findBlocksInRadius(
        loc: Location,
        radius: Int,
    ): MutableList<Block> =
        getChunksInBox(
            loc.world,
            BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble()),
        ).flatMap { chunk ->
            chunk.tileEntities
                .filter { state ->
                    state is Container &&
                        MaterialRegistry.CONTAINER_TYPES.contains(state.type) &&
                        state.location.distanceSquared(loc) <= radius * radius &&
                        (
                            state.type != Material.CHEST ||
                                !(
                                    state.block
                                        .getRelative(BlockFace.UP)
                                        .type.isSolid &&
                                        state.block
                                            .getRelative(BlockFace.UP)
                                            .type.isOccluding
                                )
                        )
                }.map { (it as Container).block }
        }.toMutableList()

    /**
     * Check if a chest contains an item with matching enchantments.
     * @param inv The inventory to check.
     * @param item The item to check for.
     * @return True if the chest contains the item, false otherwise.
     */
    fun doesChestContain(
        inv: Inventory,
        item: ItemStack,
    ): Boolean =
        inv.contents.any { otherItem ->
            otherItem != null &&
                otherItem.type == item.type &&
                hasMatchingEnchantments(item, otherItem)
        }

    /**
     * Get all chunks in a bounding box.
     * @param world The world to get chunks from.
     * @param box The bounding box to get chunks from.
     * @return A list of chunks in the bounding box.
     */
    private fun getChunksInBox(
        world: World,
        box: BoundingBox,
    ): List<Chunk> {
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
     * Get the centre of a block.
     * @param block The block to get the centre of.
     * @return The centre location of the block.
     */
    fun getCenterOfBlock(block: Block): Location {
        val baseLoc = block.location.clone()
        val state = block.state
        val centerLoc =
            if (state is Chest && state.inventory.holder is DoubleChest) {
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
     * Creates a chest effect for the specified block and player.
     * @param player The player to create the laser effect for.
     * @param block The block to create the laser effect towards.
     */
    fun chestEffect(
        player: Player,
        block: Block,
    ) {
        player.spawnParticle(Particle.CRIT, getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
    }

    /**
     * Unloads the specified amount of material from the given location.
     * @param loc The location to unload from.
     * @param mat The material to unload.
     * @param amount The amount of material to unload.
     */
    fun protocolUnload(
        loc: Location,
        mat: Material,
        amount: Int,
    ) {
        if (amount == 0) return
        unloads.computeIfAbsent(loc) { mutableMapOf() }.merge(mat, amount, Int::plus)
    }

    data class Config(
        override var enabled: Boolean = true,
        var cooldown: Long = 1L * 1000L,
        var searchRadius: Int = 5,
        var unloadRadius: Int = 5,
        var matchEnchantments: Boolean = true,
        var matchEnchantmentsOnBooks: Boolean = true,
        var soundOnUnload: SoundData =
            SoundData(
                BukkitSound.ENTITY_PLAYER_LEVELUP,
                Sound.Source.PLAYER,
            ),
    ) : ModuleInterface.Config
}
