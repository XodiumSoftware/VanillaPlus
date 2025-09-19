@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.destroystokyo.paper.ParticleBuilder
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv mechanics within the system. */
internal class InvModule : ModuleInterface<InvModule.Config> {
    override val config: Config = Config()

    private val unloads = ConcurrentHashMap<Location, MutableMap<Material, Int>>()
    private val lastUnloads = ConcurrentHashMap<UUID, List<Block>>()
    private val activeVisualizations = ConcurrentHashMap<UUID, MutableList<Int>>()

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
            player.sendActionBar(config.l18n.noMaterialSpecified.mm())
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
        activeVisualizations[player.uniqueId]?.let { taskIds ->
            taskIds.forEach { instance.server.scheduler.cancelTask(it) }
            activeVisualizations.remove(player.uniqueId)
        }

        val chests =
            findBlocksInRadius(player.location, config.searchRadius)
                .filter { it.state is Container }
                .filter { searchItemInContainers(material, (it.state as Container).inventory) }

        if (chests.isEmpty()) {
            return player.sendActionBar(
                config.l18n.noMatchingItems.mm(Placeholder.component("material", material.name.mm())),
            )
        }

        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val filteredChests =
            chests.filter { block ->
                val inventory = (block.state as Container).inventory
                val holder = inventory.holder
                if (holder is DoubleChest && !seenDoubleChests.add(holder.leftSide)) return@filter false
                true
            }

        val sortedChests = filteredChests.sortedBy { it.location.distanceSquared(player.location) }
        if (sortedChests.isEmpty()) return

        val closestChest = sortedChests.first()
        chestEffect(player, closestChest)
        laserEffectSchedule(player, listOf(closestChest))

        val otherChests = sortedChests.drop(1)
        if (otherChests.isNotEmpty()) {
            otherChests.forEach { chestEffect(player, it) }
            laserEffectSchedule(player, otherChests)
        }
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        val startSlot = 9
        val endSlot = 35
        val chests =
            findBlocksInRadius(player.location, config.unloadRadius)
                .filter { it.state is Container }
                .sortedBy { it.location.distanceSquared(player.location) }
        if (chests.isEmpty()) return player.sendActionBar(config.l18n.noNearbyChests.mm())

        val affectedChests = mutableListOf<Block>()
        for (block in chests) {
            val inv = (block.state as Container).inventory
            if (stuffInventoryIntoAnother(player, inv, true, startSlot, endSlot)) {
                affectedChests.add(block)
            }
        }

        if (affectedChests.isEmpty()) return player.sendActionBar(config.l18n.noItemsUnloaded.mm())

        player.sendActionBar(config.l18n.inventoryUnloaded.mm())
        lastUnloads[player.uniqueId] = affectedChests

