package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.sql.Connection
import java.sql.DriverManager

/** Manages the SQLite database connection. */
internal object DatabaseManager {
    internal lateinit var connection: Connection
        private set

    /** Opens the database connection and ensures the schema is up to date. */
    fun init() {
        instance.dataFolder.mkdirs()
        connection =
            DriverManager.getConnection(
                "jdbc:sqlite:${instance.dataFolder.resolve("vanillaplus.db").absolutePath}",
            )
        connection.createStatement().use { it.execute("PRAGMA journal_mode=WAL") }
    }

    /** Closes the database connection. */
    fun close() {
        if (::connection.isInitialized && !connection.isClosed) connection.close()
    }
}
