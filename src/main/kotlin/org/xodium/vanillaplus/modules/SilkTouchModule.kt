package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling silk touch mechanics within the system. */
internal class SilkTouchModule : ModuleInterface<SilkTouchModule.Config> {
    override val config: Config = Config()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        if (!enabled()) return
        val player = event.player
        val block = event.block
        val tool = player.inventory.itemInMainHand

        if (!isValidTool(tool)) return

        when (block.type) {
            Material.SPAWNER -> {
                if (config.allowSpawnerSilk) {
                    event.isDropItems = false
                    block.world.dropItemNaturally(block.location, ItemStack.of(Material.SPAWNER))
                    println("DEBUG: ${player.name} mined a spawner with Silk Touch.")
                }
            }

            Material.BUDDING_AMETHYST -> {
                if (config.allowBuddingAmethystSilk) {
                    event.isDropItems = false
                    block.world.dropItemNaturally(block.location, ItemStack.of(Material.BUDDING_AMETHYST))
                    println("DEBUG: ${player.name} mined budding amethyst with Silk Touch.")
                }
            }

            else -> return
        }
    }

    /**
     * Checks if the item is a pickaxe with Silk Touch.
     * @param item The item to check.
     * @return `true` if the item is a pickaxe with Silk Touch, otherwise `false`.
     */
    private fun isValidTool(item: ItemStack?): Boolean {
        if (item == null) return false
        val type = item.type
        val isPickaxe = type.name.endsWith("_PICKAXE")
        val hasSilkTouch = item.containsEnchantment(Enchantment.SILK_TOUCH)
        return isPickaxe && hasSilkTouch
    }

    data class Config(
        override var enabled: Boolean = true,
        var allowSpawnerSilk: Boolean = true,
        var allowBuddingAmethystSilk: Boolean = true,
    ) : ModuleInterface.Config
}
