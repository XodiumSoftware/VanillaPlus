@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.ItemTypeKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import io.papermc.paper.tag.TagEntry
import net.kyori.adventure.key.Key
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
        val TOOLS = TagKey.create(RegistryKey.ITEM, Key.key(INSTANCE, "tools"))
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.apply {
            registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM)) { event ->
                event.registrar().setTag(
                    TOOLS,
                    setOf(
                        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES),
                        TagEntry.tagEntry(ItemTypeTagKeys.AXES),
                        TagEntry.tagEntry(ItemTypeTagKeys.SHOVELS),
                        TagEntry.tagEntry(ItemTypeTagKeys.HOES),
                        TagEntry.valueEntry(ItemTypeKeys.SHEARS),
                    ),
                )
            }
            registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                    val hoeTag = event.getOrCreateTag(ItemTypeTagKeys.HOES)
                    val toolsTag = event.getOrCreateTag(TOOLS)

                    event.registry().apply {
                        register(REPLANT) { ReplantEnchantment.builder(it).supportedItems(hoeTag) }
                        register(PICKUP) { PickupEnchantment.builder(it).supportedItems(toolsTag) }
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
