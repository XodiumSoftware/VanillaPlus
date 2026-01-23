package org.xodium.vanillaplus.interfaces

import org.bukkit.inventory.Recipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.logging.Logger
import kotlin.time.measureTime

/** Represents a contract for recipes within the system. */
internal interface RecipeInterface {
    /**
     * Retrieves the set of recipes to be registered.
     * @return A [Collection] of [Recipe] instances.
     */
    val recipes: Collection<Recipe>

    /**
     * Registers all recipes returned by [recipes] with the server.
     * @return The time taken to register the recipes in milliseconds.
     */
    fun register(): Long = measureTime { recipes.forEach { instance.server.addRecipe(it) } }.inWholeMilliseconds

    /**
     * Logs the registration details of a list of recipes.
     * @receiver Logger The logger to use for logging.
     * @param recipes List of [RecipeInterface] instances to log.
     */
    fun Logger.info(recipes: List<RecipeInterface>) {
        info("Registered: ${recipes.sumOf { it.recipes.size }} recipes(s) | Took ${recipes.sumOf { it.register() }}ms")
    }
}
