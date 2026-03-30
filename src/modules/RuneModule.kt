@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.ElderGuardian
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.menus.RuneMenu
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.runes.HealthRune
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import kotlin.random.Random

/** Represents a module handling rune mechanics within the system. */
internal object RuneModule : ModuleInterface {
    val RUNE_TYPE_KEY = NamespacedKey(instance, "rune_type")

    /** All registered runes. Add new [RuneInterface] implementations here to activate them. */
    val RUNES: List<RuneInterface> = listOf(HealthRune)

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("runes")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> RuneMenu.open(player) }
                    .then(
                        Commands
                            .literal("give")
                            .requires { it.sender.hasPermission(perms[1]) }
                            .then(
                                Commands
                                    .argument("rune", StringArgumentType.word())
                                    .suggests { _, builder ->
                                        RUNES.forEach { builder.suggest(it.id) }
                                        builder.buildFuture()
                                    }.playerExecuted { player, ctx ->
                                        val runeId = StringArgumentType.getString(ctx, "rune")
                                        val rune = RUNES.firstOrNull { it.id == runeId } ?: return@playerExecuted
                                        player.inventory.addItem(rune.item.clone())
                                    }.then(
                                        Commands
                                            .argument("target", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                instance.server.onlinePlayers.forEach { builder.suggest(it.name) }
                                                builder.buildFuture()
                                            }.executesCatching { ctx ->
                                                val runeId = StringArgumentType.getString(ctx, "rune")
                                                val rune =
                                                    RUNES.firstOrNull { it.id == runeId }
                                                        ?: return@executesCatching
                                                val targetName = StringArgumentType.getString(ctx, "target")
                                                val target =
                                                    instance.server.getPlayerExact(targetName)
                                                        ?: return@executesCatching
                                                target.inventory.addItem(rune.item.clone())
                                            },
                                    ),
                            ),
                    ),
                "Opens the rune equipment menu",
                listOf("r"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.rune".lowercase(),
                "Allows use of the rune command",
                PermissionDefault.TRUE,
            ),
            Permission(
                "${instance.javaClass.simpleName}.rune.give".lowercase(),
                "Allows giving runes to players",
                PermissionDefault.OP,
            ),
        )

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val entity = event.entity

        if (entity !is ElderGuardian && entity !is Wither && entity !is EnderDragon) return
        if (Random.nextDouble() < Config.runeDropChance) event.drops.add(HealthRune.item)
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        applyRuneModifiers(event.player)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return

        val player = event.player as? Player ?: return
        val slots =
            (0 until 5).map { i ->
                val item = event.view.topInventory.getItem(i) ?: return@map ""

                if (isRune(item)) {
                    item.persistentDataContainer.getOrDefault(RUNE_TYPE_KEY, PersistentDataType.STRING, "")
                } else {
                    ""
                }
            }

        player.runeSlots = slots
        applyRuneModifiers(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return

        val clickedInv = event.clickedInventory ?: return
        val top = event.view.topInventory

        if (clickedInv == top) {
            val cursor = event.cursor

            if (cursor.type != Material.AIR) {
                if (!isRune(cursor) || isDuplicateRune(top, cursor, excludeSlots = setOf(event.slot))) {
                    event.isCancelled = true
                }
            }
        } else if (event.isShiftClick) {
            val item = event.currentItem ?: return

            if (item.type != Material.AIR) {
                if (!isRune(item) || isDuplicateRune(top, item)) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryDragEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return

        val topSlots = event.rawSlots.filter { it < 5 }.toSet()

        if (topSlots.isEmpty()) return

        if (!isRune(event.oldCursor) ||
            isDuplicateRune(
                event.view.topInventory,
                event.oldCursor,
                excludeSlots = topSlots,
            )
        ) {
            event.isCancelled = true
        }
    }

    /** Returns `true` if the given [ItemStack] carries a rune PDC tag. */
    private fun isRune(item: ItemStack): Boolean = item.persistentDataContainer.has(RUNE_TYPE_KEY)

    /**
     * Returns `true` if a rune of the same type as [rune] already occupies any slot in the top
     * inventory, ignoring slots in [excludeSlots] (e.g. the slot currently being replaced).
     */
    private fun isDuplicateRune(
        inventory: Inventory,
        rune: ItemStack,
        excludeSlots: Set<Int> = emptySet(),
    ): Boolean =
        (0 until 5).any { slot ->
            slot !in excludeSlots &&
                inventory.getItem(slot)?.persistentDataContainer?.get(
                    RUNE_TYPE_KEY,
                    PersistentDataType.STRING,
                ) == (rune.persistentDataContainer.get(RUNE_TYPE_KEY, PersistentDataType.STRING) ?: return false)
        }

    private fun applyRuneModifiers(player: Player) {
        RUNES.forEach { rune -> rune.modifiers(player, player.runeSlots.any { it == rune.id }) }
    }

    /** Represents the config of the module. */
    object Config {
        var runeDropChance: Double = 0.10
    }
}
