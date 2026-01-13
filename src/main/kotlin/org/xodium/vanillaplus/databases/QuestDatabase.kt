@file:OptIn(ExperimentalUuidApi::class)

@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.databases

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.QuestModule
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Manages the quest database. */
internal class QuestDatabase {
    private val dbFile = File(instance.dataFolder, "quests.db")
    private val db: Database

    private object QuestProgressTable : Table("quest_progress") {
        val playerUuid = text("player_uuid")
        val difficulty = text("difficulty")
        val reqEntityType = text("req_entity_type").nullable()
        val reqMaterial = text("req_material").nullable()
        val targetAmount = integer("target_amount")
        val currentProgress = integer("current_progress")
        val rewardType = text("reward_type")
        val rewardAmount = integer("reward_amount")

        init {
            index(false, playerUuid)
        }
    }

    private object AllRewardClaimedTable : Table("quest_all_reward_claimed") {
        val playerUuid = text("player_uuid")

        override val primaryKey = PrimaryKey(playerUuid, name = "pk_quest_all_reward_claimed")
    }

    init {
        dbFile.parentFile?.mkdirs()

        db =
            Database.connect(
                url = "jdbc:sqlite:${dbFile.absolutePath}",
                driver = "org.sqlite.JDBC",
            )

        transaction(db) {
            exec("PRAGMA journal_mode=WAL;")
            SchemaUtils.createMissingTablesAndColumns(QuestProgressTable, AllRewardClaimedTable)
        }
    }

    /**
     * Loads the quests for the specified player.
     * @param playerId The UUID of the player.
     * @return A list of quests assigned to the player.
     */
    fun load(playerId: Uuid): List<QuestModule.Quest> {
        val playerUuid = playerId.toJavaUuid().toString()

        return transaction(db) {
            QuestProgressTable
                .selectAll()
                .where { QuestProgressTable.playerUuid eq playerUuid }
                .map { row ->
                    val difficulty =
                        QuestModule.Quest.Difficulty.valueOf(row[QuestProgressTable.difficulty])
                    val reqEntity =
                        row[QuestProgressTable.reqEntityType]
                            ?.let { EntityType.valueOf(it) }
                    val reqMaterial =
                        row[QuestProgressTable.reqMaterial]
                            ?.let { Material.valueOf(it) }
                    val targetAmount = row[QuestProgressTable.targetAmount]
                    val currentProgress = row[QuestProgressTable.currentProgress]
                    val rewardType = Material.valueOf(row[QuestProgressTable.rewardType])
                    val rewardAmount = row[QuestProgressTable.rewardAmount]
                    val req =
                        QuestModule.Quest.Requirement(reqEntity, reqMaterial, targetAmount).also {
                            it.currentProgress = currentProgress
                        }
                    val reward = QuestModule.Quest.Reward(rewardType, rewardAmount)

                    QuestModule.Quest(difficulty, req, reward)
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
                QuestProgressTable.insertIgnore {
                    it[QuestProgressTable.playerUuid] = playerUuid
                    it[QuestProgressTable.difficulty] = quest.difficulty.name
                    it[QuestProgressTable.reqEntityType] = quest.requirement.entityType?.name
                    it[QuestProgressTable.reqMaterial] = quest.requirement.material?.name
                    it[QuestProgressTable.targetAmount] = quest.requirement.targetAmount
                    it[QuestProgressTable.currentProgress] = quest.requirement.currentProgress
                    it[QuestProgressTable.rewardType] = quest.reward.type.name
                    it[QuestProgressTable.rewardAmount] = quest.reward.amount
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

        return transaction(db) {
            !AllRewardClaimedTable
                .select(AllRewardClaimedTable.playerUuid)
                .where { AllRewardClaimedTable.playerUuid eq playerUuid }
                .limit(1)
                .empty()
        }
    }

    /**
     * Sets the claimed status of the "all quests completed" reward for the player.
     * @param playerId The UUID of the player.
     * @param claimed True to mark as claimed, false to unmark.
     */
    fun setClaimedAllReward(
        playerId: Uuid,
        claimed: Boolean,
    ) {
        val playerUuid = playerId.toJavaUuid().toString()

        transaction(db) {
            if (claimed) {
                AllRewardClaimedTable.insertIgnore {
                    it[AllRewardClaimedTable.playerUuid] = playerUuid
                }
            } else {
                AllRewardClaimedTable.deleteWhere { AllRewardClaimedTable.playerUuid eq playerUuid }
            }
        }
    }
}