        for (block in affectedChests) chestEffect(player, block)

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
     * @param inventory The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    private fun countInventoryContents(inventory: Inventory): Int = inventory.contents.filterNotNull().sumOf { it.amount }

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
        val item = ItemStack.of(material)
        val count = doesChestContainCount(destination, material)
        if (count > 0 && doesChestContain(destination, item)) {
            destination.location?.let { location -> protocolUnload(location, material, count) }
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
     * @param chests The list of chests to affect.
     */
    private fun laserEffectSchedule(
        player: Player,
        chests: List<Block>,
    ) {
        activeVisualizations.computeIfAbsent(player.uniqueId) { mutableListOf() }.add(
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                {
                    chests.forEach { searchEffect(player.location, it.location, Color.RED, 40) }
                },
                0L,
                2L,
            ),
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.forEach { instance.server.scheduler.cancelTask(it) }
                activeVisualizations.remove(player.uniqueId)
            },
            100L,
        )
    }

    /**
     * TODO
     */
    private fun searchEffect(
        startLocation: Location,
        endLocation: Location,
        color: Color,
        travelTicks: Int,
    ): ParticleBuilder =
        Particle.TRAIL
            .builder()
            .location(startLocation)
            .data(Particle.Trail(endLocation, color, travelTicks))
            .receivers(32, true)
            .spawn()

    /**
     * TODO
     */
    private fun chestEffect() {}

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
     * @param location The location to search from.
     * @param radius The radius to search within.
     * @return A list of blocks found within the radius.
     */
    fun findBlocksInRadius(
        location: Location,
        radius: Int,
    ): List<Block> {
        val searchArea = BoundingBox.of(location, radius.toDouble(), radius.toDouble(), radius.toDouble())
        return getChunksInBox(location.world, searchArea)
            .asSequence()
            .flatMap { it.tileEntities.asSequence() }
            .filterIsInstance<Container>()
            .filter { isRelevantContainer(it, location, radius) }
            .map { it.block }
            .toList()
    }

    /**
     * Helper function to determine if a block state is a relevant container.
     * @param blockState The block state to check. Must be a Container.
     * @param center The centre location of the search area.
     * @param radius The radius of the search area.
     * @return True if the block state is a relevant container, false otherwise.
     */
    private fun isRelevantContainer(
        blockState: BlockState,
        center: Location,
        radius: Int,
    ): Boolean {
        when {
            blockState !is Container || !MaterialRegistry.CONTAINER_TYPES.contains(blockState.type) -> return false
            blockState.location.distanceSquared(center) > radius * radius -> return false
            blockState.type == Material.CHEST -> {
                val blockAbove = blockState.block.getRelative(BlockFace.UP)
                if (blockAbove.type.isSolid && blockAbove.type.isOccluding) return false
            }
        }
        return true
    }

    /**
     * Check if a chest contains an item with matching enchantments.
     * @param inventory The inventory to check.
     * @param item The item to check for.
     * @return True if the chest contains the item, false otherwise.
     */
    fun doesChestContain(
        inventory: Inventory,
        item: ItemStack,
    ): Boolean =
        inventory.contents
            .asSequence()
            .filterNotNull()
            .any { it.type == item.type && hasMatchingEnchantments(item, it) }

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
     * Creates a chest effect for the specified block and player.
     * @param player The player to create the laser effect for.
     * @param block The block to create the laser effect towards.
     */
    fun chestEffect(
        player: Player,
        block: Block,
    ) = player.spawnParticle(Particle.CRIT, block.center(), 10, 0.0, 0.0, 0.0)

    /**
     * Unloads the specified amount of material from the given location.
     * @param location The location to unload from.
     * @param material The material to unload.
     * @param amount The amount of material to unload.
     */
    fun protocolUnload(
        location: Location,
        material: Material,
        amount: Int,
    ) {
        if (amount == 0) return
        unloads.computeIfAbsent(location) { mutableMapOf() }.merge(material, amount, Int::plus)
    }

    /**
     * Get the centre of a block.
     * @return The centre location of the block.
     */
    private fun Block.center(): Location {
        val loc = location.clone()
        val stateChest = state as? Chest ?: return loc.add(0.5, 1.0, 0.5)
        val holder = stateChest.inventory.holder as? DoubleChest
        if (holder != null) {
            val leftLoc = (holder.leftSide as? Chest)?.block?.location
            val rightLoc = (holder.rightSide as? Chest)?.block?.location
            if (leftLoc != null && rightLoc != null) {
                loc.x = (leftLoc.x + rightLoc.x) / 2.0
                loc.y = (leftLoc.y + rightLoc.y) / 2.0
                loc.z = (leftLoc.z + rightLoc.z) / 2.0
                return loc.add(0.5, 1.0, 0.5)
            }
        }
        return loc.add(0.5, 1.0, 0.5)
    }

    data class Config(
        override var enabled: Boolean = true,
        var searchRadius: Int = 25,
        var unloadRadius: Int = 25,
        var matchEnchantments: Boolean = true,
        var matchEnchantmentsOnBooks: Boolean = true,
        var soundOnUnload: SoundData =
            SoundData(
                BukkitSound.ENTITY_PLAYER_LEVELUP,
                Sound.Source.PLAYER,
            ),
        var scheduleInitDelayInTicks: Long = 5,
        var l18n: L18n = L18n(),
    ) : ModuleInterface.Config {
        data class L18n(
            var noMaterialSpecified: String = "You must specify a valid material or hold something in your hand".fireFmt(),
            var noChestsFound: String = "No usable chests found for ${"<material>".roseFmt()}".fireFmt(),
            var noMatchingItems: String = "No chests contain ${"<material>".roseFmt()}".fireFmt(),
            var noNearbyChests: String = "No chests found nearby".fireFmt(),
            var noItemsUnloaded: String = "No items were unloaded".fireFmt(),
            var inventoryUnloaded: String = "Inventory unloaded".fireFmt(),
        )
    }
}
