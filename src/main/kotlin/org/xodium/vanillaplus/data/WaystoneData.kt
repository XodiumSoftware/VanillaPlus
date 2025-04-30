/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Location
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
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
                                instance.server.getWorld(resultSet.getString("world")),
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
    }
}



