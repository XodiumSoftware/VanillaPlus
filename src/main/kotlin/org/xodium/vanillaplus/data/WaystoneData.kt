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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents metadata related to a Waystone in the Minecraft world.
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
        /**
         * Namespace identifier for features and configurations related to waystones.
         */
        private const val NS = "waystone" //TODO: is this needed?

        /**
         * Creates a table in the database for the specified class if it does not already exist.
         */
        fun createTable() {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${this::class.simpleName} (
                    id TEXT PRIMARY KEY,
                    custom_name TEXT NOT NULL,
                    world TEXT NOT NULL, 
                    x REAL NOT NULL, 
                    y REAL NOT NULL, 
                    z REAL NOT NULL);
                """.trimIndent()
            )
        }

        /**
         * Inserts or updates a record in the database table corresponding to the given class,
         * using the data provided in the WaystoneData object.
         * @param waystone The WaystoneData object containing information to be stored in the database.
         *                 It includes the ID, custom name, world, and coordinates (x, y, z) of the waystone.
         */
        fun setData(waystone: WaystoneData) {
            //language=SQLite
            Database.exec(
                """
                INSERT OR REPLACE INTO ${this::class.simpleName} (id, custom_name, world, x, y, z)
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

        /**
         * Retrieves a list of `WaystoneData` objects from the specified database table.
         * @return a list of `WaystoneData` objects containing the extracted data.
         */
        fun getData(): List<WaystoneData> {
            //language=SQLite
            val sql = """
                SELECT id, custom_name, world, x, y, z
                FROM ${this::class.simpleName};
            """.trimIndent()
            return Database.query(sql) { resultSet ->
                val results = mutableListOf<WaystoneData>()
                while (resultSet.next()) {
                    results.add(
                        WaystoneData(
                            resultSet.getString("id"),
                            resultSet.getString("custom_name"),
                            Location(
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

        /**
         * Deletes data from a specified table based on the given ID.
         * @param id The unique identifier of the record to be deleted.
         */
        fun deleteData(id: String) {
            //language=SQLite
            Database.exec(
                """
                DELETE FROM ${this::class.simpleName}
                WHERE id = ?;
                """.trimIndent(), id
            )
        }


        /**
         * Creates an ItemStack representing a Waystone with specific properties.
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



