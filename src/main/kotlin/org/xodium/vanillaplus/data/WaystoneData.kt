/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents metadata related to a Waystone in the Minecraft world.
 *
 * This data class encapsulates essential properties such as the unique identifier (ID),
 * a custom name for the Waystone, and the location data where the Waystone exists.
 *
 * @property id A unique identifier for the Waystone, prefixed with a namespace (NS) and followed by a random UUID.
 * @property customName The customizable name of the Waystone, defaulting to "Waystone".
 * @property location The location of the Waystone, stored as `LocationData`.
 */
@OptIn(ExperimentalUuidApi::class)
data class WaystoneData(
    val id: String = "${NS}_${Uuid.random()}",
    val customName: String = "Waystone",
    val location: Location,
) {
    companion object {
        private const val NS = "waystone"

        fun createTable(table: KClass<*>) {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${table.simpleName} (
                    id TEXT PRIMARY KEY,
                    custom_name TEXT NOT NULL,
                    world TEXT NOT NULL, 
                    x REAL NOT NULL, 
                    y REAL NOT NULL, 
                    z REAL NOT NULL
                );
                """.trimIndent()
            )
        }

        fun setData(table: KClass<*>, waystone: WaystoneData) {
            //language=SQLite
            Database.exec(
                """
                INSERT OR REPLACE INTO ${table.simpleName} (id, custom_name, world, x, y, z)
                VALUES (?, ?, ?, ?, ?, ?);
                """.trimIndent(),
                waystone.id,
                waystone.customName,
                waystone.location.world.name,
                waystone.location.x,
                waystone.location.y,
                waystone.location.z
            )
        }

        fun getData(table: KClass<*>): List<WaystoneData> {
            //language=SQLite
            val sql = """
                SELECT id, custom_name, world, x, y, z
                FROM ${table.simpleName};
            """.trimIndent()
            return Database.query(sql) { resultSet ->
                val results = mutableListOf<WaystoneData>()
                while (resultSet.next()) {
                    results.add(
                        WaystoneData(
                            id = resultSet.getString("id"),
                            customName = resultSet.getString("custom_name"),
                            location = Location(
                                Bukkit.getWorld(resultSet.getString("world")),
                                resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z")
                            )
                        )
                    )
                }
                results
            }
        }

        fun deleteData(table: KClass<*>, id: String) {
            //language=SQLite
            Database.exec(
                """
                DELETE FROM ${table.simpleName}
                WHERE id = ?;
                """.trimIndent(), id
            )
        }


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
        fun item(
            customName: String = "Waystone",
            origin: WaystoneData? = null,
            destination: WaystoneData? = null
        ): ItemStack {
            return ItemStack(Config.WaystoneModule.WAYSTONE_MATERIAL).apply {
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



