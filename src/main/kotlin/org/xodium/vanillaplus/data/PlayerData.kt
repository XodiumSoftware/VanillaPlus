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
 */
data class PlayerData(
    val id: String,
    val autorefill: Boolean? = true,
    val autotool: Boolean? = true,
) {
    companion object {
        /**
         * Creates a table in the database for the provided class type if it does not already exist.
         */
        fun createTable() {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${this::class.simpleName} (
                    id TEXT PRIMARY KEY,
                    autorefill BOOLEAN DEFAULT false,
                    autotool BOOLEAN DEFAULT false); 
                """.trimIndent()
            )
        }

        /**
         * Inserts or updates a record in the database table corresponding to the current class
         * with the data provided in the PlayerData object.
         * @param player The PlayerData object containing information to be stored in the database.
         *               It includes the ID, autorefill, and autotool attributes of the player.
         */
        fun setData(player: PlayerData) {
            //language=SQLite
            Database.exec(
                """
                INSERT OR REPLACE INTO ${this::class.simpleName} (id, autorefill, autotool)
                VALUES (?, ?, ?);
                """.trimIndent(),
                player.id,
                player.autorefill,
                player.autotool
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
                SELECT id, autorefill, autotool
                FROM ${this::class.simpleName};
            """.trimIndent()
            return Database.query(sql) { resultSet ->
                val results = mutableListOf<PlayerData>()
                while (resultSet.next()) {
                    results.add(
                        PlayerData(
                            resultSet.getString("id"),
                            resultSet.getBoolean("autorefill"),
                            resultSet.getBoolean("autotool")
                        )
                    )
                }
                results
            }
        }
    }
}
