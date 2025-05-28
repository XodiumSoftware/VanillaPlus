/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.cdimascio.dotenv.dotenv
import io.papermc.paper.ban.BanListType
import kotlinx.coroutines.*
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DiscordCommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.time.Instant
import java.util.*

class DiscordModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DiscordModule.ENABLED

    private val token = dotenv()["DISCORD_BOT_TOKEN"]
    private val guildId = Snowflake(691029695894126623)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var kord: Kord

    init {
        if (token.isNullOrBlank()) instance.logger.warning("Warning: Discord bot token is not set!") else start(token)
    }

    /**
     * Starts the Discord bot and registers commands.
     * @param token The Discord bot token.
     */
    private fun start(token: String) {
        scope.launch {
            try {
                kord = Kord(token)
                kord.createGuildChatInputCommand(guildId, "whitelist", "Manage the whitelist") {
                    string("action", "Add, remove, or list players on the whitelist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                        choice("list", "list")
                    }
                    string("player", "The player name to whitelist") { required = false }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.createGuildChatInputCommand(guildId, "blacklist", "Manage the blacklist") {
                    string("action", "Add or remove the player from the blacklist") {
                        required = true
                        choice("add", "add")
                        choice("remove", "remove")
                        choice("list", "list")
                    }
                    string("player", "The player name to blacklist") { required = false }
                    defaultMemberPermissions = Permissions(Permission.Administrator)
                }
                kord.createGuildChatInputCommand(guildId, "map", "Open the online server map") {}
                kord.on<ChatInputCommandInteractionCreateEvent> { handleListCommand(this) }
                kord.login {}
            } catch (e: Exception) {
                instance.logger.severe("Failed to start Discord bot: ${e.message}")
            }
        }
    }

    private val discordCommands = listOf(
        DiscordCommandData("whitelist", "list") { interaction, _ ->
            val whitelisted =
                instance.server.whitelistedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
            respond(interaction, "Whitelisted Players", whitelisted.ifEmpty { "No players are whitelisted." })
        },
        DiscordCommandData("blacklist", "list") { interaction, _ ->
            val blacklisted = instance.server.bannedPlayers.joinToString(", ") { it.name ?: it.uniqueId.toString() }
            respond(interaction, "Blacklisted Players", blacklisted.ifEmpty { "No players are blacklisted." })
        },
        DiscordCommandData("whitelist", "add") { interaction, playerName ->
            if (playerName.isBlank()) {
                respond(interaction, "Invalid Input", "Please provide a valid player name.")
                return@DiscordCommandData
            }
            updateList("whitelist", "add", playerName)
            respond(interaction, "Whitelist Update", "Player `$playerName` has been whitelisted.")
        },
        DiscordCommandData("whitelist", "remove") { interaction, playerName ->
            if (playerName.isBlank()) {
                respond(interaction, "Invalid Input", "Please provide a valid player name.")
                return@DiscordCommandData
            }
            updateList("whitelist", "remove", playerName)
            respond(interaction, "Whitelist Update", "Player `$playerName` has been removed from the whitelist.")
        },
        DiscordCommandData("blacklist", "add") { interaction, playerName ->
            if (playerName.isBlank()) {
                respond(interaction, "Invalid Input", "Please provide a valid player name.")
                return@DiscordCommandData
            }
            updateList("blacklist", "add", playerName)
            respond(interaction, "Blacklist Update", "Player `$playerName` has been blacklisted.")
        },
        DiscordCommandData("blacklist", "remove") { interaction, playerName ->
            if (playerName.isBlank()) {
                respond(interaction, "Invalid Input", "Please provide a valid player name.")
                return@DiscordCommandData
            }
            updateList("blacklist", "remove", playerName)
            respond(interaction, "Blacklist Update", "Player `$playerName` has been removed from the blacklist.")
        }
    )

    /**
     * Updates the whitelist or blacklist for a player.
     * @param command The command type ("whitelist" or "blacklist").
     * @param action The action to perform ("add" or "remove").
     * @param playerName The name of the player to update.
     */
    private suspend fun updateList(command: String, action: String, playerName: String) = withContext(Dispatchers.IO) {
        instance.server.scheduler.runTask(instance, Runnable {
            val offlinePlayer = instance.server.getOfflinePlayer(playerName)
            val banList = instance.server.getBanList(BanListType.PROFILE)
            when (command) {
                "whitelist" -> offlinePlayer.isWhitelisted = (action == "add")
                "blacklist" -> if (action == "add")
                    banList.addBan(offlinePlayer.playerProfile, "", Date.from(Instant.MAX), "")
                else
                    banList.pardon(offlinePlayer.playerProfile)
            }
        })
    }

    /**
     * Handles the Discord command interaction for listing or managing players.
     * @param event The event containing the interaction data.
     */
    private suspend fun handleListCommand(event: ChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val commandName = interaction.command.rootName
        val action = interaction.command.strings["action"] ?: ""
        val playerName = interaction.command.strings["player"] ?: ""

        if (commandName == "map") {
            respond(interaction, "Open the Online Server Map", "Click the title above to open the map.")
            return
        }

        val command = discordCommands.find { it.name == commandName && it.action == action }
        if (command != null) {
            command.handler(interaction, playerName)
        } else {
            respond(interaction, "Unknown Command", "This command/action is not recognized.")
        }
    }

    /**
     * Responds to a Discord interaction with an ephemeral message.
     * @param interaction The interaction to respond to.
     * @param title The title of the response embed.
     * @param description The description of the response embed.
     */
    private suspend fun respond(interaction: ChatInputCommandInteraction, title: String, description: String) {
        interaction.respondEphemeral { embeds = buildEmbed(title, description) }
    }

    /**
     * Builds an embed message with the specified title and description.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @return A list containing the constructed EmbedBuilder.
     */
    private fun buildEmbed(title: String, description: String): MutableList<EmbedBuilder> = mutableListOf(
        EmbedBuilder().apply {
            this.title = title
            this.description = description
            this.color = Color(0x00FF00)
        }
    )

    /**
     * Sends an embed message in response to an interaction.
     * @param title The title of the embed.
     * @param description The description of the embed.
     * @param color The color of the embed in hexadecimal format.
     */
    suspend fun sendEventEmbed(
        title: String,
        description: String,
        color: Int
    ) {
        kord.getChannelOf<TextChannel>(Snowflake(1285516564153761883))?.createMessage {
            embeds = mutableListOf(
                EmbedBuilder().apply {
                    this.title = title
                    this.description = description
                    this.color = Color(color)
                }
            )
        }
    }
}