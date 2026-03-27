package org.xodium.vanillaplus.data

import org.intellij.lang.annotations.Language
import org.xodium.vanillaplus.managers.DatabaseManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Represents a kingdom within the system. */
@OptIn(ExperimentalUuidApi::class)
internal data class KingdomData(
    /** The [Uuid] of the player who owns the kingdom. */
    val owner: Uuid,
    /** The display name of the kingdom. */
    val name: String,
    /** The [Uuid]s of all players in this kingdom, including the owner. */
    val members: List<Uuid> = emptyList(),
) {
    /** Deletes this kingdom record from the database. */
    fun delete() {
        val sql = "DELETE FROM kingdoms WHERE owner = ?"

        DatabaseManager.connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, owner.toString())
            stmt.executeUpdate()
        }
    }

    /** Inserts or replaces this kingdom record in the database. */
    fun save() {
        val sql = "INSERT OR REPLACE INTO kingdoms (owner, name, members) VALUES (?, ?, ?)"

        DatabaseManager.connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, owner.toString())
            stmt.setString(2, name)
            stmt.setString(3, members.joinToString(","))
            stmt.executeUpdate()
        }
    }

    companion object Schema {
        @Language("SQL")
        val KINGDOMS =
            """
            CREATE TABLE IF NOT EXISTS kingdoms (
                owner TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                members TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()

        /**
         * Returns the [KingdomData] for [owner], or `null` if none exists.
         * @param owner The [Uuid] of the kingdom owner.
         */
        fun get(owner: Uuid): KingdomData? {
            val sql = "SELECT name, members FROM kingdoms WHERE owner = ?"

            return DatabaseManager.connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, owner.toString())
                stmt.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    KingdomData(
                        owner = owner,
                        name = rs.getString("name"),
                        members =
                            rs
                                .getString("members")
                                .split(",")
                                .filter { it.isNotBlank() }
                                .map {
                                    java.util.UUID
                                        .fromString(it)
                                        .toKotlinUuid()
                                },
                    )
                }
            }
        }
    }
}
