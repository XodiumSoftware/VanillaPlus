package org.xodium.vanillaplus.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling torch arrow recipe implementation within the system. */
internal object TorchArrowRecipe : RecipeInterface {
    val torchArrowKey = NamespacedKey(instance, "torch_arrow")
    val torchArrow =
        ItemStack.of(Material.ARROW).apply {
            @Suppress("UnstableApiUsage")
            setData(DataComponentTypes.CUSTOM_NAME, "Torch Arrow".mm())
            editPersistentDataContainer { it.set(torchArrowKey, PersistentDataType.BYTE, 1) }
        }

    override val recipes =
        setOf(
            ShapelessRecipe(torchArrowKey, torchArrow).apply {
                addIngredient(Material.ARROW)
                addIngredient(Material.TORCH)
            },
        )
}
