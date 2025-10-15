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
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
/** Main bootstrap class of the plugin. */
internal class VanillaPlusBootstrap : PluginBootstrap {
    private val registryAccess = RegistryAccess.registryAccess()
    private val driftKey = Key.key("vanillaplus", "drift")
    private val fortitudeKey = Key.key("vanillaplus", "fortitude")
    private val nimbusKey = Key.key("vanillaplus", "nimbus")
    private val zephyrKey = Key.key("vanillaplus", "zephyr")

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                val registry = event.registry()
                registry.register(EnchantmentKeys.create(driftKey)) { b -> setDriftEnchantment(b, event) }
                registry.register(EnchantmentKeys.create(fortitudeKey)) { b -> setFortitudeEnchantment(b, event) }
                registry.register(EnchantmentKeys.create(nimbusKey)) { b -> setNimbusEnchantment(b, event) }
                registry.register(EnchantmentKeys.create(zephyrKey)) { b -> setZephyrEnchantment(b, event) }
            },
        )
    }

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
            .description("Drift".mm())
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
            .description("Fortitude".mm())
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
            .description("Nimbus".mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(3)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
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
            .description("Zephyr".mm())
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
            .anvilCost(2)
            .maxLevel(5)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)
    }

    /**
     * Retrieves the "Drift" enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the Drift key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun getDriftEnchantment(): Enchantment =
        registryAccess
            .getRegistry(RegistryKey.ENCHANTMENT)
            .getOrThrow(RegistryKey.ENCHANTMENT.typedKey(driftKey))

    /**
     * Retrieves the "Fortitude" enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the Fortitude key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun getFortitudeEnchantment(): Enchantment =
        registryAccess
            .getRegistry(RegistryKey.ENCHANTMENT)
            .getOrThrow(RegistryKey.ENCHANTMENT.typedKey(fortitudeKey))

    /**
     * Retrieves the "Nimbus" enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the Nimbus key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun getNimbusEnchantment(): Enchantment =
        registryAccess
            .getRegistry(RegistryKey.ENCHANTMENT)
            .getOrThrow(RegistryKey.ENCHANTMENT.typedKey(nimbusKey))

    /**
     * Retrieves the "Zephyr" enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the Zephyr key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun getZephyrEnchantment(): Enchantment =
        registryAccess
            .getRegistry(RegistryKey.ENCHANTMENT)
            .getOrThrow(RegistryKey.ENCHANTMENT.typedKey(zephyrKey))
}
