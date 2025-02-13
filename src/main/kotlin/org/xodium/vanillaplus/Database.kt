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

object Database {
    private val databaseFile = instance.dataFolder.resolve("vanillaplus.db")
    private const val DRIVER = "org.sqlite.JDBC"
    lateinit var conn: Connection
        private set

    init {
        try {
            Class.forName(DRIVER)
            databaseFile.parentFile.apply { if (!exists()) mkdirs() }
            conn = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
            instance.logger.info("Opened Database Connection.")
            listOf(Config).forEach { createTable(it::class) }
            Runtime.getRuntime().addShutdownHook(Thread { close() })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun createTable(table: KClass<*>) {
        conn.createStatement().use { stmt ->
            stmt.execute(
                """
                    CREATE TABLE IF NOT EXISTS ${table.simpleName} (
                        k TEXT PRIMARY KEY,
                        v TEXT
                    );
                    """.trimIndent()
            )
        }
    }

    fun setData(table: KClass<*>, key: String, value: String) {
        conn.prepareStatement(
            """
            INSERT OR REPLACE INTO ${table.simpleName} (k, v) VALUES (?, ?);
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, key)
            stmt.setString(2, value)
            stmt.executeUpdate()
        }
    }

    fun getData(table: KClass<*>, key: String): String? {
        return conn.prepareStatement(
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