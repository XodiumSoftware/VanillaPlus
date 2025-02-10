/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

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
            Runtime.getRuntime().addShutdownHook(Thread { close() })
            conn.createStatement()
                .use { stmt -> stmt.executeUpdate("CREATE TABLE IF NOT EXISTS example (id INTEGER PRIMARY KEY, value TEXT);") }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun close() {
        if (this::conn.isInitialized && !conn.isClosed) {
            try {
                conn.close()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }
}