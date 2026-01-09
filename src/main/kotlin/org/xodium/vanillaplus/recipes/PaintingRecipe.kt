package org.xodium.vanillaplus.recipes

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.StonecuttingRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling painting recipe implementation within the system. */
internal object PaintingRecipe : RecipeInterface {
    override val recipes =
        buildSet {
            val paintingRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT)

            paintingRegistry.forEach { variant ->
                val variantKey = paintingRegistry.getKey(variant) ?: return@forEach

                val paintingItem =
                    ItemStack(Material.PAINTING).apply {
                        editMeta { meta ->
                            meta.setEnchantmentGlintOverride(false)
                            meta.displayName(MM.deserialize(variant.key().value().replaceFirstChar { it.uppercase() }))
                        }
                    }

                add(
                    StonecuttingRecipe(
                        NamespacedKey(instance, "painting_${variantKey.value().replace(':', '_')}"),
                        paintingItem,
                        Material.PAINTING,
                    ),
                )
            }
        }
}
