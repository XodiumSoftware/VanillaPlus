package org.xodium.vanillaplus.interfaces

import org.bukkit.inventory.Recipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import kotlin.time.measureTime

/** Represents a contract for recipes within the system. */
internal interface RecipeInterface {
    /**
     * Retrieves the set of recipes to be registered.
     * @return A set of [Recipe] instances.
     */
    val recipes: Set<Recipe>

    /** Registers all recipes returned by [recipes] with the server. */
    fun register() {
        instance.logger.info(
            "Registering: ${this::class.simpleName} | Took ${
                measureTime { recipes.forEach { recipe -> instance.server.addRecipe(recipe) } }.inWholeMilliseconds
            }ms",
        )
    }
}
