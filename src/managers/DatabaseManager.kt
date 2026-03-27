package org.xodium.vanillaplus.managers

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.KingdomData
import java.sql.Connection
import java.sql.DriverManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Manages the SQLite database connection and schema for VanillaPlus. */
@OptIn(ExperimentalUuidApi::class)
internal object DatabaseManager {
    private lateinit var connection: Connection

    /** Opens the database connection and ensures the schema is up to date. */
    fun init() {
        val dbFile = instance.dataFolder.resolve("kingdoms.db")

        instance.dataFolder.mkdirs()
        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        connection.createStatement().use { it.execute("PRAGMA journal_mode=WAL") }
        connection.createStatement().use { it.execute("PRAGMA foreign_keys=ON") }
        connection.createStatement().use { it.execute(KingdomData.KINGDOMS) }
        connection.createStatement().use { it.execute(KingdomData.KINGDOM_MEMBERS) }
    }

    /** Closes the database connection. */
    fun close() {
        if (::connection.isInitialized && !connection.isClosed) connection.close()
    }

    /**
     * Returns the [KingdomData] for [ownerUuid], or `null` if none exists.
     * @param ownerUuid The UUID of the kingdom owner.
     */
    fun getKingdom(ownerUuid: Uuid): KingdomData? {
        val kingdom =
            connection
                .prepareStatement("SELECT name FROM kingdoms WHERE owner_uuid = ?")
                .use { stmt ->
                    stmt.setString(1, ownerUuid.toString())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getString("name") else return null
                    }
                }

        val members =
            connection
                .prepareStatement("SELECT member_uuid FROM kingdom_members WHERE owner_uuid = ?")
                .use { stmt ->
                    stmt.setString(1, ownerUuid.toString())
                    stmt.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) {
                                add(
                                    java.util.UUID
                                        .fromString(rs.getString("member_uuid"))
                                        .toKotlinUuid(),
                                )
                            }
                        }
                    }
                }

        return KingdomData(owner = ownerUuid, name = kingdom, members = members)
    }

    /**
     * Inserts or replaces the [kingdom] record, syncing its member list.
     * @param kingdom The [KingdomData] to persist.
     */
    fun setKingdom(kingdom: KingdomData) {
        connection.autoCommit = false
        runCatching {
            connection
                .prepareStatement("INSERT OR REPLACE INTO kingdoms (owner_uuid, name) VALUES (?, ?)")
                .use { stmt ->
                    stmt.setString(1, kingdom.owner.toString())
                    stmt.setString(2, kingdom.name)
                    stmt.executeUpdate()
                }

            connection
                .prepareStatement("DELETE FROM kingdom_members WHERE owner_uuid = ?")
                .use { stmt ->
                    stmt.setString(1, kingdom.owner.toString())
                    stmt.executeUpdate()
                }

            connection
                .prepareStatement("INSERT INTO kingdom_members (owner_uuid, member_uuid) VALUES (?, ?)")
                .use { stmt ->
                    kingdom.members.forEach { member ->
                        stmt.setString(1, kingdom.owner.toString())
                        stmt.setString(2, member.toString())
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }

            connection.commit()
        }.onFailure {
            connection.rollback()
            throw it
        }.also {
            connection.autoCommit = true
        }
    }
}
