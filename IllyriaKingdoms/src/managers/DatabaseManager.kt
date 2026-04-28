package org.xodium.illyriaplus.managers

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.xodium.illyriaplus.IllyriaKingdoms
import java.io.File

/** Manages the SQLite database connection and schema for IllyriaKingdoms. */
internal object DatabaseManager {
    private lateinit var database: Database

    /**
     * Initializes the database connection and creates tables if they don't exist.
     * Must be called from the main thread during plugin enable.
     */
    fun init() {
        val dbFile = File(IllyriaKingdoms.instance.dataFolder, "illyriakingdoms.db")

        if (!dbFile.parentFile.exists()) dbFile.parentFile.mkdirs()

        database =
            Database.connect(
                url = "jdbc:sqlite:${dbFile.absolutePath}",
                driver = "org.sqlite.JDBC",
            )

        TransactionManager.defaultDatabase = database

        IllyriaKingdoms.instance.logger.info("Database initialized at: ${dbFile.absolutePath}")
    }

    /**
     * Closes the database connection.
     * Should be called during plugin disable.
     */
    fun close() {
        if (::database.isInitialized) {
            TransactionManager.closeAndUnregister(database)
            IllyriaKingdoms.instance.logger.info("Database connection closed")
        }
    }
}
