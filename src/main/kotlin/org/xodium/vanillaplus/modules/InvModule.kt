@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.BlockUtils.center
import org.xodium.vanillaplus.utils.ChunkUtils.filterAndSortContainers
import org.xodium.vanillaplus.utils.ChunkUtils.findContainersInRadius
import org.xodium.vanillaplus.utils.ChunkUtils.isContainerAccessible
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.glorpFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.InvUtils.transferItems
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
        activeVisualizations[player.uniqueId]?.let { taskIds ->
            taskIds.forEach { instance.server.scheduler.cancelTask(it) }
            activeVisualizations.remove(player.uniqueId)
        }

        val containers =
            findContainersInRadius(
                location = player.location,
                radius = config.searchRadius,
                containerTypes = MaterialRegistry.CONTAINER_TYPES,
                containerFilter = ::isRelevantContainer,
            )
        val matchingContainers =
            containers.filter { container ->
                val inventory = (container.state as Container).inventory
                inventory.contents.any { item ->
                    item?.type == material && hasMatchingEnchantments(ItemStack(material), item)
                }
            }

        if (matchingContainers.isEmpty()) {
            player.sendActionBar(
                config.i18n.noMatchingItems.mm(Placeholder.component("material", material.name.mm())),
            )
            return
        }

        val sortedChests = filterAndSortContainers(matchingContainers, player.location)

        if (sortedChests.isEmpty()) return

        val closestChest = sortedChests.first()

        schedulePlayerTask(player, {
            Particle.TRAIL
                .builder()
                .location(player.location)
                .data(Particle.Trail(closestChest.center(), Color.MAROON, 40))
                .receivers(player)
                .spawn()
            Particle.DUST
                .builder()
                .location(closestChest.center())
                .count(10)
                .data(Particle.DustOptions(Color.MAROON, 5.0f))
                .receivers(player)
                .spawn()
        })

        val otherChests = sortedChests.drop(1)

        if (otherChests.isNotEmpty()) {
            schedulePlayerTask(player, {
                otherChests.forEach {
                    Particle.TRAIL
                        .builder()
                        .location(player.location)
                        .data(Particle.Trail(it.center(), Color.RED, 40))
                        .receivers(player)
                        .spawn()
                    Particle.DUST
                        .builder()
                        .location(it.center())
                        .count(10)
                        .data(Particle.DustOptions(Color.RED, 5.0f))
                        .receivers(player)
                        .spawn()
                }
            })
        }
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        val startSlot = 9
        val endSlot = 35
        val containers =
            findContainersInRadius(
                location = player.location,
                radius = config.unloadRadius,
                containerTypes = MaterialRegistry.CONTAINER_TYPES,
                containerFilter = ::isRelevantContainer,
            )
        val sortedChests = filterAndSortContainers(containers, player.location)

        if (sortedChests.isEmpty()) {
            player.sendActionBar(config.i18n.noNearbyChests.mm())
            return
        }

        val affectedChests = mutableListOf<Block>()

        for (block in sortedChests) {
            val inv = (block.state as Container).inventory
            if (performUnload(player, inv, startSlot, endSlot)) affectedChests.add(block)
        }

        if (affectedChests.isEmpty()) {
            player.sendActionBar(config.i18n.noItemsUnloaded.mm())
            return
        }

        player.sendActionBar(config.i18n.inventoryUnloaded.mm())
        lastUnloads[player.uniqueId] = affectedChests

        for (chest in affectedChests) {
            Particle.DUST
                .builder()
                .location(chest.center())
                .count(10)
                .data(Particle.DustOptions(Color.LIME, 5.0f))
                .receivers(player)
                .spawn()
        }

        player.playSound(config.soundOnUnload.toSound(), Sound.Emitter.self())
    }

    /**
     * Transfers items from the specified player's inventory to the destination inventory within the given slot range.
     *
     * Only items matching certain criteria (e.g., matching enchantments) are transferred.
     *
     * @param player The player whose inventory items are to be transferred.
     * @param destination The inventory to which items will be transferred.
     * @param startSlot The starting slot index in the player's inventory to consider for transfer.
     * @param endSlot The ending slot index in the player's inventory to consider for transfer.
     * @return `true` if any items were successfully transferred, `false` otherwise.
     */
    private fun performUnload(
        player: Player,
        destination: Inventory,
        startSlot: Int,
        endSlot: Int,
    ): Boolean {
        val (success, itemsTransferred) =
            transferItems(
                source = player.inventory,
                destination = destination,
                startSlot = startSlot,
                endSlot = endSlot,
                onlyMatching = true,
                enchantmentChecker = ::hasMatchingEnchantments,
            )

        if (success && itemsTransferred > 0) {
            destination.location?.let { location -> unloads.computeIfAbsent(location) { mutableMapOf() } }
        }

        return success
    }

    /**
     * Schedules a repeating task for a specific player and automatically cancels it after a set duration.
     * @param player The player for whom the task is scheduled.
     * @param task The repeating task to execute.
     * @param initialDelay Ticks to wait before the first execution. Default is `0L`.
     * @param repeatDelay Ticks between each execution. Default is 2L.
     * @param durationTicks Total duration in ticks before the task is automatically cancelled. Default is `100L`.
     * @return The task ID of the scheduled repeating task.
     */
    private fun schedulePlayerTask(
        player: Player,
        task: () -> Unit,
        initialDelay: Long = 0L,
        repeatDelay: Long = 2L,
        durationTicks: Long = 100L,
    ): Int {
        val taskId =
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                task,
                initialDelay,
                repeatDelay,
            )

        activeVisualizations.computeIfAbsent(player.uniqueId) { mutableListOf() }.add(taskId)

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.remove(taskId)
                instance.server.scheduler.cancelTask(taskId)
            },
            durationTicks,
        )

        return taskId
    }

    /**
     * Checks if two ItemStacks have matching enchantments.
     * @param first The first ItemStack.
     * @param second The second ItemStack.
     * @return True if the enchantments match, false otherwise.
     */
    @Suppress("UnstableApiUsage")
    private fun hasMatchingEnchantments(
        first: ItemStack,
        second: ItemStack,
    ): Boolean {
        if (!config.matchEnchantments && (!config.matchEnchantmentsOnBooks || first.type != Material.ENCHANTED_BOOK)) return true
        // Early return if both items have no enchantments
        if (first.enchantments.isEmpty() && second.enchantments.isEmpty()) return true
        // Gets enchantments from the ItemStack Data.
        val firstEnchants = first.getData(DataComponentTypes.ENCHANTMENTS)
        val secondEnchants = second.getData(DataComponentTypes.ENCHANTMENTS)
        // Gets the stored enchantments from the ItemStack Data.
        val firstStoredEnchants = first.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        val secondStoredEnchants = second.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        // Compares the enchantments and stored enchantments between the 2 ItemStack Data's.
        return firstEnchants == secondEnchants && firstStoredEnchants == secondStoredEnchants
    }

    /**
     * Helper function to determine if a block is a relevant container.
     * @param block The block to check.
     * @return True if the block is a relevant container, false otherwise.
     */
    private fun isRelevantContainer(block: Block): Boolean {
        if (block.type == Material.CHEST) return isContainerAccessible(block)
        return true
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
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var noMaterialSpecified: String = "You must specify a valid material or hold something in your hand".fireFmt(),
            var noChestsFound: String = "No usable chests found for ${"<material>".roseFmt()}".fireFmt(),
            var noMatchingItems: String = "No chests contain ${"<material>".roseFmt()}".fireFmt(),
            var noNearbyChests: String = "No chests found nearby".fireFmt(),
            var noItemsUnloaded: String = "No items were unloaded".fireFmt(),
            var inventoryUnloaded: String = "Inventory unloaded".glorpFmt(),
        )
    }
}
