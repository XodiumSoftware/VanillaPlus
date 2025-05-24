/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object PlayerDataSchema : IdTable<String>(PlayerData::class.simpleName.toString()) {
    override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    val autorefill: Column<Boolean> = bool("autorefill").default(false)
    val autotool: Column<Boolean> = bool("autotool").default(false)
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class PlayerDataEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, PlayerDataEntity>(PlayerDataSchema)

    var autorefill: Boolean by PlayerDataSchema.autorefill
    var autotool: Boolean by PlayerDataSchema.autotool

    fun toData(): PlayerData = PlayerData(
        id.value,
        autorefill,
        autotool,
    )
}

/**
 * Represents player-specific configuration data.
 * @property id A unique identifier for the player.
 * @property autorefill Indicates whether the autorefill feature is enabled for the player.
 * @property autotool Indicates whether the autotool feature is enabled for the player.
 */
data class PlayerData(
    val id: String,
    val autorefill: Boolean = false,
    val autotool: Boolean = false,
) {
    companion object {
        /** Creates a table in the database for the provided class type if it does not already exist. */
        fun createTable(): Unit = transaction { SchemaUtils.create(PlayerDataSchema) }

        /**
         * Sets the [PlayerData] in the database.
         * @param data The [PlayerData] to set.
         * @return The updated or newly created [PlayerDataEntity].
         */
        fun setData(data: PlayerData): PlayerDataEntity = transaction {
            PlayerDataEntity.findById(data.id)?.apply {
                autorefill = data.autorefill
                autotool = data.autotool
            } ?: PlayerDataEntity.new(data.id) {
                autorefill = data.autorefill
                autotool = data.autotool
            }
        }

        /**
         * Retrieves all [PlayerData] records from the database.
         * @return A list of [PlayerData] objects representing the configuration data for all players.
         */
        fun getData(): List<PlayerData> = transaction { PlayerDataEntity.all().map { it.toData() } }
    }
}
