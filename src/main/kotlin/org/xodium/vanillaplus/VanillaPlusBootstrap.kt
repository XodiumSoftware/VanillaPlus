@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.ItemTypeKeys
import io.papermc.paper.registry.keys.tags.DialogTagKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import io.papermc.paper.tag.TagEntry
import net.kyori.adventure.key.Key
import org.xodium.vanillaplus.dialogs.FaqDialog
import org.xodium.vanillaplus.enchantments.*

/** Main bootstrap class of the plugin. */
@Suppress("UnstableApiUsage")
internal class VanillaPlusBootstrap : PluginBootstrap {
    companion object {
        const val INSTANCE = "vanillaplus"

        val TOOLS = TagKey.create(RegistryKey.ITEM, Key.key(INSTANCE, "tools"))
        val WEAPONS = TagKey.create(RegistryKey.ITEM, Key.key(INSTANCE, "weapons"))
        val TOOLS_WEAPONS = TagKey.create(RegistryKey.ITEM, Key.key(INSTANCE, "tools_weapons"))
    }

    override fun bootstrap(ctx: BootstrapContext) {
        ctx.lifecycleManager.apply {
            registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM)) {
                it.registrar().apply {
                    setTag(
                        TOOLS,
                        setOf(
                            TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES),
                            TagEntry.tagEntry(ItemTypeTagKeys.AXES),
                            TagEntry.tagEntry(ItemTypeTagKeys.SHOVELS),
                            TagEntry.tagEntry(ItemTypeTagKeys.HOES),
                            TagEntry.valueEntry(ItemTypeKeys.SHEARS),
                            TagEntry.valueEntry(ItemTypeKeys.BRUSH),
                            TagEntry.valueEntry(ItemTypeKeys.FISHING_ROD),
                        ),
                    )
                    setTag(
                        WEAPONS,
                        setOf(
                            TagEntry.tagEntry(ItemTypeTagKeys.SWORDS),
                            TagEntry.valueEntry(ItemTypeKeys.BOW),
                            TagEntry.valueEntry(ItemTypeKeys.CROSSBOW),
                            TagEntry.valueEntry(ItemTypeKeys.TRIDENT),
                            TagEntry.valueEntry(ItemTypeKeys.MACE),
                            TagEntry.tagEntry(ItemTypeTagKeys.SPEARS),
                        ),
                    )
                    setTag(
                        TOOLS_WEAPONS,
                        setOf(
                            TagEntry.tagEntry(TOOLS),
                            TagEntry.tagEntry(WEAPONS),
                        ),
                    )
                }
            }
            registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                    event.registry().apply {
                        register(ReplantEnchantment.key) {
                            ReplantEnchantment
                                .invoke(it)
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HOES))
                        }
                        register(PickupEnchantment.key) {
                            PickupEnchantment
                                .invoke(it)
                                .supportedItems(event.getOrCreateTag(TOOLS_WEAPONS))
                        }
                        register(NightVisionEnchantment.key) {
                            NightVisionEnchantment
                                .invoke(it)
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                        }
                        register(NimbusEnchantment.key) {
                            NimbusEnchantment
                                .invoke(it)
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HARNESSES))
                        }
                        register(VeinMineEnchantment.key) {
                            VeinMineEnchantment
                                .invoke(it)
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.PICKAXES))
                        }
                    }
                },
            )
            registerEventHandler(
                RegistryEvents.DIALOG.compose().newHandler { event ->
                    event.registry().apply {
                        register(FaqDialog.key) { FaqDialog.invoke(it) }
                    }
                },
            )
            registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) {
                it.registrar().apply {
                    val enchants =
                        setOf(
                            ReplantEnchantment.key,
                            PickupEnchantment.key,
                            NightVisionEnchantment.key,
                            NimbusEnchantment.key,
                            VeinMineEnchantment.key,
                        )

                    addToTag(EnchantmentTagKeys.TRADEABLE, enchants)
                    addToTag(EnchantmentTagKeys.NON_TREASURE, enchants)
                    addToTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE, enchants)
                }
            }
            registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.DIALOG)) {
                it.registrar().apply {
                    addToTag(DialogTagKeys.QUICK_ACTIONS, setOf(FaqDialog.key))
                }
            }
        }
    }
}
