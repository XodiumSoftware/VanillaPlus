package org.xodium.illyriaplus.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import java.io.File

/** Manages the SQLite database connection and schema for IllyriaKingdoms. */
internal object DatabaseManager {
    private lateinit var database: Database

    /**
     * Initializes the database connection and creates tables if they don't exist.
     * Must be called from the main thread during plugin enable.
     */
    fun init() {
        val dbFile = File(instance.dataFolder, "illyriakingdoms.db")

        if (!dbFile.parentFile.exists()) dbFile.parentFile.mkdirs()

        database =
            Database.connect(
                url = "jdbc:sqlite:${dbFile.absolutePath}",
                driver = "org.sqlite.JDBC",
            )

        TransactionManager.defaultDatabase = database

        instance.logger.info("Database initialized at: ${dbFile.absolutePath}")
    }

    /**
     * Closes the database connection.
     * Should be called during plugin disable.
     */
    fun close() {
        if (::database.isInitialized) {
            TransactionManager.closeAndUnregister(database)
            instance.logger.info("Database connection closed")
        }
    }

    /**
     * Executes a database transaction.
     * Automatically handles transaction scope and error handling.
     *
     * @param block The transaction block to execute
     * @return The result of the transaction block
     */
    fun <T> transaction(block: () -> T): T = transaction(database) { block() }
}
