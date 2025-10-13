package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryComposeEvent
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
internal class VanillaPlusBootstrap : PluginBootstrap {
    // FIX: usage of instance which is a late-init var will give null since we try to access it in the bootstrap,
    // FIX: while it wont be available till plugin load. So maybe moving the instance accessibility to here might be an option?
    private val nimbusKey = Key.key(instance, "nimbus")

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                event.registry().register(EnchantmentKeys.create(nimbusKey)) { b -> setNimbusEnchantment(b, event) }
            },
        )
    }

    private fun setNimbusEnchantment(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        builder
            .description("Nimbus".mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(1)
            .maxLevel(25)
            .weight(10)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
            .activeSlots(EquipmentSlotGroup.ANY)
    }

    fun getNimbusEnchantment(): Enchantment =
        RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT)
            .getOrThrow(RegistryKey.ENCHANTMENT.typedKey(nimbusKey))
}
