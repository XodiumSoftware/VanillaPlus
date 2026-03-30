package org.xodium.vanillaplus.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.runes.HealthRune

/**
 * Recipes for the Health Rune progression.
 *
 * Containers are crafted in a ring shape; iron/gold/diamond require the previous group's max rune in the center.
 * Combining a [HealthRune.GEM] with a container at a crafting table yields the base rune of that tier group.
 *
 * | Container | Material      | Center ingredient | Yields         |
 * |-----------|---------------|-------------------|----------------|
 * | Copper    | Copper Ingot  | —                 | Health Rune I  |
 * | Iron      | Iron Ingot    | Health Rune V     | Health Rune VI |
 * | Gold      | Gold Ingot    | Health Rune X     | Health Rune XI |
 * | Diamond   | Diamond       | Health Rune XV    | Health Rune XVI|
 */
internal object HealthRuneRecipe : RecipeInterface {
    override val recipes =
        setOf(
            // --- Containers ---
            ShapedRecipe(
                NamespacedKey(instance, "health_container_copper_shaped_recipe"),
                HealthRune.CONTAINER_COPPER,
            ).apply {
                shape("CCC", "C C", "CCC")
                setIngredient('C', Material.COPPER_INGOT)
            },
            ShapedRecipe(
                NamespacedKey(instance, "health_container_iron_shaped_recipe"),
                HealthRune.CONTAINER_IRON,
            ).apply {
                shape("III", "IRI", "III")
                setIngredient('I', Material.IRON_INGOT)
                setIngredient('R', RecipeChoice.ExactChoice(HealthRune.tiers[4].item.clone()))
            },
            ShapedRecipe(
                NamespacedKey(instance, "health_container_gold_shaped_recipe"),
                HealthRune.CONTAINER_GOLD,
            ).apply {
                shape("GGG", "GRG", "GGG")
                setIngredient('G', Material.GOLD_INGOT)
                setIngredient('R', RecipeChoice.ExactChoice(HealthRune.tiers[9].item.clone()))
            },
            ShapedRecipe(
                NamespacedKey(instance, "health_container_diamond_shaped_recipe"),
                HealthRune.CONTAINER_DIAMOND,
            ).apply {
                shape("DDD", "DRD", "DDD")
                setIngredient('D', Material.DIAMOND)
                setIngredient('R', RecipeChoice.ExactChoice(HealthRune.tiers[14].item.clone()))
            },
            // --- Gem infusion: Gem + Container → base rune of that tier group ---
            ShapelessRecipe(
                NamespacedKey(instance, "health_rune_copper_shapeless_recipe"),
                HealthRune.tiers[0].item.clone(),
            ).apply {
                addIngredient(RecipeChoice.ExactChoice(HealthRune.GEM.clone()))
                addIngredient(RecipeChoice.ExactChoice(HealthRune.CONTAINER_COPPER.clone()))
            },
            ShapelessRecipe(
                NamespacedKey(instance, "health_rune_iron_shapeless_recipe"),
                HealthRune.tiers[5].item.clone(),
            ).apply {
                addIngredient(RecipeChoice.ExactChoice(HealthRune.GEM.clone()))
                addIngredient(RecipeChoice.ExactChoice(HealthRune.CONTAINER_IRON.clone()))
            },
            ShapelessRecipe(
                NamespacedKey(instance, "health_rune_gold_shapeless_recipe"),
                HealthRune.tiers[10].item.clone(),
            ).apply {
                addIngredient(RecipeChoice.ExactChoice(HealthRune.GEM.clone()))
                addIngredient(RecipeChoice.ExactChoice(HealthRune.CONTAINER_GOLD.clone()))
            },
            ShapelessRecipe(
                NamespacedKey(instance, "health_rune_diamond_shapeless_recipe"),
                HealthRune.tiers[15].item.clone(),
            ).apply {
                addIngredient(RecipeChoice.ExactChoice(HealthRune.GEM.clone()))
                addIngredient(RecipeChoice.ExactChoice(HealthRune.CONTAINER_DIAMOND.clone()))
            },
        )
}
