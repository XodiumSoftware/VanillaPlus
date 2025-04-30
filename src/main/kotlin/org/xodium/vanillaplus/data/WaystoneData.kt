/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents metadata related to a Waystone in the Minecraft world.
 * @property id A unique identifier for the Waystone, prefixed with a namespace (NS) and followed by a random UUID.
 * @property customName The customisable name of the Waystone, defaulting to "Waystone".
 * @property location The location of the Waystone, stored as `LocationData`.
 */
@OptIn(ExperimentalUuidApi::class)
data class WaystoneData(
    val id: String = "${Uuid.random()}",
    val customName: String = "Waystone",
    val location: Location,
) {
    companion object {
        /**
         * Creates a table in the database for the specified class if it does not already exist.
         */
        fun createTable() {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${WaystoneData::class.simpleName} (
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
         * @param data The WaystoneData object containing information to be stored in the database.
         *                 It includes the ID, custom name, world, and coordinates (x, y, z) of the waystone.
         */
        fun setData(data: WaystoneData) {
            //language=SQLite
            Database.exec(
                """
                INSERT OR REPLACE INTO ${WaystoneData::class.simpleName} (id, custom_name, world, x, y, z)
                VALUES (?, ?, ?, ?, ?, ?);
                """.trimIndent(),
                data.id,
                data.customName,
                data.location.world.name,
                data.location.x,
                data.location.y,
                data.location.z
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
                FROM ${WaystoneData::class.simpleName};
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
                DELETE FROM ${WaystoneData::class.simpleName}
                WHERE id = ?;
                """.trimIndent(), id
            )
        }


        /**
         * Creates an `ItemStack` instance configured as a waystone item, with optional origin and destination data.
         * If both origin and destination are provided, the item will include a lore displaying the XP cost
         * required for travelling between the two locations.
         * @param customName The custom name of the item. Defaults to "Waystone".
         * @param origin The origin `WaystoneData` representing the starting location. Null if not needed.
         * @param destination The destination `WaystoneData` representing the target location. Null if not needed.
         * @param player The player interacting with the waystone, used to determine if the player is mounted.
         * @return A configured `ItemStack` representing the waystone with the specified attributes and lore.
         */
        fun item(
            customName: String = "Waystone",
            origin: Location? = null,
            destination: Location? = null,
            player: Player? = null,
        ): ItemStack {
            @Suppress("UnstableApiUsage")
            return ItemStack(Config.WaystoneModule.WAYSTONE_MATERIAL).apply {
                val loreLines = mutableListOf("Click to teleport".fireFmt().mm())
                if (origin != null && destination != null && player?.gameMode in listOf(
                        GameMode.SURVIVAL,
                        GameMode.ADVENTURE
                    )
                ) {
                    loreLines.add(
                        "Travel Cost: ${
                            calculateXpCost(
                                origin,
                                destination,
                                player?.isInsideVehicle ?: false
                            )
                        }".mm()
                    )
                }

                setData(DataComponentTypes.CUSTOM_NAME, customName.mm())
                setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA,
                    CustomModelData.customModelData().addString(Config.WaystoneModule.WAYSTONE_CUSTOM_MODEL_DATA)
                )
                setData(DataComponentTypes.LORE, ItemLore.lore().addLines(loreLines))
            }
        }

        /**
         * Creates a custom-shaped crafting recipe for the given item.
         * @param item The resulting item of the crafting recipe.
         * @return A custom `ShapedRecipe` for the provided item using the defined shape and ingredients.
         */
        fun recipe(item: ItemStack): Recipe {
            return ShapedRecipe(NamespacedKey(instance, "waystone_recipe"), item).apply {
                shape("CCC", "CBC", "AAA")
                setIngredient('A', Material.OBSIDIAN)
                setIngredient('B', Material.ENDER_EYE)
                setIngredient('C', Material.GLASS)
            }
        }

        /**
         * Calculates the experience point (XP) cost for travelling between two locations,
         * factoring in whether the travel is mounted and whether the destination is in a different dimension.
         * @param origin The starting location of the player, represented as a `Location` object.
         * @param destination The destination location of the player, represented as a `Location` object.
         * @param isMounted A Boolean flag indicating whether the player is mounted (e.g. riding a horse). Default is false.
         * @return The calculated XP cost as an integer based on the distance, dimension, and whether the player is mounted.
         */
        fun calculateXpCost(origin: Location, destination: Location, isMounted: Boolean): Int {
            val config = Config.WaystoneModule
            val baseCost = config.BASE_XP_COST + when (origin.world) {
                destination.world -> (origin.distance(destination) * config.DISTANCE_MULTIPLIER).toInt()
                else -> config.DIMENSIONAL_MULTIPLIER
            }
            return if (isMounted) (baseCost * config.MOUNT_MULTIPLIER).toInt() else baseCost
        }
    }
}



