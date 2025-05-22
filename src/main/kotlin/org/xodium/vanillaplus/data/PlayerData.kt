/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.enums.ChiselMode

/**
 * Represents player-specific configuration data.
 * @property id A unique identifier for the player.
 * @property autorefill Indicates whether the autorefill feature is enabled for the player.
 * @property autotool Indicates whether the autotool feature is enabled for the player.
 */
data class PlayerData(
    val id: String,
    val autorefill: Boolean = false,
    val autotool: Boolean = false,
    val chiselMode: ChiselMode = ChiselMode.ROTATE,
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
                    chiselMode TEXT NOT NULL DEFAULT 'ROTATE'
                )
                """.trimIndent()
            )
        }

        /**
         * Inserts or updates a record in the database table corresponding to the current class
         * with the data provided in the [PlayerData] object.
         * @param data The [PlayerData] object containing information to be stored in the database.
         */
        fun setData(data: PlayerData) {
            Database.exec(
                //language=SQLite
                """
                INSERT OR REPLACE INTO ${PlayerData::class.simpleName} (id, autorefill, autotool, chiselMode)
                VALUES (?, ?, ?, ?);
                """.trimIndent(),
                data.id,
                data.autorefill,
                data.autotool,
                data.chiselMode.name,
            )
        }

        /**
         * Retrieves a list of [PlayerData] objects from the corresponding database table.
         * @return a list of [PlayerData] extracted from the database.
         */
        fun getData(): List<PlayerData> {
            return Database.query(
                //language=SQLite
                """
                SELECT id, autorefill, autotool, chiselMode
                FROM ${PlayerData::class.simpleName};
                """.trimIndent()
            ) { rs ->
                generateSequence {
                    if (rs.next()) PlayerData(
                        rs.getString("id"),
                        rs.getBoolean("autorefill"),
                        rs.getBoolean("autotool"),
                        ChiselMode.valueOf(rs.getString("chiselMode"))
                    ) else null
                }.toList()
            }
        }
    }
}
