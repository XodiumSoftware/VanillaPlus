@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import org.xodium.vanillaplus.enchantments.PickupEnchantment
import org.xodium.vanillaplus.enchantments.ReplantEnchantment

/** Main bootstrap class of the plugin. */
@Suppress("UnstableApiUsage", "Unused")
internal class VanillaPlusBootstrap : PluginBootstrap {
    companion object {
        const val INSTANCE = "vanillaplus"
        val REPLANT = ReplantEnchantment.key
        val PICKUP = PickupEnchantment.key
        val ENCHANTS = setOf(REPLANT, PICKUP)
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.apply {
            registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                    val pickaxeTag = event.getOrCreateTag(ItemTypeTagKeys.PICKAXES)
                    val axeTag = event.getOrCreateTag(ItemTypeTagKeys.AXES)
                    val shovelTag = event.getOrCreateTag(ItemTypeTagKeys.SHOVELS)
                    val hoeTag = event.getOrCreateTag(ItemTypeTagKeys.HOES)

                    event.registry().apply {
                        register(REPLANT) { ReplantEnchantment.builder(it).supportedItems(hoeTag) }
                        register(PICKUP) {
                            PickupEnchantment
                                .builder(it)
                                .supportedItems(pickaxeTag)
                                .supportedItems(axeTag)
                                .supportedItems(shovelTag)
                                .supportedItems(hoeTag)
                        }
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
