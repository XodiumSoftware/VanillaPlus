/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
     * Executes an SQL statement, optionally with parameters.
     * @param sql The SQL statement to be executed
     * @param params Optional parameters to bind to the statement
     */
    fun exec(sql: String, vararg params: Any?) {
        if (params.isEmpty()) {
            conn.createStatement().use { it.execute(sql) }
        } else {
            conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { index, param -> stmt.setObject(index + 1, param) }
                stmt.executeUpdate()
            }
        }
    }

    /**
     * Executes SQL queries that return results.
     * @param sql the SQL query to be executed
     * @param handler a lambda to handle the result set
     */
    fun <T> query(sql: String, handler: (java.sql.ResultSet) -> T): T {
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sql).use { resultSet ->
                return handler(resultSet)
            }
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