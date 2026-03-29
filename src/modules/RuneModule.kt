@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

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
                    .playerExecuted { player, _ -> RuneMenu.open(player) },
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
        )

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val entity = event.entity

        if (entity !is ElderGuardian && entity !is Wither && entity !is EnderDragon) return
        if (Random.nextDouble() < Config.runeDropChance) event.drops.add(HealthRune.item)
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        applyRuneModifiers(event.player, event.player.runeSlots)
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
        applyRuneModifiers(player, slots)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return

        val clickedInv = event.clickedInventory ?: return

        if (clickedInv == event.view.topInventory) {
            if (event.cursor.type != Material.AIR && !isRune(event.cursor)) event.isCancelled = true
        } else if (event.isShiftClick) {
            val item = event.currentItem ?: return

            if (item.type != Material.AIR && !isRune(item)) event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryDragEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return
        if (event.rawSlots.any { it < 5 } && !isRune(event.oldCursor)) event.isCancelled = true
    }

    /** Returns `true` if the given [ItemStack] carries a rune PDC tag. */
    private fun isRune(item: ItemStack): Boolean = item.persistentDataContainer.has(RUNE_TYPE_KEY)

    private fun applyRuneModifiers(
        player: Player,
        slots: List<String>,
    ) {
        RUNES.forEach { rune -> rune.modifiers(player, slots.count { it == rune.id }) }
    }

    /** Represents the config of the module. */
    object Config {
        var runeDropChance: Double = 0.10
    }
}
