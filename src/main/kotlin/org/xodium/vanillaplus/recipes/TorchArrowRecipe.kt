package org.xodium.vanillaplus.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface

/** Represents an object handling torch arrow recipe implementation within the system. */
internal object TorchArrowRecipe : RecipeInterface {
    private val torchArrowKey = NamespacedKey(instance, "torch_arrow")

    override val recipes =
        setOf(
            ShapelessRecipe(
                torchArrowKey,
                ItemStack.of(Material.ARROW).apply {
                    editPersistentDataContainer { it.set(torchArrowKey, PersistentDataType.BYTE, 1) }
                },
            ).apply {
                addIngredient(Material.ARROW)
                addIngredient(Material.TORCH)
            },
        )
}
