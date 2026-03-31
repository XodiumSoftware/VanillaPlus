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
import org.xodium.vanillaplus.data.RuneDropTableData
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
        val entry = Config.dropTable.firstOrNull { it.entityType == event.entity.type } ?: return
        val pool = entry.runes

        if (pool.isNotEmpty() && Random.nextDouble() < entry.chance) event.drops.add(pool.random().item.clone())
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        applyRuneModifiers(event.player)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        if (event.view !in RuneMenu.openViews) return

        val player = event.player as? Player ?: return
        val existing = player.runeSlots
        val slots =
            (0 until 5).map { i ->
                if (isSlotLocked(player, i)) {
                    existing[i]
                } else {
                    event.view.topInventory
                        .getItem(i)
                        ?.let { runeTypeOf(it) } ?: ""
                }
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
            val player = event.whoClicked as? Player ?: return

            if (isSlotLocked(player, event.slot)) {
                event.isCancelled = true
                return
            }

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

        val player = event.whoClicked as? Player ?: return

        if (topSlots.any { isSlotLocked(player, it) } ||
            !isRune(event.oldCursor) ||
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

    /** Returns `true` if [slot] in the rune menu is locked for [player] based on their XP level. */
    private fun isSlotLocked(
        player: Player,
        slot: Int,
    ): Boolean = player.level < Config.slotLevelRequirements.getOrElse(slot) { Int.MAX_VALUE }

    /** Applies or removes each registered rune's modifier on [player] based on their current slots and level. */
    private fun applyRuneModifiers(player: Player) {
        val slots = player.runeSlots
        val equippedIds =
            slots
                .mapIndexedNotNull { i, id -> id.takeIf { it.isNotEmpty() && !isSlotLocked(player, i) } }
                .toSet()

        RUNES.forEach { rune -> rune.modifiers(player, rune.id in equippedIds) }
    }

    private val all = listOf(CrimsoniteRune.tiers[0], ZephyriteRune.tiers[0])

    /** Represents the config of the module. */
    object Config {
        var dropTable: List<RuneDropTableData> =
            listOf(
                RuneDropTableData(EntityType.ELDER_GUARDIAN, 0.05, all),
                RuneDropTableData(EntityType.WITHER, 0.10, all),
                RuneDropTableData(EntityType.WARDEN, 0.15, all),
                RuneDropTableData(EntityType.ENDER_DRAGON, 0.20, all),
            )
        var anvilCombineCost: Int = 5
        val slotLevelRequirements: List<Int> = listOf(0, 10, 20, 30, 40)
    }
}
