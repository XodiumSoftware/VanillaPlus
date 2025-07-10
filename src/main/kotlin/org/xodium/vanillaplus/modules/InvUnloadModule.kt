package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.CooldownManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.Utils
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface<InvUnloadModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("invunload")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { unload(it.sender as Player) } }
            ),
            "Allows players to unload their inventory into nearby chests.",
            listOf("unload", "unloadinv", "invu")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.invunload.use".lowercase(),
                "Allows use of the autorestart command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        val uuid = event.player.uniqueId
        Utils.lastUnloads.remove(uuid)
        Utils.activeVisualizations.remove(uuid)
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
        val chests = Utils.findBlocksInRadius(player.location, config.unloadRadius)
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
        Utils.lastUnloads[player.uniqueId] = affectedChests

        for (block in affectedChests) {
            Utils.chestEffect(player, block)
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
            if (onlyMatchingStuff && !Utils.doesChestContain(destination, item)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }
            if (movedAmount > 0) {
                moved = true
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
                destination.location?.let { Utils.protocolUnload(it, item.type, movedAmount) }
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

    data class Config(
        override var enabled: Boolean = true,
        var unloadRadius: Int = 5,
        var cooldown: Long = 1L * 1000L,
        var matchEnchantments: Boolean = true,
        var matchEnchantmentsOnBooks: Boolean = true,
        var soundOnUnload: SoundData = SoundData(
            BukkitSound.ENTITY_PLAYER_LEVELUP,
            Sound.Source.PLAYER
        ),
    ) : ModuleInterface.Config
}