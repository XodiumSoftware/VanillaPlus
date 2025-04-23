/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents data related to a waystone in a Minecraft world, encapsulating information such as
 * identifier, display name, dimension, and physical location.
 *
 * This data class provides the structure necessary to uniquely identify and interact with
 * waystones within the game. It supports operations like calculating the cost of teleportation
 * between two locations.
 *
 * @property id A unique identifier (UUID) associated with the waystone.
 * @property customName The custom name of the waystone, which can be shown to players.
 * @property location The physical location of the waystone within the world.
 */
@OptIn(ExperimentalUuidApi::class)
data class WaystoneData(
    val id: Uuid = Uuid.random(),
    val customName: String = "Waystone",
    val location: Location,
) {
    companion object {
        private const val NS = "waystone"

        /**
         * Creates an ItemStack representing a Waystone with specific properties.
         *
         * This method generates a custom item with the given name, an optional origin, and
         * an optional destination. If both origin and destination are provided, the item
         * includes a lore line displaying the XP cost of traveling between the two locations.
         *
         * @param customName The custom name to set for the item.
         * @param origin The optional origin WaystoneData, representing the starting point.
         * @param destination The optional destination WaystoneData, representing the endpoint.
         * @return An ItemStack configured with the specified properties.
         */
        fun item(customName: String, origin: WaystoneData? = null, destination: WaystoneData? = null): ItemStack {
            return ItemStack(Config.WaystoneModule.WAYSTONE_MATERIAL).apply {
                editPersistentDataContainer {
                    if (origin != null) {
                        it.set(
                            NamespacedKey(instance, NS + "_id"),
                            PersistentDataType.STRING, origin.id.toString()
                        )
                        it.set(
                            NamespacedKey(instance, NS + "_name"),
                            PersistentDataType.STRING, origin.customName
                        )
                        it.set(
                            NamespacedKey(instance, NS + "_dimension"),
                            PersistentDataType.STRING, origin.location.world.name
                        )
                        it.set(
                            NamespacedKey(instance, NS + "_x"),
                            PersistentDataType.DOUBLE, origin.location.x
                        )
                        it.set(
                            NamespacedKey(instance, NS + "_y"),
                            PersistentDataType.DOUBLE, origin.location.y
                        )
                        it.set(
                            NamespacedKey(instance, NS + "_z"),
                            PersistentDataType.DOUBLE, origin.location.z
                        )
                    }
                }
                itemMeta = itemMeta.apply {
                    customName(customName.mm())
                    setCustomModelData(Config.WaystoneModule.WAYSTONE_CUSTOM_MODEL_DATA)
                    if (origin != null && destination != null) {
                        val cost = calculateXpCost(origin.location, destination.location)
                        lore(listOf("<bold>Cost: $cost XP</bold>".mangoFmt().mm()))
                    }
                }
            }
        }

        /**
         * Creates a custom shaped crafting recipe for the given item.
         *
         * The recipe is defined with the following shape:
         * - Row 1: "   " (empty row)
         * - Row 2: "CBC" (middle row with an Ender Pearl in the center and a Compass on each side)
         * - Row 3: "AAA" (bottom row filled with Stone Bricks)
         *
         * Uses specific ingredients to shape the recipe:
         * - 'A' corresponds to Stone Bricks.
         * - 'B' corresponds to an Ender Pearl.
         * - 'C' corresponds to a Compass.
         *
         * @param item The resulting item of the crafting recipe.
         * @return A custom `ShapedRecipe` for the provided item using the defined shape and ingredients.
         */
        fun recipe(item: ItemStack): Recipe {
            return ShapedRecipe(NamespacedKey(instance, NS + "_recipe"), item).apply {
                shape("   ", "CBC", "AAA")
                setIngredient('A', Material.STONE_BRICKS)
                setIngredient('B', Material.ENDER_PEARL)
                setIngredient('C', Material.COMPASS)
            }
        }

        /**
         * Calculates the experience (XP) cost for traveling or teleporting between two locations.
         * @param origin The starting location from which the travel begins. Must include world and coordinates.
         * @param destination The target location to which the travel ends. Must include world and coordinates.
         * @return The calculated XP cost for traveling between the given origin and destination.
         */
        fun calculateXpCost(origin: Location, destination: Location): Int {
            return Config.WaystoneModule.BASE_XP_COST + when (origin.world) {
                destination.world -> (origin.distance(destination) * Config.WaystoneModule.DISTANCE_MULTIPLIER).toInt()
                else -> Config.WaystoneModule.DIMENSIONAL_MULTIPLIER
            }
        }
    }
}



