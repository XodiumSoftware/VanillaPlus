package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling silk touch mechanics within the system. */
internal class SilkTouchModule : ModuleInterface<SilkTouchModule.Config> {
    override val config: Config = Config()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        if (!enabled() || !isValidTool(event.player.inventory.itemInMainHand)) return

        when (event.block.type) {
            Material.SPAWNER -> handleSpawnerBreak(event)
            Material.BUDDING_AMETHYST -> handleBuddingAmethystBreak(event)
            else -> return
        }
    }

    /**
     * Handles breaking a spawner with Silk Touch.
     * @param event The block break event.
     */
    private fun handleSpawnerBreak(event: BlockBreakEvent) {
        if (!config.allowSpawnerSilk) return
        event.isDropItems = false

        val state = event.block.state
        if (state is CreatureSpawner) {
            val spawnerItem = ItemStack.of(Material.SPAWNER)
            val meta = spawnerItem.itemMeta as BlockStateMeta
            val spawnerState = meta.blockState as CreatureSpawner
            spawnerState.spawnedType = state.spawnedType
            spawnerState.update()
            meta.blockState = spawnerState
            spawnerItem.itemMeta = meta
            event.block.world.dropItemNaturally(event.block.location, spawnerItem)
        }
    }

    /**
     * Handles breaking a budding amethyst with Silk Touch.
     * @param event The block break event.
     */
    private fun handleBuddingAmethystBreak(event: BlockBreakEvent) {
        if (!config.allowBuddingAmethystSilk) return
        event.isDropItems = false
        event.block.world.dropItemNaturally(event.block.location, ItemStack.of(Material.BUDDING_AMETHYST))
    }

    /**
     * Checks if the item is a pickaxe with Silk Touch.
     * @param item The item to check.
     * @return `true` if the item is a pickaxe with Silk Touch, otherwise `false`.
     */
    private fun isValidTool(item: ItemStack?): Boolean =
        item?.let { Tag.ITEMS_PICKAXES.isTagged(it.type) && it.containsEnchantment(Enchantment.SILK_TOUCH) } == true

    data class Config(
        override var enabled: Boolean = true,
        var allowSpawnerSilk: Boolean = true,
        var allowBuddingAmethystSilk: Boolean = true,
    ) : ModuleInterface.Config
}
