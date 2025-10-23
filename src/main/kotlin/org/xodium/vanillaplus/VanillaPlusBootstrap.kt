package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import org.xodium.vanillaplus.enchantments.DriftEnchantment
import org.xodium.vanillaplus.enchantments.FortitudeEnchantment
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.enchantments.ZephyrEnchantment

/** Main bootstrap class of the plugin. */
@Suppress("UnstableApiUsage")
internal class VanillaPlusBootstrap : PluginBootstrap {
    companion object {
        const val INSTANCE = "vanillaplus"
        val DRIFT = DriftEnchantment.key
        val FORTITUDE = FortitudeEnchantment.key
        val NIMBUS = NimbusEnchantment.key
        val ZEPHYR = ZephyrEnchantment.key
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.apply {
            registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                    event.registry().apply {
                        register(DRIFT) { DriftEnchantment.set(it, event) }
                        register(FORTITUDE) { FortitudeEnchantment.set(it, event) }
                        register(NIMBUS) { NimbusEnchantment.set(it, event) }
                        register(ZEPHYR) { ZephyrEnchantment.set(it, event) }
                    }
                },
            )
            registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
                event.registrar().apply {
                    addToTag(EnchantmentTagKeys.TRADEABLE, setOf(DRIFT, FORTITUDE, NIMBUS, ZEPHYR))
                    addToTag(EnchantmentTagKeys.NON_TREASURE, setOf(DRIFT, FORTITUDE, NIMBUS, ZEPHYR))
                    addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, setOf(DRIFT, FORTITUDE, NIMBUS, ZEPHYR))
                }
            }
        }
    }
}
