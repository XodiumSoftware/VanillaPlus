/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.Database

/**
 * Represents player-specific configuration data.
 * @property id A unique identifier for the player.
 * @property autorefill Indicates whether the autorefill feature is enabled for the player.
 * @property autotool Indicates whether the autotool feature is enabled for the player.
 * @property discoveredWaystones A list of discovered Waystone IDs. Null if none discovered.
 */
data class PlayerData(
    val id: String,
    val autorefill: Boolean? = false,
    val autotool: Boolean? = false,
    val discoveredWaystones: List<String>? = null
) {
    companion object {
        /** Creates a table in the database for the provided class type if it does not already exist. */
        fun createTable() {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${PlayerData::class.simpleName} (
                    id TEXT PRIMARY KEY,
                    autorefill BOOLEAN NOT NULL DEFAULT false,
                    autotool BOOLEAN NOT NULL DEFAULT false,
                    discovered_waystones TEXT); 
                """.trimIndent()
            )
        }

        /**
         * Inserts or updates a record in the database table corresponding to the current class
         * with the data provided in the PlayerData object.
         * @param data The PlayerData object containing information to be stored in the database.
         *               It includes the ID, autorefill, and autotool attributes of the player.
         */
        fun setData(data: PlayerData) {
            Database.exec(
                //language=SQLite
                """
                INSERT OR REPLACE INTO ${PlayerData::class.simpleName} (id, autorefill, autotool, discovered_waystones)
                VALUES (?, ?, ?, ?);
                """.trimIndent(),
                data.id,
                data.autorefill,
                data.autotool,
                data.discoveredWaystones?.joinToString(",")
            )
        }

        /**
         * Retrieves a list of `PlayerData` objects from the corresponding database table.
         * @return a list of `PlayerData` containing the id, autorefill, and autotool fields
         * extracted from the database.
         */
        fun getData(): List<PlayerData> {
            //language=SQLite
            val sql = """
                SELECT id, autorefill, autotool, discovered_waystones
                FROM ${PlayerData::class.simpleName};
            """.trimIndent()
            return Database.query(sql) { resultSet ->
                val results = mutableListOf<PlayerData>()
                while (resultSet.next()) {
                    val discoveredString = resultSet.getString("discovered_waystones")
                    val discoveredList = discoveredString?.split(',')?.filter { it.isNotEmpty() }?.toList()
                    results.add(
                        PlayerData(
                            resultSet.getString("id"),
                            resultSet.getBoolean("autorefill"),
                            resultSet.getBoolean("autotool"),
                            discoveredList
                        )
                    )
                }
                results
            }
        }
    }
}
