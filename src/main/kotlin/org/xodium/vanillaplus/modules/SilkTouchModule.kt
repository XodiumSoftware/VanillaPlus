package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling silk touch mechanics within the system. */
internal class SilkTouchModule : ModuleInterface<SilkTouchModule.Config> {
    override val config: Config = Config()

    private val spawnerKey = NamespacedKey(instance, "spawner_type")

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        if (!enabled() ||
            event.player.gameMode != GameMode.SURVIVAL ||
            !isValidTool(event.player.inventory.itemInMainHand)
        ) {
            return
        }

        when (event.block.type) {
            Material.SPAWNER -> handleSpawnerBreak(event)
            Material.BUDDING_AMETHYST -> handleBuddingAmethystBreak(event)
            else -> return
        }
    }

    @EventHandler
    fun on(event: BlockPlaceEvent) {
        if (!enabled()) return

        val item = event.itemInHand
        val block = event.blockPlaced

        if (block.type != Material.SPAWNER) return

        val typeName = item.persistentDataContainer.get(spawnerKey, PersistentDataType.STRING) ?: return
        val spawner = block.state as CreatureSpawner

        spawner.spawnedType = EntityType.valueOf(typeName)
        spawner.update()
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
            val spawnedTypeName = state.spawnedType?.name ?: return
            @Suppress("UnstableApiUsage")
            event.block.world.dropItemNaturally(
                event.block.location,
                ItemStack.of(Material.SPAWNER).apply {
                    setData(
                        DataComponentTypes.LORE,
                        ItemLore.lore(
                            config.i18n.spawnerLore
                                .mm(
                                    Placeholder.component("spawner_type", spawnedTypeName.mm()),
                                ),
                        ),
                    )
                    editPersistentDataContainer {
                        it.set(
                            spawnerKey,
                            PersistentDataType.STRING,
                            spawnedTypeName,
                        )
                    }
                },
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
        item?.let { Tag.ITEMS_PICKAXES.isTagged(it.type) && it.containsEnchantment(Enchantment.SILK_TOUCH) } == true

    data class Config(
        var allowSpawnerSilk: Boolean = true,
        var allowBuddingAmethystSilk: Boolean = true,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var spawnerLore: List<String> = listOf("Type: <spawner_type>"),
        )
    }
}
