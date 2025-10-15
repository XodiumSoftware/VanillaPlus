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
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage", "Unused")
/** Main bootstrap class of the plugin. */
internal class VanillaPlusBootstrap : PluginBootstrap {
    private val registryAccess = RegistryAccess.registryAccess()

    companion object {
        private const val INSTANCE = "vanillaplus"
        val DRIFT = Key.key(INSTANCE, "drift")
        val FORTITUDE = Key.key(INSTANCE, "fortitude")
        val NIMBUS = Key.key(INSTANCE, "nimbus")
        val ZEPHYR = Key.key(INSTANCE, "zephyr")
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                val registry = event.registry()
                registry.register(EnchantmentKeys.create(DRIFT)) { setDriftEnchantment(it, event) }
                registry.register(EnchantmentKeys.create(FORTITUDE)) { setFortitudeEnchantment(it, event) }
                registry.register(EnchantmentKeys.create(NIMBUS)) { setNimbusEnchantment(it, event) }
                registry.register(EnchantmentKeys.create(ZEPHYR)) { setZephyrEnchantment(it, event) }
            },
        )
    }

    /**
     * Retrieves the enchantment from the registry.
     * @param key The unique [Key] identifying the enchantment in the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun getEnchantment(key: Key): Enchantment =
        registryAccess.getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(RegistryKey.ENCHANTMENT.typedKey(key))

    /**
     * Configures the Drift enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @param event The registry compose event providing access to tags and registration context.
     */
    private fun setDriftEnchantment(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        builder
            .description(DRIFT.value().replaceFirstChar { it.uppercase() }.mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(2)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
    }

    /**
     * Configures the Fortitude enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @param event The registry compose event providing access to tags and registration context.
     */
    private fun setFortitudeEnchantment(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        builder
            .description(FORTITUDE.value().replaceFirstChar { it.uppercase() }.mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(4)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
    }

    /**
     * Configures the Nimbus enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @param event The registry compose event providing access to tags and registration context.
     */
    private fun setNimbusEnchantment(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        builder
            .description(NIMBUS.value().replaceFirstChar { it.uppercase() }.mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(3)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
            .exclusiveWith(
                RegistrySet.keySet(
                    RegistryKey.ENCHANTMENT,
                    setOf(RegistryKey.ENCHANTMENT.typedKey(ZEPHYR)),
                ),
            )
    }

    /**
     * Configures the Zephyr enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @param event The registry compose event providing access to tags and registration context.
     */
    private fun setZephyrEnchantment(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        builder
            .description(ZEPHYR.value().replaceFirstChar { it.uppercase() }.mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(5)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
            .exclusiveWith(
                RegistrySet.keySet(
                    RegistryKey.ENCHANTMENT,
                    setOf(RegistryKey.ENCHANTMENT.typedKey(NIMBUS)),
                ),
            )
    }
}
