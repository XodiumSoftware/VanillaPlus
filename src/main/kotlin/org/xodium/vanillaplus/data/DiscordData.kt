/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object DiscordDataSchema : UUIDTable(DiscordData::class.simpleName.toString()) {
    val allowedChannels: Column<String> = text("allowed_channels")
}

class DiscordDataEntity(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, DiscordDataEntity>(DiscordDataSchema)

    var allowedChannels: String by DiscordDataSchema.allowedChannels

    fun toData(): DiscordData = DiscordData(
        id.value,
        allowedChannels.split(",").filter { it.isNotBlank() }.map { Snowflake(it) }
    )
}

/**
 * Data class representing Discord-related configuration.
 * @property id A unique identifier for the [DiscordData], represented as a [UUID].
 * @property allowedChannels A list of [Snowflake] IDs representing channels that are allowed for some operation.
 */
data class DiscordData(
    val id: UUID,
    val allowedChannels: List<Snowflake>
) {
    companion object {
        /** Creates a table in the database for the provided class type if it does not already exist. */
        fun createTable(): Unit = transaction { SchemaUtils.create(DiscordDataSchema) }

        /**
         * Sets the [DiscordData] in the database.
         * @param data The [DiscordData] to set.
         * @return The updated or newly created [DiscordDataEntity].
         */
        fun setData(data: DiscordData): DiscordDataEntity = transaction {
            DiscordDataEntity.findById(data.id)?.apply {
                allowedChannels = data.allowedChannels.joinToString(",") { it.value.toString() }
            } ?: DiscordDataEntity.new(data.id) {
                allowedChannels = data.allowedChannels.joinToString(",") { it.value.toString() }
            }
        }

        /**
         * Retrieves all [DiscordData] records from the database.
         * @return A list of [DiscordData] objects representing the configuration data for all players.
         */
        fun getData(): List<DiscordData> = transaction { DiscordDataEntity.all().map { it.toData() } }
    }
}
