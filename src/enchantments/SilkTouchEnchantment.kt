package org.xodium.vanillaplus.enchantments

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.modules.PlayerModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling silk touch enchantment implementation within the system. */
internal object SilkTouchEnchantment : EnchantmentInterface {
    override val guide by lazy {
        ItemStack.of(Material.SPAWNER).apply {
            editMeta {
                it.displayName(MM.deserialize("<!italic><b><gold>Silk Touch</gold></b>"))
                it.lore(
                    listOf(
                        MM.deserialize("<!italic><dark_gray>Slot: <gray>Pickaxe</gray></dark_gray>"),
                        MM.deserialize("<!italic>"),
                        MM.deserialize("<!italic><dark_aqua>Allows collecting spawners and</dark_aqua>"),
                        MM.deserialize("<!italic><dark_aqua>budding amethyst blocks.</dark_aqua>"),
                        MM.deserialize("<!italic>"),
                        MM.deserialize("<!italic><gray><i>Vanilla enchantment, extended behaviour.</i></gray>"),
                    ),
                )
            }
        }
    }

    private val config = PlayerModule.config.silkTouch

    /**
     * Handles breaking blocks with Silk Touch.
     * @param event The block break event.
     */
    fun silkTouch(event: BlockBreakEvent) {
        if (!isValidTool(event.player.inventory.itemInMainHand)) return

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
        event.expToDrop = 0

        val state = event.block.state

        if (state is CreatureSpawner) {
            event.block.world.dropItemNaturally(event.block.location, ItemStack.of(Material.SPAWNER))
            event.block.world.dropItemNaturally(
                event.block.location,
                ItemStack.of(Material.matchMaterial("${state.spawnedType?.name}_SPAWN_EGG") ?: return),
            )
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
        item?.let {
            Tag.ITEMS_PICKAXES.isTagged(it.type) && it.containsEnchantment(Enchantment.SILK_TOUCH)
        } ?: false
}
