@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.RUNE_TYPE_KEY
import org.xodium.vanillaplus.menus.RuneMenu
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.runes.CrimsoniteRune
import org.xodium.vanillaplus.runes.ZephyriteRune
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import kotlin.random.Random

/** Represents a module handling rune mechanics within the system. */
internal object RuneModule : ModuleInterface {
    /** All registered runes across all tiers. Add new [RuneInterface] implementations here to activate them. */
    val RUNES: List<RuneInterface> = CrimsoniteRune.tiers + ZephyriteRune.tiers

    /** Runes that can drop from boss mobs. Edit this list to control what bosses drop. */
    private val DROPPABLE: List<RuneInterface> = listOf(CrimsoniteRune.tiers[0], ZephyriteRune.tiers[0])

    /** All items giveable via `/runes give`: every registered rune tier. */
    private val GIVE_ITEMS: Map<String, ItemStack> = RUNES.associate { it.id to it.item }

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
        val chance = Config.dropChances[event.entity.type] ?: return

        if (DROPPABLE.isNotEmpty() && Random.nextDouble() < chance) event.drops.add(DROPPABLE.random().item.clone())
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

    /** Returns the rune type identifier of [item], or `null` if it is not a rune. */
    private fun runeTypeOf(item: ItemStack): String? =
        item.persistentDataContainer.get(RUNE_TYPE_KEY, PersistentDataType.STRING)

    /** Returns the rune family of [item] derived from its type id, or `null` if it is not a rune. */
    private fun runeFamilyOf(item: ItemStack): String? = runeTypeOf(item)?.substringBeforeLast("_")

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

    /** Applies or removes each registered rune's modifier on [player] based on their current slots. */
    private fun applyRuneModifiers(player: Player) {
        RUNES.forEach { rune -> rune.modifiers(player, player.runeSlots.any { it == rune.id }) }
    }

    /** Represents the config of the module. */
    object Config {
        var dropChances: Map<EntityType, Double> =
            mapOf(
                EntityType.ELDER_GUARDIAN to 0.05,
                EntityType.WITHER to 0.10,
                EntityType.WARDEN to 0.15,
                EntityType.ENDER_DRAGON to 0.20,
            )
        var anvilCombineCost: Int = 5
    }
}
