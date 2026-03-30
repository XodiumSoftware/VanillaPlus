@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.ElderGuardian
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
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
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.RUNE_FAMILY_KEY
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.RUNE_TYPE_KEY
import org.xodium.vanillaplus.menus.RuneMenu
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.runes.HealthRune
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import kotlin.random.Random

/** Represents a module handling rune mechanics within the system. */
internal object RuneModule : ModuleInterface {
    /** All registered runes across all tiers. Add new [RuneInterface] implementations here to activate them. */
    val RUNES: List<RuneInterface> = HealthRune.tiers

    /** All items giveable via `/runes give`: every rune tier plus the gem and containers. */
    private val GIVE_ITEMS: Map<String, ItemStack> =
        buildMap {
            RUNES.forEach { put(it.id, it.item) }
            put("health_gem", HealthRune.GEM)
            put("container_copper", HealthRune.CONTAINER_COPPER)
            put("container_iron", HealthRune.CONTAINER_IRON)
            put("container_gold", HealthRune.CONTAINER_GOLD)
            put("container_diamond", HealthRune.CONTAINER_DIAMOND)
        }

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
                                        GIVE_ITEMS.keys.forEach { builder.suggest(it) }
                                        builder.buildFuture()
                                    }.playerExecuted { player, ctx ->
                                        val id = StringArgumentType.getString(ctx, "rune")
                                        val item = GIVE_ITEMS[id] ?: return@playerExecuted

                                        player.inventory.addItem(item.clone())
                                    }.then(
                                        Commands
                                            .argument("target", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                instance.server.onlinePlayers.forEach { builder.suggest(it.name) }
                                                builder.buildFuture()
                                            }.executesCatching { ctx ->
                                                val id = StringArgumentType.getString(ctx, "rune")
                                                val item = GIVE_ITEMS[id] ?: return@executesCatching
                                                val targetName = StringArgumentType.getString(ctx, "target")
                                                val target =
                                                    instance.server.getPlayerExact(targetName)
                                                        ?: return@executesCatching

                                                target.inventory.addItem(item.clone())
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
        if (Random.nextDouble() < Config.runeDropChance) {
            event.drops.add(HealthRune.GEM.clone())
        }
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        applyRuneModifiers(event.player)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        if (event.view !in RuneMenu.openViews) return

        val player = event.player as? Player ?: return
        val slots =
            (0 until 5).map { i ->
                event.view.topInventory
                    .getItem(i)
                    ?.let { runeTypeOf(it) } ?: ""
            }

        player.runeSlots = slots
        applyRuneModifiers(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (event.view !in RuneMenu.openViews) return

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
        if (event.view !in RuneMenu.openViews) return

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

    /** Returns the rune type identifier of [item], or `null` if it is not a rune. */
    private fun runeTypeOf(item: ItemStack): String? =
        item.persistentDataContainer.get(RUNE_TYPE_KEY, PersistentDataType.STRING)

    /** Returns the rune family of [item], or `null` if it is not a rune. */
    private fun runeFamilyOf(item: ItemStack): String? =
        item.persistentDataContainer.get(RUNE_FAMILY_KEY, PersistentDataType.STRING)

    /** Returns `true` if the given [ItemStack] carries a rune PDC tag. */
    private fun isRune(item: ItemStack): Boolean = runeTypeOf(item) != null

    /**
     * Returns `true` if a rune of the same family as [rune] already occupies any slot in the top
     * inventory, ignoring slots in [excludeSlots] (e.g. the slot currently being replaced).
     */
    private fun isDuplicateRune(
        inventory: Inventory,
        rune: ItemStack,
        excludeSlots: Set<Int> = emptySet(),
    ): Boolean {
        val family = runeFamilyOf(rune) ?: return false
        return (0 until 5).any { slot ->
            slot !in excludeSlots && inventory.getItem(slot)?.let { runeFamilyOf(it) } == family
        }
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun on(event: PrepareAnvilEvent) {
        val first = event.inventory.getItem(0) ?: return
        val second = event.inventory.getItem(1) ?: return

        val firstType = runeTypeOf(first) ?: return
        if (firstType != runeTypeOf(second)) return

        val rune = RUNES.firstOrNull { it.id == firstType } ?: return
        val next = rune.nextTier() ?: return

        event.result = next.item.clone()
        event.view.repairCost = (RUNES.indexOf(rune) + 1) * Config.anvilCombineCost
    }

    /** Applies or removes each registered rune's modifier on [player] based on their current slots. */
    private fun applyRuneModifiers(player: Player) {
        RUNES.forEach { rune -> rune.modifiers(player, player.runeSlots.any { it == rune.id }) }
    }

    /** Represents the config of the module. */
    object Config {
        var runeDropChance: Double = 0.10
        var anvilCombineCost: Int = 5
    }
}
