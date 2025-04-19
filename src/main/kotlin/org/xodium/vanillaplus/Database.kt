/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Types
import kotlin.reflect.KClass

/**
 * Handles database connection
 */
object Database {
    private val databaseFile = instance.dataFolder.resolve("vanillaplus.db")
    private const val DRIVER = "org.sqlite.JDBC"
    private val connUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
    lateinit var conn: Connection
        private set

    /**
     * Initialises the database connection.
     */
    init {
        try {
            Class.forName(DRIVER)
            databaseFile.parentFile.apply { if (!exists()) mkdirs() }
            conn = DriverManager.getConnection(connUrl)
            instance.logger.info("Opened Database Connection.")
            Runtime.getRuntime().addShutdownHook(Thread { close() })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Creates a table in the database.
     * @param table The table to create.
     */
    fun createTable(table: KClass<*>) {
        conn.createStatement().use { stmt ->
            stmt.execute(
                // language=SQLite
                """
                    CREATE TABLE IF NOT EXISTS ${table.simpleName} (
                        k TEXT PRIMARY KEY,
                        v TEXT
                    );
                    """.trimIndent()
            )
        }
    }

    /**
     * Sets data in the database.
     * @param table The table to set the data in.
     * @param key The key to set the data with.
     * @param value The value to set the data to.
     */
    fun setData(table: KClass<*>, key: String, value: String? = null) {
        conn.prepareStatement(
            // language=SQLite
            """
            INSERT OR REPLACE INTO ${table.simpleName} (k, v) VALUES (?, ?);
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, key)
            if (value == null) {
                stmt.setNull(2, Types.VARCHAR)
            } else stmt.setString(2, value)
            stmt.executeUpdate()
        }
    }

    /**
     * Gets data from the database.
     * - If a key is supplied, returns the value for that key (or null if not found).
     * - If no key is supplied (null), returns all keys and values as a Map.
     *
     * @param table The table to get the data from.
     * @param key The key to get the data with, or null to get all.
     * @return The data retrieved from the database (String?, or Map<String, String?> if key is null).
     */
    fun getData(table: KClass<*>, key: String? = null): Any? {
        return if (key != null) {
            conn.prepareStatement(
                // language=SQLite
                "SELECT v FROM ${table.simpleName} WHERE k = ?;"
            ).use { stmt ->
                stmt.setString(1, key)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getString("v") else null
                }
            }
        } else {
            conn.createStatement().use { stmt ->
                stmt.executeQuery(
                    // language=SQLite
                    "SELECT k, v FROM ${table.simpleName};"
                ).use { rs ->
                    val result = mutableMapOf<String, String?>()
                    while (rs.next()) {
                        result[rs.getString("k")] = rs.getString("v")
                    }
                    result
                }
            }
        }
    }

    /**
     * Deletes data from the database.
     * @param table The table to delete the data from.
     * @param key The key of the data to delete.
     */
    fun deleteData(table: KClass<*>, key: String) {
        conn.prepareStatement(
            // language=SQLite
            """
            DELETE FROM ${table.simpleName} WHERE k = ?;
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.executeUpdate()
        }
    }

    /**
     * Closes the database connection.
     */
    private fun close() {
        if (this::conn.isInitialized && !conn.isClosed) {
            try {
                conn.close()
                instance.logger.info("Closed Database Connection.")
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }
}