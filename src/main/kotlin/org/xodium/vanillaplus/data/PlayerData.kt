/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.Database

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
