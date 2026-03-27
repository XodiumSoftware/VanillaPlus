package org.xodium.vanillaplus.data

import org.intellij.lang.annotations.Language
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Represents a kingdom within the system. */
@OptIn(ExperimentalUuidApi::class)
internal data class KingdomData(
    /** The [Uuid] of the player who owns the kingdom. */
    val owner: Uuid,
    /** The display name of the kingdom. */
    val name: String,
    /** The [Uuid]s of all players who are members of this kingdom. */
    val members: List<Uuid> = emptyList(),
) {
    companion object Schema {
        @Language("SQL")
        val KINGDOMS =
            """
            CREATE TABLE IF NOT EXISTS kingdoms (
                owner_uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL
            )
            """.trimIndent()

        @Language("SQL")
        val KINGDOM_MEMBERS =
            """
            CREATE TABLE IF NOT EXISTS kingdom_members (
                owner_uuid TEXT NOT NULL REFERENCES kingdoms(owner_uuid) ON DELETE CASCADE,
                member_uuid TEXT NOT NULL,
                PRIMARY KEY (owner_uuid, member_uuid)
            )
            """.trimIndent()
    }
}
