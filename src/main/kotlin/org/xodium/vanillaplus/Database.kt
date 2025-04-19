/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
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
     * Initializes the database connection.
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
     *
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
     *
     * @param table The table to set the data in.
     * @param key The key to set the data with.
     * @param value The value to set the data with.
     */
    fun setData(table: KClass<*>, key: String, value: String) {
        conn.prepareStatement(
            // language=SQLite
            """
            INSERT OR REPLACE INTO ${table.simpleName} (k, v) VALUES (?, ?);
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.executeUpdate()
        }
    }

    /**
     * Gets data from the database.
     *
     * @param table The table to get the data from.
     * @param key The key to get the data with.
     * @return The data retrieved from the database.
     */
    fun getData(table: KClass<*>, key: String): String? {
        return conn.prepareStatement(
            // language=SQLite
            """
            SELECT v FROM ${table.simpleName} WHERE k = ?;
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getString("v") else null
            }
        }
    }

    /**
     * Deletes data from the database.
     *
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