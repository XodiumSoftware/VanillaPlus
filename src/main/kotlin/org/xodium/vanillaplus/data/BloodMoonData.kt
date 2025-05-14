/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.Database

/**
 * Data class to hold information about blood-moons.
 * @property isActive Indicates if the blood-moon is active.
 */
data class BloodMoonData(
    var isActive: Boolean = false
) {
    companion object {
        /** Creates a table in the database for the specified class if it does not already exist. */
        fun createTable() {
            Database.exec(
                //language=SQLite
                """
                CREATE TABLE IF NOT EXISTS ${BloodMoonData::class.simpleName} (
                    isActive BOOLEAN PRIMARY KEY);
                """.trimIndent()
            )
        }

        /**
         * Inserts or updates a record in the database table corresponding to the current class
         * with the data provided in the BloodMoonData object.
         * @param data The BloodMoonData object containing information to be stored in the database.
         *               It includes the isActive attributes of the blood-moon.
         */
        fun setData(data: BloodMoonData) {
            Database.exec(
                //language=SQLite
                """
                INSERT OR REPLACE INTO ${PlayerData::class.simpleName} (isActive)
                VALUES (?);
                """.trimIndent(),
                data.isActive
            )
        }

        /**
         * Retrieves a list of `BloodMoonData` objects from the corresponding database table.
         * @return a list of `BloodMoonData` containing the isActive extracted from the database.
         */
        fun getData(): List<BloodMoonData> {
            //language=SQLite
            val sql = """
                SELECT isActive
                FROM ${PlayerData::class.simpleName};
            """.trimIndent()
            return Database.query(sql) { resultSet ->
                val results = mutableListOf<BloodMoonData>()
                while (resultSet.next()) {
                    results.add(
                        BloodMoonData(
                            resultSet.getBoolean("isActive")
                        )
                    )
                }
                results
            }
        }
    }
}
