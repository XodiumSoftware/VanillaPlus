package org.xodium.vanillaplus.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface

/** Represents an object handling chainmail recipe implementation within the system. */
internal object ChainmailRecipe : RecipeInterface {
    override val recipes =
        setOf(
            ShapedRecipe(
                NamespacedKey(instance, "chainmail_helmet"),
                ItemStack.of(Material.CHAINMAIL_HELMET),
            ).apply {
                shape("AAA", "A A")
                setIngredient('A', Material.IRON_BARS)
            },
            ShapedRecipe(
                NamespacedKey(instance, "chainmail_chestplate"),
                ItemStack.of(Material.CHAINMAIL_CHESTPLATE),
            ).apply {
                shape("A A", "AAA", "AAA")
                setIngredient('A', Material.IRON_BARS)
            },
            ShapedRecipe(
                NamespacedKey(instance, "chainmail_leggings"),
                ItemStack.of(Material.CHAINMAIL_LEGGINGS),
            ).apply {
                shape("AAA", "A A", "A A")
                setIngredient('A', Material.IRON_BARS)
            },
            ShapedRecipe(
                NamespacedKey(instance, "chainmail_boots"),
                ItemStack.of(Material.CHAINMAIL_BOOTS),
            ).apply {
                shape("A A", "A A")
                setIngredient('A', Material.IRON_BARS)
            },
        )
}
