@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import org.xodium.vanillaplus.enchantments.ReplantEnchantment

/** Main bootstrap class of the plugin. */
@Suppress("UnstableApiUsage", "Unused")
internal class VanillaPlusBootstrap : PluginBootstrap {
    companion object {
        const val INSTANCE = "vanillaplus"
        val REPLANT = ReplantEnchantment.key
        val ENCHANTS = setOf(REPLANT)
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.apply {
            registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                    val enchantableMiningTag = event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_MINING)
                    event.registry().apply {
                        register(REPLANT) { ReplantEnchantment.init(it).supportedItems(enchantableMiningTag) }
                    }
                },
            )
            registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
                event.registrar().apply {
                    addToTag(EnchantmentTagKeys.TRADEABLE, ENCHANTS)
                    addToTag(EnchantmentTagKeys.NON_TREASURE, ENCHANTS)
                    addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, ENCHANTS)
                }
            }
        }
    }
}
