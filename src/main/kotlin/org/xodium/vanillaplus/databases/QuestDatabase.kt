@file:OptIn(ExperimentalUuidApi::class)

@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.databases

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.QuestModule
import java.io.File
import java.sql.DriverManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Manages the quest database. */
internal class QuestDatabase {
    private val dbFile = File(instance.dataFolder, "quests.db")
    private val db: Database

    private object QuestProgressTable : Table("quest_progress") {
        val playerUuid = text("player_uuid")
        val questId = integer("quest_id")
        val currentProgress = integer("current_progress")

        init {
            index(false, playerUuid)
            index(false, playerUuid, questId)
        }
    }

    private companion object {
        const val ALL_QUESTS_REQUIRED: Int = 5
    }

    init {
        dbFile.parentFile?.mkdirs()

        val jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"

        db = Database.connect(jdbcUrl, "org.sqlite.JDBC")

        DriverManager.getConnection(jdbcUrl).use { conn ->
            conn.autoCommit = true
            conn.createStatement().use { statement -> statement.execute("PRAGMA journal_mode=WAL;") }
        }

        transaction(db) { SchemaUtils.createMissingTablesAndColumns(QuestProgressTable) }
    }

    /**
     * Loads the quests for the specified player.
     * @param playerId The UUID of the player.
     * @return A list of quests assigned to the player.
     */
    fun load(playerId: Uuid): List<QuestModule.Quest> {
        val playerUuid = playerId.toJavaUuid().toString()
        val questById =
            QuestModule.config.questModule.quests
                .associateBy { it.id }

        return transaction(db) {
            QuestProgressTable
                .selectAll()
                .where { QuestProgressTable.playerUuid eq playerUuid }
                .mapNotNull { row ->
                    val id = row[QuestProgressTable.questId]
                    val progress = row[QuestProgressTable.currentProgress].coerceAtLeast(0)
                    val base = questById[id] ?: return@mapNotNull null

                    base.copy(requirement = base.requirement.copy()).also { q ->
                        q.requirement.currentProgress = progress
                    }
                }
        }
    }

    /**
     * Saves the quests for the specified player.
     * @param playerId The UUID of the player.
     * @param quests The list of quests to save.
     */
    fun save(
        playerId: Uuid,
        quests: List<QuestModule.Quest>,
    ) {
        val playerUuid = playerId.toJavaUuid().toString()

        transaction(db) {
            QuestProgressTable.deleteWhere { QuestProgressTable.playerUuid eq playerUuid }

            quests.forEach { quest ->
                QuestProgressTable.insertIgnore { row ->
                    row[QuestProgressTable.playerUuid] = playerUuid
                    row[QuestProgressTable.questId] = quest.id
                    row[QuestProgressTable.currentProgress] = quest.requirement.currentProgress
                }
            }
        }
    }

    /**
     * Checks if the player has claimed the "all quests completed" reward.
     * @param playerId The UUID of the player.
     * @return True if the player has claimed the reward, false otherwise.
     */
    fun hasClaimedAllReward(playerId: Uuid): Boolean {
        val playerUuid = playerId.toJavaUuid().toString()
        val questById =
            QuestModule.config.questModule.quests
                .associateBy { it.id }

        return transaction(db) {
            val completedCount =
                QuestProgressTable
                    .selectAll()
                    .where { QuestProgressTable.playerUuid eq playerUuid }
                    .count { row ->
                        val id = row[QuestProgressTable.questId]
                        val progress = row[QuestProgressTable.currentProgress].coerceAtLeast(0)
                        val target = questById[id]?.requirement?.targetAmount ?: return@count false

                        progress >= target
                    }

            completedCount >= ALL_QUESTS_REQUIRED
        }
    }
}
