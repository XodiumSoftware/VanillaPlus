package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.REPLANT
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
internal object ReplantEnchantment : EnchantmentInterface {
    override val key: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, "replant"))

    override fun builder(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(REPLANT.value().replaceFirstChar { it.uppercase() }.mm())
            // TODO: Adjust costs and levels as needed
            .anvilCost(8)
            .maxLevel(1)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 10))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(8, 20))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Automatically replants a crop block after it has been fully grown and harvested.
     * @param block The block that was broken.
     * @param tool The tool used to break the block.
     */
    fun replant(
        block: Block,
        tool: ItemStack,
    ) {
        val ageable = block.blockData as? Ageable ?: return

        if (ageable.age < ageable.maximumAge) return
        if (!tool.hasItemMeta() || !tool.itemMeta.hasEnchant(get())) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val blockType = block.type

                block.type = blockType
                block.blockData = ageable.apply { age = 0 }
            },
            2,
        )
    }
}
